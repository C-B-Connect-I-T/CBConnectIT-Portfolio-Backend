package com.cbconnectit.data.database.tables

import com.cbconnectit.domain.models.project.Project
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.javatime.CurrentDateTime
import org.jetbrains.exposed.sql.javatime.datetime

object ProjectsTable : UUIDTable() {
    val bannerImage = varchar("banner_image", 255).nullable().default(null)
    val image = varchar("image", 255).nullable().default(null)
    val title = varchar("title", 255)
    val shortDescription = varchar("short_description", 1000)
    val description = text("description")
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)
    val updatedAt = datetime("updated_at").defaultExpression(CurrentDateTime)
}

fun ResultRow.toProject() = Project(
    id = this[ProjectsTable.id].value,
    bannerImage = this[ProjectsTable.bannerImage],
    image = this[ProjectsTable.image],
    title = this[ProjectsTable.title],
    shortDescription = this[ProjectsTable.shortDescription],
    description = this[ProjectsTable.description],
    createdAt = this[ProjectsTable.createdAt],
    updatedAt = this[ProjectsTable.updatedAt]
)

fun Iterable<ResultRow>.toProjects() = this.map { it.toProject() }
fun Iterable<ResultRow>.toProject() = this.firstOrNull()?.toProject()
