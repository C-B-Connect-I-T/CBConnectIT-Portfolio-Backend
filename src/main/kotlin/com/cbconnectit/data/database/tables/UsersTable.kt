package com.cbconnectit.data.database.tables

import com.cbconnectit.data.database.tables.Constants.normalTextSize
import com.cbconnectit.data.database.tables.Constants.smallerTextSize
import com.cbconnectit.domain.models.user.User
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.javatime.CurrentDateTime
import org.jetbrains.exposed.sql.javatime.datetime

object UsersTable : UUIDTable() {
    val fullName = varchar("full_name", smallerTextSize).nullable().default(null)
    val username = varchar("username", smallerTextSize).uniqueIndex()
    val password = varchar("password", normalTextSize)
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)
    val updatedAt = datetime("updated_at").defaultExpression(CurrentDateTime)
    val role = enumeration<User.Role>("role").default(User.Role.User)
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
