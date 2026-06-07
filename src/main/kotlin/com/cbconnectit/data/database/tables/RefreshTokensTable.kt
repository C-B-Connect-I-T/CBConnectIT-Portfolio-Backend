package com.cbconnectit.data.database.tables

import com.cbconnectit.data.database.tables.Constants.mediumTextSize
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.javatime.datetime

object RefreshTokensTable : UUIDTable() {
    val userId = uuid("user_id").references(UsersTable.id)
    val token = varchar("token", mediumTextSize).uniqueIndex()
    val createdAt = datetime("created_at")
    val expiresAt = datetime("expires_at")
    val invalidated = bool("invalidated").default(false)
    val replacedByToken = text("replaced_by_token").nullable()
    val replacedAt = datetime("replaced_at").nullable()
}
