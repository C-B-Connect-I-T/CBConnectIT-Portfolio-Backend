package com.cbconnectit.data.dto.responses

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AuthStatusResponse(
    val authenticated: Boolean,
    val role: String? = null,
    @SerialName("user_id")
    val userId: String? = null,
    val username: String? = null
)
