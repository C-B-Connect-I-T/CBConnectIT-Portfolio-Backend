package com.cbconnectit.data.database.tables

import com.cbconnectit.domain.models.tag.Tag
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.Query
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.javatime.CurrentDateTime
import org.jetbrains.exposed.sql.javatime.datetime
import java.util.*

object TagsTable: UUIDTable() {
    val name = varchar("name", 255).uniqueIndex()
    val slug = varchar("slug", 255).uniqueIndex()
//    val parentTagId = reference("parent_tag_id", TagsTable, ReferenceOption.CASCADE)
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

fun parseTags(results: Query, getParentId: (ResultRow) -> UUID): MutableMap<UUID, List<Tag>> {
    val newMap = results
        .fold(mutableMapOf<UUID, List<Tag>>()) { map, resultRow ->
            val parentId = getParentId(resultRow)

            val link = if (resultRow.getOrNull(TagsTable.id) != null) {
                resultRow.toTag()
            } else null

            val current = map.getOrDefault(parentId, emptyList())
            map[parentId] = current.toMutableList() + listOfNotNull(link)
            map
        }

    return newMap
}
