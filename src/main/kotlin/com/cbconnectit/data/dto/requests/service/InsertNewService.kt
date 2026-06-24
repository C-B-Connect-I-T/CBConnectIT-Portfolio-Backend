package com.cbconnectit.data.dto.requests.service

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class InsertNewService(
    val title: String,
    @SerialName("image_alt_text")
    val imageAltText: String? = null,
    @SerialName("banner_image_alt_text")
    val bannerImageAltText: String? = null,
    @SerialName("short_description")
    val shortDescription: String? = null,
    val description: String,
    @SerialName("banner_description")
    val bannerDescription: String? = null,
    @SerialName("extra_info")
    val extraInfo: String? = null,
    @SerialName("parent_service_id")
    val parentServiceId: String? = null,
    @SerialName("tag_id")
    val tagId: String? = null
) {
    val parentServiceUuid get() = parentServiceId?.let { UUID.fromString(it) }
    val tagUuid: UUID? get() = tagId?.let { id -> UUID.fromString(id) }
    val isValid get() = title.isNotBlank() && description.isNotBlank()
}

@Serializable
data class UpdateService(
    val title: String,
    @SerialName("image_alt_text")
    val imageAltText: String? = null,
    @SerialName("banner_image_alt_text")
    val bannerImageAltText: String? = null,
    @SerialName("short_description")
    val shortDescription: String? = null,
    val description: String,
    @SerialName("banner_description")
    val bannerDescription: String? = null,
    @SerialName("extra_info")
    val extraInfo: String? = null,
    @SerialName("parent_service_id")
    val parentServiceId: String? = null,
    @SerialName("tag_id")
    val tagId: String? = null
) {
    val parentServiceUuid get() = parentServiceId?.let { UUID.fromString(it) }
    val tagUuid: UUID? get() = tagId?.let { id -> UUID.fromString(id) }
    val isValid get() = title.isNotBlank() && description.isNotBlank()
}
