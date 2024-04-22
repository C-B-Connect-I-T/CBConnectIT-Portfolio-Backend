package com.cbconnectit.data.dto.requests

data class CreateTokenDto(
    val username: String,
    val password: String
)

fun CreateTokenDto.hasData() = username.isNotBlank() && password.isNotBlank()
