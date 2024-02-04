package com.cbconnectit.data.dto.requests.link

import com.cbconnectit.domain.models.link.LinkType
import com.cbconnectit.utils.isValidUrl

data class InsertNewLink(
    val projectId: String,
    val url: String,
    val type: LinkType
) {
    val isValid get() = projectId.isNotBlank() && url.isNotBlank() && url.isValidUrl
}

data class UpdateLink(
    val url: String,
    val type: LinkType
) {
    val isValid get() = url.isNotBlank() && url.isValidUrl
}

