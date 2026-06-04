package com.cbconnectit.domain.models.user

import com.cbconnectit.data.dto.requests.user.NameAble
import com.cbconnectit.data.dto.requests.user.UserDto
import com.cbconnectit.utils.toDatabaseString
import java.time.LocalDateTime
import java.util.*

data class User(
    val id: UUID = UUID.randomUUID(),
    override val fullName: String? = null,
    val username: String = "",
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now(),
    val role: Role = Role.User,
    @Transient val password: String? = null,
) : NameAble {

    enum class Role {
        User,
        Admin
    }

    val isAdmin: Boolean
        get() = role == Role.Admin
}

fun User.toDto() = UserDto(
    id = this.id.toString(),
    fullName = this.fullName,
    username = this.username,
    createdAt = this.createdAt.toDatabaseString(),
    updatedAt = this.updatedAt.toDatabaseString(),
    role = this.role
)
