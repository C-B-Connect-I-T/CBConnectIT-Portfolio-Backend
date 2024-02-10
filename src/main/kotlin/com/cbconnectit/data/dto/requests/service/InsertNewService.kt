package com.cbconnectit.data.dto.requests.service

import com.google.gson.annotations.SerializedName

data class InsertNewService(
    val name: String,
    @SerializedName("parent_service_id")
    val parentServiceId: String? = null,
    @SerializedName("tag_id")
    val tagId: String
) {
    val isValid get() = name.isNotBlank() && tagId.isNotBlank()
}

data class UpdateService(
    val name: String,
    @SerializedName("parent_service_id")
    val parentServiceId: String? = null,
    @SerializedName("tag_id")
    val tagId: String
) {
    val isValid get() = name.isNotBlank() && tagId.isNotBlank()
}
