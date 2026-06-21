package com.cbconnectit.utils

import io.ktor.http.content.*
import io.ktor.utils.io.*
import kotlinx.serialization.json.Json
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import javax.imageio.ImageIO

fun List<Parts>.getFile(name: String = "image"): Parts.File? =
    firstOrNull { it.name == name && it is Parts.File } as? Parts.File

fun List<Parts>.getFiles(nameStartsWith: String = "media"): List<Parts.File> =
    filter { it.name.startsWith(nameStartsWith) && it is Parts.File }.filterIsInstance<Parts.File>()

inline fun <reified T> List<Parts>.getPayload(json: Json, name: String = "payload"): T? {
    val payloadFormItem = this.firstOrNull { it.name == name && it is Parts.Form } as? Parts.Form
        ?: return null

    return try {
        json.decodeFromString<T>(payloadFormItem.formData)
    } catch (_: Exception) {
        null
    }
}

suspend fun MultiPartData.toParts(): List<Parts> {
    val parts = mutableListOf<Parts>()
    this.forEachPart { part ->
        try {
            when (part) {
                is PartData.FormItem -> parts.add(Parts.Form(part.value, part.name ?: ""))

                is PartData.FileItem -> {
                    val fileName = part.originalFileName ?: return@forEachPart
                    val contentType = part.contentType?.toString() ?: return@forEachPart
                    val fileBytes = part.provider().toByteArray()
                    val image: BufferedImage? = try {
                        ImageIO.read(ByteArrayInputStream(fileBytes))
                    } catch (_: Exception) {
                        null
                    }

                    parts.add(
                        Parts.File(
                            name = part.name ?: "",
                            fileName = fileName,
                            contentType = contentType,
                            size = fileBytes.size.toLong(),
                            height = image?.height,
                            width = image?.width,
                            data = fileBytes
                        )
                    )
                }

                else -> Unit
            }
        } finally {
            part.dispose()
        }
    }
    return parts
}

interface Parts {
    val name: String

    data class Form(
        val formData: String,
        override val name: String
    ) : Parts

    data class File(
        override val name: String,
        val fileName: String,
        val contentType: String,
        val height: Int?,
        val width: Int?,
        val size: Long,
        val data: ByteArray
    ) : Parts {
        // Override equals/hashCode to handle ByteArray correctly in data class
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as File

            if (name != other.name) return false
            if (fileName != other.fileName) return false
            if (contentType != other.contentType) return false
            if (height != other.height) return false
            if (width != other.width) return false
            if (size != other.size) return false
            if (!data.contentEquals(other.data)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = name.hashCode()
            result = 31 * result + fileName.hashCode()
            result = 31 * result + contentType.hashCode()
            result = 31 * result + (height ?: 0)
            result = 31 * result + (width ?: 0)
            result = 31 * result + size.hashCode()
            result = 31 * result + data.contentHashCode()
            return result
        }
    }
}
