package com.cbconnectit.data.dto.requests.link

import com.cbconnectit.utils.isValidUrl
import kotlinx.serialization.Serializable

@Serializable
data class InsertNewLink(
    val url: String
) {
    val isValid get() = url.isNotBlank() && url.isValidUrl
}

@Serializable
data class UpdateLink(
    val url: String
) {
    val isValid get() = url.isNotBlank() && url.isValidUrl
}
