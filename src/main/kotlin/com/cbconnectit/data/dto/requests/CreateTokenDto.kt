package com.cbconnectit.data.dto.requests

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateTokenDto(
    val username: String,
    val password: String
)

fun CreateTokenDto.hasData() = username.isNotBlank() && password.isNotBlank()

@Serializable
data class RefreshTokenDto(
    @SerialName("refresh_token")
    val refreshToken: String
)

fun RefreshTokenDto.hasData() = refreshToken.isNotBlank()
