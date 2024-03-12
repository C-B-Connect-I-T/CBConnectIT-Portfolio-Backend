package com.cbconnectit.data.database.tables

import com.cbconnectit.domain.models.service.Service
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.javatime.CurrentDateTime
import org.jetbrains.exposed.sql.javatime.datetime

object ServicesTable : UUIDTable() {
    val imageUrl = varchar("image_url", 255)
    val title = varchar("name", 255).uniqueIndex()
    val shortDescription = varchar("short_description", 1000).nullable().default(null)
    val description = text("description")
    val bannerDescription = text("banner_description").nullable().default(null)
    val extraInfo = text("extra_info").nullable().default(null)
    val tagId = optReference("tag_id", TagsTable, ReferenceOption.NO_ACTION).default(null)
    val parentServiceId = optReference("parent_service_id", ServicesTable, ReferenceOption.CASCADE).default(null)
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)
    val updatedAt = datetime("updated_at").defaultExpression(CurrentDateTime)
}

fun ResultRow.toService() = Service(
    id = this[ServicesTable.id].value,
    imageUrl = this[ServicesTable.imageUrl],
    title = this[ServicesTable.title],
    shortDescription = this[ServicesTable.shortDescription],
    description = this[ServicesTable.description],
    bannerDescription = this[ServicesTable.bannerDescription],
    extraInfo = this[ServicesTable.extraInfo],
    tag = this[ServicesTable.tagId]?.value?.let { this.toTag() },
    createdAt = this[ServicesTable.createdAt],
    updatedAt = this[ServicesTable.updatedAt]
)

fun Iterable<ResultRow>.toServices() = this.map { it.toService() }
fun Iterable<ResultRow>.toService() = this.firstOrNull()?.toService()
