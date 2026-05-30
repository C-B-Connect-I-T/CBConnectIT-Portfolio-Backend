package com.cbconnectit.data.dto.requests.user

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UpdatePassword(
    @SerialName("old_password")
    val oldPassword: String,
    override val password: String,
    @SerialName("repeat_password")
    override val repeatPassword: String?
) : PasswordAble
