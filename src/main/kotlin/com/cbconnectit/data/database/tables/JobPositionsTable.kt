package com.cbconnectit.data.database.tables

import com.cbconnectit.data.database.tables.CompaniesTable.defaultExpression
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.javatime.CurrentDateTime
import org.jetbrains.exposed.sql.javatime.datetime

object JobPositionsTable: UUIDTable() {
    val name = varchar("name", 255)
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)
    val updatedAt = datetime("updated_at").defaultExpression(CurrentDateTime)
}