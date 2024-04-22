package com.cbconnectit.data.database.tables

import com.cbconnectit.data.database.tables.Constants.normalTextSize
import com.cbconnectit.domain.models.jobPosition.JobPosition
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.javatime.CurrentDateTime
import org.jetbrains.exposed.sql.javatime.datetime

object JobPositionsTable : UUIDTable() {
    val name = varchar("name", normalTextSize)
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)
    val updatedAt = datetime("updated_at").defaultExpression(CurrentDateTime)
}

fun ResultRow.toJobPosition() = JobPosition(
    id = this[JobPositionsTable.id].value,
    name = this[JobPositionsTable.name],
    createdAt = this[JobPositionsTable.createdAt],
    updatedAt = this[JobPositionsTable.updatedAt]
)

fun Iterable<ResultRow>.toJobPositions() = this.map { it.toJobPosition() }
fun Iterable<ResultRow>.toJobPosition() = this.firstOrNull()?.toJobPosition()
