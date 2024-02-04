package com.cbconnectit.data.database.tables

import com.cbconnectit.domain.models.user.User
import com.cbconnectit.domain.models.user.UserRoles
import com.cbconnectit.utils.toDatabaseString
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ResultRow
import java.time.LocalDateTime

object UsersTable : UUIDTable() {
    val fullName = varchar("full_name", 100).nullable().default(null)
    val username = varchar("username", 100).uniqueIndex()
    val password = varchar("password", 255)
    val createdAt = varchar("created_at", 255).default(LocalDateTime.now().toDatabaseString())
    val updatedAt = varchar("updated_at", 255).default(LocalDateTime.now().toDatabaseString())
    val role = enumeration<UserRoles>("role").default(UserRoles.User)
}

fun ResultRow.toUser() = User(
    id = this[UsersTable.id].value,
    fullName = this[UsersTable.fullName],
    username = this[UsersTable.username],
    createdAt = this[UsersTable.createdAt],
    updatedAt = this[UsersTable.updatedAt],
    role = this[UsersTable.role]
)

fun ResultRow.toUserHashable() = this.toUser().copy(password = this[UsersTable.password])

fun Iterable<ResultRow>.toUserHashable() = this.firstOrNull()?.toUserHashable()

fun Iterable<ResultRow>.toUsers() = this.map { it.toUser() }
fun Iterable<ResultRow>.toUser() = this.firstOrNull()?.toUser()
