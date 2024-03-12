package com.cbconnectit.data.dto.requests.service

import com.google.gson.annotations.SerializedName
import java.util.*

data class InsertNewService(
    val title: String,
    @SerializedName("image_url")
    val imageUrl: String,
    @SerializedName("short_description")
    val shortDescription: String? = null,
    val description: String,
    @SerializedName("banner_description")
    val bannerDescription: String? = null,
    @SerializedName("extra_info")
    val extraInfo: String? = null,
    @SerializedName("parent_service_id")
    val parentServiceId: String? = null,
    @SerializedName("tag_id")
    val tagId: String? = null
) {
    val parentServiceUuid get() = parentServiceId?.let { UUID.fromString(it) }
    val tagUuid: UUID? get() = tagId?.let { id -> UUID.fromString(id) }
    val isValid get() = title.isNotBlank() && description.isNotBlank() && imageUrl.isNotBlank() // && (tagId == null || tagId.isNotBlank())
}

data class UpdateService(
    val title: String,
    @SerializedName("image_url")
    val imageUrl: String,
    @SerializedName("short_description")
    val shortDescription: String? = null,
    val description: String,
    @SerializedName("banner_description")
    val bannerDescription: String? = null,
    @SerializedName("extra_info")
    val extraInfo: String? = null,
    @SerializedName("parent_service_id")
    val parentServiceId: String? = null,
    @SerializedName("tag_id")
    val tagId: String? = null
) {
    val parentServiceUuid get() = parentServiceId?.let { UUID.fromString(it) }
    val tagUuid: UUID? get() = tagId?.let { id -> UUID.fromString(id) }
    val isValid get() = title.isNotBlank() && description.isNotBlank() && imageUrl.isNotBlank() //&& (tagId == null || tagId.isNotBlank())
}
