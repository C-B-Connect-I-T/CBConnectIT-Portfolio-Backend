package com.cbconnectit.data.dto.requests.user

import com.google.gson.annotations.SerializedName

data class InsertNewUser(
    @SerializedName("full_name")
    override val fullName: String,
    val username: String,
    override val password: String,
    @SerializedName("repeat_password")
    override val repeatPassword: String?
) : PasswordAble, NameAble {
    val isValid get() = fullName.isNotBlank() && password.isNotBlank() && repeatPassword?.isNotBlank() == true && username.isNotBlank()
}
