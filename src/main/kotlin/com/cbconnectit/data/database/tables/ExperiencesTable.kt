package com.cbconnectit.data.database.tables

import com.cbconnectit.domain.models.experience.Experience
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.javatime.CurrentDateTime
import org.jetbrains.exposed.sql.javatime.datetime

object ExperiencesTable : UUIDTable() {
    val jobPositionId = reference("job_position_id", JobPositionsTable, ReferenceOption.CASCADE)
    val shortDescription = text("short_description")
    val description = mediumText("description")
    val companyId = reference("company_id", CompaniesTable, ReferenceOption.CASCADE)
    val from = datetime("from")
    val to = datetime("to")
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)
    val updatedAt = datetime("updated_at").defaultExpression(CurrentDateTime)
}

fun ResultRow.toExperience() = Experience(
    id = this[ExperiencesTable.id].value,
    shortDescription = this[ExperiencesTable.shortDescription],
    description = this[ExperiencesTable.description],
    from = this[ExperiencesTable.from],
    to = this[ExperiencesTable.to],
    jobPosition = this.toJobPosition(),
    company = this.toCompany(),
    createdAt = this[ExperiencesTable.createdAt],
    updatedAt = this[ExperiencesTable.updatedAt],
)

fun Iterable<ResultRow>.toExperiences() = this.map { it.toExperience() }
fun Iterable<ResultRow>.toExperience() = this.firstOrNull()?.toExperience()