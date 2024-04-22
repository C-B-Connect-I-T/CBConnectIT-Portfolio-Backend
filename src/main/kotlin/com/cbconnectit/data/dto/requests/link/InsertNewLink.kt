package com.cbconnectit.data.dto.requests.link

import com.cbconnectit.utils.isValidUrl

data class InsertNewLink(
    val url: String
) {
    val isValid get() = url.isNotBlank() && url.isValidUrl
}

data class UpdateLink(
    val url: String
) {
    val isValid get() = url.isNotBlank() && url.isValidUrl
}
