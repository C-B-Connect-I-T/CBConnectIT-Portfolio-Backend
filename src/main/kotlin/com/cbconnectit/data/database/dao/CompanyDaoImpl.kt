package com.cbconnectit.data.database.dao

import com.cbconnectit.data.database.tables.CompaniesLinksPivotTable
import com.cbconnectit.data.database.tables.CompaniesTable
import com.cbconnectit.data.database.tables.LinksTable
import com.cbconnectit.data.database.tables.parseLinks
import com.cbconnectit.data.database.tables.toCompany
import com.cbconnectit.data.dto.requests.company.InsertNewCompany
import com.cbconnectit.data.dto.requests.company.UpdateCompany
import com.cbconnectit.domain.interfaces.ICompanyDao
import com.cbconnectit.domain.models.company.Company
import com.cbconnectit.domain.models.link.Link
import org.jetbrains.exposed.sql.Query
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.notInList
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.javatime.CurrentDateTime
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import java.util.*

class CompanyDaoImpl : ICompanyDao {
    override fun getCompanyById(id: UUID): Company? {
        val companyWithRelations = (CompaniesTable leftJoin CompaniesLinksPivotTable leftJoin LinksTable)

        val results = companyWithRelations.selectAll().where { CompaniesTable.id eq id }
        val links = parseLinks(results)

        return results
            .distinctBy { it[CompaniesTable.id].value }
            .map { row ->
                row.toCompany().copy(
                    links = links[id]?.distinctBy { it.id } ?: emptyList()
                )
            }
            .firstOrNull()
    }

    override fun getCompanies(): List<Company> {
        val companyWithRelations = (CompaniesTable leftJoin CompaniesLinksPivotTable leftJoin LinksTable)

        val results = companyWithRelations.selectAll()
        val links = parseLinks(results)

        return results
            .distinctBy { it[CompaniesTable.id].value }
            .map { row ->
                val id = row[CompaniesTable.id].value
                row.toCompany().copy(
                    links = links[id]?.distinctBy { it.id } ?: emptyList()
                )
            }
    }

    private fun parseLinks(results: Query): MutableMap<UUID, List<Link>> {
        return parseLinks(results) {
            it[CompaniesTable.id].value
        }
    }

    override fun insertCompany(insertNewCompany: InsertNewCompany): Company? {
        val id = CompaniesTable.insertAndGetId {
            it[name] = insertNewCompany.name
        }.value

        insertNewCompany.links?.forEach { linkId ->
            CompaniesLinksPivotTable.insert {
                it[this.linkId] = UUID.fromString(linkId)
                it[companyId] = id
            }
        }

        return getCompanyById(id)
    }

    override fun updateCompany(id: UUID, updateCompany: UpdateCompany): Company? {
        CompaniesTable.update({ CompaniesTable.id eq id }) {
            it[name] = updateCompany.name

            it[LinksTable.updatedAt] = CurrentDateTime
        }

        CompaniesLinksPivotTable.deleteWhere {
            companyId eq id and (linkId notInList (updateCompany.links ?: emptyList()).map { linkId -> UUID.fromString(linkId) })
        }

        updateCompany.links?.forEach { linkId ->
            CompaniesLinksPivotTable.insert {
                it[this.linkId] = UUID.fromString(linkId)
                it[companyId] = id
            }
        }

        return getCompanyById(id)
    }

    override fun deleteCompany(id: UUID): Boolean {
        val result = CompaniesTable.deleteWhere { CompaniesTable.id eq id } > 0
        val result2 = CompaniesLinksPivotTable.deleteWhere { companyId eq id } > 0

        return result && result2
    }

    override fun companyUnique(name: String): Boolean =
        CompaniesTable.selectAll().where { CompaniesTable.name eq name }.empty()

    override fun getListOfExistingCompanyIds(companyIds: List<UUID>): List<UUID> =
        CompaniesTable.selectAll().where { CompaniesTable.id inList companyIds }.map { it[CompaniesTable.id].value }
}
