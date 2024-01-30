package com.cbconnectit.data.dto.requests.tag

data class InsertNewTag(
    val name: String
) {
    val isValid get() = name.isNotBlank()
}

data class UpdateTag(
    val name: String
) {
    val isValid get() = name.isNotBlank()
}
