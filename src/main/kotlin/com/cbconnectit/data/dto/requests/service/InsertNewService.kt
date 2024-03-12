package com.cbconnectit.data.dto.requests.service

import com.google.gson.annotations.SerializedName
import java.util.*

data class InsertNewService(
    val title: String,
    @SerializedName("parent_service_id")
    val parentServiceId: String? = null,
    @SerializedName("tag_id")
    val tagId: String? = null
) {
    val parentServiceUuid get() = parentServiceId?.let { UUID.fromString(it) }
    val tagUuid: UUID? get() = tagId?.let { id -> UUID.fromString(id) }
    val isValid get() = title.isNotBlank()// && (tagId == null || tagId.isNotBlank())
}

data class UpdateService(
    val title: String,
    @SerializedName("parent_service_id")
    val parentServiceId: String? = null,
    @SerializedName("tag_id")
    val tagId: String? = null
) {
    val parentServiceUuid get() = parentServiceId?.let { UUID.fromString(it) }
    val tagUuid: UUID? get() = tagId?.let { id -> UUID.fromString(id) }
    val isValid get() = title.isNotBlank() //&& (tagId == null || tagId.isNotBlank())
}
