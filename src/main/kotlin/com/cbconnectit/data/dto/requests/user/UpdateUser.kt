package com.cbconnectit.data.dto.requests.user

import com.google.gson.annotations.SerializedName

data class UpdateUser(
    @SerializedName("full_name")
    override val fullName: String? = null,
    val username: String? = null
) : NameAble {

    val isValid get() = !fullName.isNullOrBlank() || !username.isNullOrBlank()
}
