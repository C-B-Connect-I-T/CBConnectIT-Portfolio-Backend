package com.cbconnectit.data.database.tables

import com.cbconnectit.domain.models.service.Service
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.javatime.CurrentDateTime
import org.jetbrains.exposed.sql.javatime.datetime

object ServicesTable : UUIDTable() {
    val name = varchar("name", 255).uniqueIndex()
    val tagId = reference("tag_id", TagsTable, ReferenceOption.NO_ACTION)
    val parentServiceId = optReference("parent_service_id", ServicesTable, ReferenceOption.CASCADE).default(null)
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)
    val updatedAt = datetime("updated_at").defaultExpression(CurrentDateTime)
}

fun ResultRow.toService() = Service(
    id = this[ServicesTable.id].value,
    name = this[ServicesTable.name],
    tag = this.toTag(),
    createdAt = this[ServicesTable.createdAt],
    updatedAt = this[ServicesTable.updatedAt]
)

fun Iterable<ResultRow>.toServices() = this.map { it.toService() }
fun Iterable<ResultRow>.toService() = this.firstOrNull()?.toService()
