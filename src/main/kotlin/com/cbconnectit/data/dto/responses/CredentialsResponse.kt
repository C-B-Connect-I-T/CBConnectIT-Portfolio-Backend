package com.cbconnectit.data.dto.responses

import com.google.gson.annotations.SerializedName

data class CredentialsResponse(
    @SerializedName("access_token")
    val accessToken: String,
    @SerializedName("refresh_token")
    val refreshToken: String,
    @SerializedName("expires_in")
    val expiresIn: Long,
    @SerializedName("token_type")
    val tokenType: String = "Bearer "
)
