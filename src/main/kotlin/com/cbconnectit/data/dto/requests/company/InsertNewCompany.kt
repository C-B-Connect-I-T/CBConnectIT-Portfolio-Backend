package com.cbconnectit.data.dto.requests.company

import kotlinx.serialization.Serializable

@Serializable
data class InsertNewCompany(
    val name: String,
    val links: List<String>? = null
) {
    val isValid get() = name.isNotBlank()
}

@Serializable
data class UpdateCompany(
    val name: String,
    val links: List<String>? = null
) {
    val isValid get() = name.isNotBlank()
}
