package com.cbconnectit.data.dto.requests.jobPosition

import kotlinx.serialization.Serializable

@Serializable
data class InsertNewJobPosition(
    val name: String
) {
    val isValid get() = name.isNotBlank()
}

@Serializable
data class UpdateJobPosition(
    val name: String
) {
    val isValid get() = name.isNotBlank()
}
