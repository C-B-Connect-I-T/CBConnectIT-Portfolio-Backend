package com.cbconnectit.data.dto.requests.user

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class InsertNewUser(
    @SerialName("full_name")
    override val fullName: String,
    val username: String,
    override val password: String,
    @SerialName("repeat_password")
    override val repeatPassword: String?
) : PasswordAble, NameAble {
    val isValid get() = fullName.isNotBlank() && password.isNotBlank() && repeatPassword?.isNotBlank() == true && username.isNotBlank()
}
