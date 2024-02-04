package com.cbconnectit.domain.models.user

import com.cbconnectit.data.dto.requests.user.NameAble
import com.cbconnectit.data.dto.requests.user.UserDto
import com.cbconnectit.domain.models.interfaces.DateAble
import com.cbconnectit.utils.toDatabaseString
import io.ktor.server.auth.*
import java.time.LocalDateTime
import java.util.UUID

data class User(
    val id: UUID = UUID.randomUUID(),
    override val fullName: String? = null,
    val username: String = "",
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now(),
    val role: UserRoles = UserRoles.User,
    @Transient val password: String? = null,
) : NameAble, Principal

fun User.toDto() = UserDto(
    id = this.id.toString(),
    fullName = this.fullName,
    username = this.username,
    createdAt = this.createdAt.toDatabaseString(),
    updatedAt = this.updatedAt.toDatabaseString(),
    role = this.role
)

enum class UserRoles {
    User,
    Admin
}