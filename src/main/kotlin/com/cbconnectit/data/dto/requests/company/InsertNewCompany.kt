package com.cbconnectit.data.dto.requests.company

import com.cbconnectit.utils.isValidUrl
import kotlinx.serialization.Serializable

@Serializable
data class InsertNewCompany(
    val name: String,
    val links: List<String>? = null
) {
    val isValid get() = name.isNotBlank() && (links == null || links.all { it.isNotBlank() && it.isValidUrl })
}

@Serializable
data class UpdateCompany(
    val name: String,
    val links: List<String>? = null
) {
    val isValid get() = name.isNotBlank() && (links == null || links.all { it.isNotBlank() && it.isValidUrl })
}
