package com.cbconnectit.domain.models.mediafile

import com.cbconnectit.data.dto.responses.mediafile.CompactMediaFileDto
import com.cbconnectit.data.dto.responses.mediafile.MediaFileDto
import com.cbconnectit.services.StorageResult
import com.cbconnectit.utils.Parts
import com.cbconnectit.utils.toDatabaseString
import java.time.LocalDateTime
import java.util.*

data class MediaFile(
    val id: UUID = UUID.randomUUID(),
    val url: String = "",
    val ownerId: UUID = UUID.randomUUID(),
    val ownerType: OwnerType = OwnerType.PROJECT,
    val mediaType: MediaType = MediaType.IMAGE,
    val fileSize: Long = 0L,
    val originalFilename: String = "",
    val altText: String = "",
    val mimeType: String = "",
    val width: Int? = null,
    val height: Int? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
) {
    constructor(
        storageResult: StorageResult,
        imageFile: Parts.File?,
        ownerId: UUID,
        ownerType: OwnerType,
        mediaType: MediaType = MediaType.IMAGE,
        altText: String
    ) : this(
        url = storageResult.url,
        ownerId = ownerId,
        ownerType = ownerType,
        mediaType = mediaType,
        fileSize = storageResult.fileSize,
        originalFilename = storageResult.originalFilename,
        altText = altText,
        mimeType = storageResult.mimeType,
        width = imageFile?.width,
        height = imageFile?.height
    )
}

enum class OwnerType {
    TESTIMONIAL,
    SERVICE,
    PROJECT
}

enum class MediaType {
    IMAGE,
    BANNER
}

fun MediaFile.toDto() = MediaFileDto(
    id = this.id.toString(),
    url = this.url,
    ownerId = this.ownerId.toString(),
    ownerType = this.ownerType,
    mediaType = this.mediaType,
    fileSize = this.fileSize,
    originalFilename = this.originalFilename,
    altText = this.altText,
    mimeType = this.mimeType,
    width = this.width,
    height = this.height,
    createdAt = this.createdAt.toDatabaseString(),
    updatedAt = this.updatedAt.toDatabaseString()
)

fun MediaFile.toCompactDto() = CompactMediaFileDto(
    url = this.url,
    originalFilename = this.originalFilename,
    altText = this.altText,
    mimeType = this.mimeType
)
