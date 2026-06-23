package com.cbconnectit.data.dto.requests.mediafile

import com.cbconnectit.domain.models.mediafile.MediaType
import com.cbconnectit.domain.models.mediafile.OwnerType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class InsertMediaFile(
    @SerialName("owner_id")
    val ownerId: String,
    @SerialName("owner_type")
    val ownerType: OwnerType,
    @SerialName("media_type")
    val mediaType: MediaType,
    @SerialName("alt_text")
    val altText: String = ""
)

@Serializable
data class UpdateMediaFile(
    @SerialName("alt_text")
    val altText: String? = null
)
