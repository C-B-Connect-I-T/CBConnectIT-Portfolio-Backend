package com.cbconnectit.data.database.tables

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ReferenceOption
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