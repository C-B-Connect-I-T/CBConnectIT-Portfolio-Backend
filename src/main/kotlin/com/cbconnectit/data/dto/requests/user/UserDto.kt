package com.cbconnectit.data.dto.requests.user

import com.cbconnectit.domain.models.interfaces.DateAble
import com.cbconnectit.domain.models.user.UserRoles
import com.google.gson.annotations.SerializedName

data class UserDto(
    val id: Int = 0,
    @SerializedName("full_name")
    override val fullName: String = "",
    val username: String = "",
    @SerializedName("created_at")
    override val createdAt: String = "",
    @SerializedName("updated_at")
    override val updatedAt: String = "",
    val role: UserRoles = UserRoles.User
) : DateAble, NameAble