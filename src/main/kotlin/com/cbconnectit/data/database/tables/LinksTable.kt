package com.cbconnectit.data.database.tables

import com.cbconnectit.data.database.tables.Constants.normalTextSize
import com.cbconnectit.domain.models.link.Link
import com.cbconnectit.domain.models.link.LinkType
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.Query
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.javatime.CurrentDateTime
import org.jetbrains.exposed.sql.javatime.datetime
import java.util.*

object LinksTable: UUIDTable() {
    val url = varchar("url", normalTextSize)
    val type = enumeration<LinkType>("type").default(LinkType.Unknown)
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)
    val updatedAt = datetime("updated_at").defaultExpression(CurrentDateTime)
}

fun ResultRow.toLink() = Link(
    id = this[LinksTable.id].value,
    url = this[LinksTable.url],
    type = this[LinksTable.type],
    createdAt = this[LinksTable.createdAt],
    updatedAt = this[LinksTable.updatedAt],
)

fun Iterable<ResultRow>.toLinks() = this.map { it.toLink() }
fun Iterable<ResultRow>.toLink() = this.firstOrNull()?.toLink()

fun parseLinks(results: Query, getParentId: (ResultRow) -> UUID?): MutableMap<UUID, List<Link>> {
    val newMap = results
        .distinctBy { it.getOrNull(LinksTable.id)?.value }
        .fold(mutableMapOf<UUID, List<Link>>()) { map, resultRow ->
            val parentId = getParentId(resultRow) ?: return@fold map

            val link = resultRow.getOrNull(LinksTable.id)?.let { resultRow.toLink() }

            val current = map.getOrDefault(parentId, emptyList())
            map[parentId] = current.toMutableList() + listOfNotNull(link)
            map
        }

    return newMap
}
