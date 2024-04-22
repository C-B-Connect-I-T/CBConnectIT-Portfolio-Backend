package com.cbconnectit.data.database.tables

import com.cbconnectit.data.database.tables.Constants.normalTextSize
import com.cbconnectit.domain.models.company.Company
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.javatime.CurrentDateTime
import org.jetbrains.exposed.sql.javatime.datetime

object CompaniesTable: UUIDTable() {
    val name = varchar("name", normalTextSize)
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)
    val updatedAt = datetime("updated_at").defaultExpression(CurrentDateTime)
}

fun ResultRow.toCompany() = Company(
    id = this[CompaniesTable.id].value,
    name = this[CompaniesTable.name],
    createdAt = this[CompaniesTable.createdAt],
    updatedAt = this[CompaniesTable.updatedAt]
)

fun Iterable<ResultRow>.toCompanies() = this.map { it.toCompany() }
fun Iterable<ResultRow>.toCompany() = this.firstOrNull()?.toCompany()
