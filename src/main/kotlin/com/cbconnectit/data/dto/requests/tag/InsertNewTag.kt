package com.cbconnectit.data.dto.requests.tag

import kotlinx.serialization.Serializable

@Serializable
data class InsertNewTag(
    val name: String
) {
    val isValid get() = name.isNotBlank()
}

@Serializable
data class UpdateTag(
    val name: String
) {
    val isValid get() = name.isNotBlank()
}
