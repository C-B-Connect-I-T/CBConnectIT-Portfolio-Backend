package com.cbconnectit.data.dto.requests.user

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UpdateUser(
    @SerialName("full_name")
    override val fullName: String? = null,
    val username: String? = null
) : NameAble {

    val isValid get() = !fullName.isNullOrBlank() || !username.isNullOrBlank()
}
