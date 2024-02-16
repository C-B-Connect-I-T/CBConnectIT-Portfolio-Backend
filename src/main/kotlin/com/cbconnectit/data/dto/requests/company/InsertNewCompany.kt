package com.cbconnectit.data.dto.requests.company

data class InsertNewCompany(
    val name: String,
    val linkIds: List<String>? = null
) {
    val isValid get() = name.isNotBlank()
}

data class UpdateCompany(
    val name: String,
    val linkIds: List<String>? = null
) {
    val isValid get() = name.isNotBlank()
}