package com.cbconnectit.data.dto.responses.mediafile

import com.cbconnectit.domain.models.interfaces.DateAble
import com.cbconnectit.domain.models.mediafile.MediaType
import com.cbconnectit.domain.models.mediafile.OwnerType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MediaFileDto(
    val id: String = "",
    val url: String = "",
    @SerialName("owner_id")
    val ownerId: String = "",
    @SerialName("owner_type")
    val ownerType: OwnerType,
    @SerialName("media_type")
    val mediaType: MediaType,
    @SerialName("file_size")
    val fileSize: Long = 0L,
    @SerialName("original_filename")
    val originalFilename: String = "",
    @SerialName("alt_text")
    val altText: String = "",
    @SerialName("mime_type")
    val mimeType: String = "",
    val width: Int? = null,
    val height: Int? = null,
    @SerialName("created_at")
    override val createdAt: String = "",
    @SerialName("updated_at")
    override val updatedAt: String = "",
) : DateAble

@Serializable
data class CompactMediaFileDto(
    val url: String = "",
    @SerialName("original_filename")
    val originalFilename: String = "",
    @SerialName("alt_text")
    val altText: String = "",
    @SerialName("mime_type")
    val mimeType: String = ""
)
