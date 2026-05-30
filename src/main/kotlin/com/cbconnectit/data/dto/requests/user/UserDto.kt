package com.cbconnectit.data.dto.requests.user

import com.cbconnectit.domain.models.interfaces.DateAble
import com.cbconnectit.domain.models.user.UserRoles
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserDto(
    val id: String = "",
    @SerialName("full_name")
    override val fullName: String? = null,
    val username: String = "",
    @SerialName("created_at")
    override val createdAt: String = "",
    @SerialName("updated_at")
    override val updatedAt: String = "",
    val role: UserRoles = UserRoles.User
) : DateAble, NameAble
