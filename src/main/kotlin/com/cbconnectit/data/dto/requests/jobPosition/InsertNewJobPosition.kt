package com.cbconnectit.data.dto.requests.jobPosition

data class InsertNewJobPosition(
    val name: String
) {
    val isValid get() = name.isNotBlank()
}

data class UpdateJobPosition(
    val name: String
) {
    val isValid get() = name.isNotBlank()
}
