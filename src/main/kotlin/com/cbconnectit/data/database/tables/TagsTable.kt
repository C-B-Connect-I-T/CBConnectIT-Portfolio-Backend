package com.cbconnectit.data.database.tables

import com.cbconnectit.domain.models.tag.Tag
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.javatime.CurrentDateTime
import org.jetbrains.exposed.sql.javatime.datetime

object TagsTable: UUIDTable() {
    val name = varchar("name", 255).uniqueIndex()
    val slug = varchar("slug", 255).uniqueIndex()
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)
    val updatedAt = datetime("updated_at").defaultExpression(CurrentDateTime)
}

fun ResultRow.toTag() = Tag(
    id = this[TagsTable.id].value,
    name = this[TagsTable.name],
    slug = this[TagsTable.slug],
    createdAt = this[TagsTable.createdAt],
    updatedAt = this[TagsTable.updatedAt],
)

fun Iterable<ResultRow>.toTags() = this.map { it.toTag() }
fun Iterable<ResultRow>.toTag() = this.firstOrNull()?.toTag()