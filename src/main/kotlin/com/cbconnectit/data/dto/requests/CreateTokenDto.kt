package com.cbconnectit.data.dto.requests

import kotlinx.serialization.Serializable

@Serializable
data class CreateTokenDto(
    val username: String,
    val password: String
)

fun CreateTokenDto.hasData() = username.isNotBlank() && password.isNotBlank()
