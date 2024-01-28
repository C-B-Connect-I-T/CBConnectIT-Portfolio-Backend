package com.cbconnectit.domain.models.user

import com.cbconnectit.data.dto.requests.user.NameAble
import com.cbconnectit.data.dto.requests.user.UserDto
import com.cbconnectit.domain.models.interfaces.DateAble
import io.ktor.server.auth.*

data class User(
    val id: Int = 0,
    override val fullName: String = "",
    val username: String = "",
    override val createdAt: String = "",
    override val updatedAt: String = "",
    val role: UserRoles = UserRoles.User,
    @Transient val password: String? = null,
) : DateAble, NameAble, Principal

fun User.toDto() = UserDto(
    id = this.id,
    fullName = this.fullName,
    username = this.username,
    createdAt = this.createdAt,
    updatedAt = this.updatedAt,
    role = this.role
)

enum class UserRoles {
    User,
    Admin
}