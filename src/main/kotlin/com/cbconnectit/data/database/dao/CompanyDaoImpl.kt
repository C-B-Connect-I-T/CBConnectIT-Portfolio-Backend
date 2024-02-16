package com.cbconnectit.data.database.dao

import com.cbconnectit.data.database.tables.CompaniesTable
import com.cbconnectit.data.database.tables.LinksTable
import com.cbconnectit.data.database.tables.toCompany
import com.cbconnectit.data.database.tables.toCompanies
import com.cbconnectit.data.dto.requests.company.InsertNewCompany
import com.cbconnectit.data.dto.requests.company.UpdateCompany
import com.cbconnectit.domain.interfaces.ICompanyDao
import com.cbconnectit.domain.models.company.Company
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.javatime.CurrentDateTime
import java.util.*

class CompanyDaoImpl : ICompanyDao {
    override fun getCompanyById(id: UUID): Company? =
        CompaniesTable.select { CompaniesTable.id eq id }.toCompany()

    override fun getCompanies(): List<Company> =
        CompaniesTable.selectAll().toCompanies()

    override fun insertCompany(insertNewCompany: InsertNewCompany): Company? {
        val id = CompaniesTable.insertAndGetId {
            it[name] = insertNewCompany.name
        }.value

        return getCompanyById(id)
    }

    override fun updateCompany(id: UUID, updateCompany: UpdateCompany): Company? {
        CompaniesTable.update({ CompaniesTable.id eq id }) {
            it[name] = updateCompany.name

            it[LinksTable.updatedAt] = CurrentDateTime
        }

        return getCompanyById(id)
    }

    override fun deleteCompany(id: UUID): Boolean = CompaniesTable.deleteWhere { CompaniesTable.id eq id } > 0

    override fun companyUnique(name: String): Boolean =
        CompaniesTable.select { CompaniesTable.name eq name }.empty()

    override fun getListOfExistingCompanyIds(companyIds: List<UUID>): List<UUID> =
        CompaniesTable.select { CompaniesTable.id inList companyIds }.map { it[CompaniesTable.id].value }
}