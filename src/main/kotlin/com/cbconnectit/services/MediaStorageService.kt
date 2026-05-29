package com.cbconnectit.services

import com.cbconnectit.domain.models.Environment
import io.ktor.http.content.*
import java.io.File
import java.util.*

/**
 * Interface for media storage operations.
 * This allows easy replacement with cloud storage providers (S3, Azure, GCS, etc.)
 */
interface MediaStorageService {
    /**
     * Stores a file and returns the full URL to access it
     * @param file The file metadata (for filename, content type, etc.)
     * @param fileBytes The actual file content as byte array
     */
    suspend fun store(file: PartData.FileItem, fileBytes: ByteArray): StorageResult

    /**
     * Stores file bytes directly
     * @param fileBytes The file content as byte array
     * @param originalFileName The original filename
     * @param mimeType The MIME type (optional)
     */
    suspend fun storeFromBytes(fileBytes: ByteArray, originalFileName: String, mimeType: String?): StorageResult

    /**
     * Deletes a file by its URL
     */
    suspend fun delete(url: String): Boolean

    /**
     * Checks if a file exists
     */
    suspend fun exists(url: String): Boolean
}

data class StorageResult(
    val url: String,
    val fileSize: Long,
    val mimeType: String,
    val originalFilename: String
)

/**
 * Local file system implementation for development.
 * In production, this should be replaced with a cloud storage provider.
 */
class LocalMediaStorageService(
    private val uploadDirectory: String = "uploads",
    private val environment: Environment
) : MediaStorageService {

    init {
        // Ensure upload directory exists
        File(uploadDirectory).mkdirs()
    }

    override suspend fun store(file: PartData.FileItem, fileBytes: ByteArray): StorageResult {
        val originalFilename = file.originalFileName ?: "unknown"
        val mimeType = file.contentType?.toString()
        return storeFromBytes(fileBytes, originalFilename, mimeType)
    }

    override suspend fun storeFromBytes(fileBytes: ByteArray, originalFileName: String, mimeType: String?): StorageResult {
        val extension = originalFileName.substringAfterLast(".", "bin")
        val uniqueFilename = "${UUID.randomUUID()}.$extension"
        val uploadPath = File(uploadDirectory, uniqueFilename)

        // Write byte array to file
        uploadPath.writeBytes(fileBytes)

        val normalizedEnvironment = if (environment.clientUrl.contains("cb-connect-it")) {
            environment.clientUrl
        } else {
            "http://localhost:8080"
        }
        val fileSize = uploadPath.length()
        val finalMimeType = mimeType ?: "application/octet-stream"
        val url = "$normalizedEnvironment/api/$uploadDirectory/$uniqueFilename"

        return StorageResult(
            url = url,
            fileSize = fileSize,
            mimeType = finalMimeType,
            originalFilename = originalFileName
        )
    }

    override suspend fun delete(url: String): Boolean {
        // Extract filename from URL
        val filename = url.substringAfterLast("/")
        val file = File(uploadDirectory, filename)

        return if (file.exists()) {
            file.delete()
        } else {
            false
        }
    }

    override suspend fun exists(url: String): Boolean {
        val filename = url.substringAfterLast("/")
        val file = File(uploadDirectory, filename)
        return file.exists()
    }
}
