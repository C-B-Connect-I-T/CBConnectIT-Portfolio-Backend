package com.cbconnectit.data.database.tables

import com.cbconnectit.domain.models.project.Project
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.javatime.CurrentDateTime
import org.jetbrains.exposed.sql.javatime.datetime

object ProjectsTable : UUIDTable() {
    val bannerImageUrl = varchar("banner_image_url", 255).nullable().default(null)
    val imageUrl = varchar("image_url", 255).nullable().default(null)
    val title = varchar("title", 255)
    val shortDescription = varchar("short_description", 1000)
    val description = text("description")
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)
    val updatedAt = datetime("updated_at").defaultExpression(CurrentDateTime)
}

fun ResultRow.toProject() = Project(
    id = this[ProjectsTable.id].value,
    bannerImageUrl = this[ProjectsTable.bannerImageUrl],
    imageUrl = this[ProjectsTable.imageUrl],
    title = this[ProjectsTable.title],
    shortDescription = this[ProjectsTable.shortDescription],
    description = this[ProjectsTable.description],
    createdAt = this[ProjectsTable.createdAt],
    updatedAt = this[ProjectsTable.updatedAt]
)

fun Iterable<ResultRow>.toProjects() = this.map { it.toProject() }
fun Iterable<ResultRow>.toProject() = this.firstOrNull()?.toProject()
