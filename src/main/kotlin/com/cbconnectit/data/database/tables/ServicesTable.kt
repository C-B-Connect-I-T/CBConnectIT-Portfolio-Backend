package com.cbconnectit.data.database.tables

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.javatime.CurrentDateTime
import org.jetbrains.exposed.sql.javatime.datetime

object ServicesTable : UUIDTable() {
    val name = varchar("name", 255).uniqueIndex()
    val tagId = reference("tag_id", TagsTable, ReferenceOption.NO_ACTION)
    val parentServiceId = reference("parent_service_id", ServicesTable, ReferenceOption.CASCADE)
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)
    val updatedAt = datetime("updated_at").defaultExpression(CurrentDateTime)
}