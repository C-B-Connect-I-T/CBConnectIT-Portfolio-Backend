package com.cbconnectit.data.database.dao

import com.cbconnectit.data.database.tables.CompaniesLinksPivotTable
import com.cbconnectit.data.database.tables.CompaniesTable
import com.cbconnectit.data.database.tables.JobPositionsTable
import com.cbconnectit.data.database.tables.LinksTable
import com.cbconnectit.data.database.tables.TestimonialsTable
import com.cbconnectit.data.database.tables.toLink
import com.cbconnectit.data.database.tables.toTestimonial
import com.cbconnectit.data.dto.requests.testimonial.InsertTestimonial
import com.cbconnectit.data.dto.requests.testimonial.UpdateTestimonial
import com.cbconnectit.domain.interfaces.ITestimonialDao
import com.cbconnectit.domain.models.link.Link
import com.cbconnectit.domain.models.mediafile.OwnerType
import com.cbconnectit.domain.models.testimonial.Testimonial
import com.cbconnectit.utils.leftJoinMediaFiles
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import java.time.LocalDateTime
import java.util.*

class TestimonialDaoImpl : ITestimonialDao {

    private fun getTestimonialQuery() = (TestimonialsTable leftJoin JobPositionsTable leftJoin CompaniesTable leftJoin CompaniesLinksPivotTable leftJoin LinksTable)
        .leftJoinMediaFiles(
            onColumn = { TestimonialsTable.id },
            ownerType = OwnerType.TESTIMONIAL
        )
        .selectAll()

    override fun readById(id: UUID): Testimonial? =
        getTestimonialQuery()
            .where { TestimonialsTable.id eq id }
            .toList()
            .let(::parseTestimonialData)
            .firstOrNull()

    private fun parseTestimonialData(resultRows: List<ResultRow>): List<Testimonial> {
        val companyIds = resultRows.mapNotNull { it[TestimonialsTable.companyId]?.value }.distinct()

        val links = fetchLinksForCompanies(companyIds)

        return resultRows
            .map { result ->
                val id = result[TestimonialsTable.companyId]?.value
                val companyLinks = links[id].orEmpty()
                val testimonial = result.toTestimonial()
                testimonial.copy(company = testimonial.company?.copy(links = companyLinks))
            }
    }

    private fun fetchLinksForCompanies(companyIds: List<UUID>): Map<UUID, List<Link>> = (CompaniesLinksPivotTable innerJoin LinksTable)
        .selectAll()
        .where { CompaniesLinksPivotTable.companyId inList companyIds }
        .groupBy {
            it[CompaniesLinksPivotTable.companyId].value to it[LinksTable.id].value
        }
        .map { (ids, rows) -> ids.first to rows.first().toLink() }
        .groupBy({ it.first }, { it.second })

    override fun readAll(): List<Testimonial> =
        getTestimonialQuery()
            .toList()
            .let(::parseTestimonialData)

    override fun create(id: UUID, insertTestimonial: InsertTestimonial): UUID =
        TestimonialsTable.insertAndGetId {
            it[TestimonialsTable.id] = EntityID(id, TestimonialsTable)
            it[fullName] = insertTestimonial.fullName
            it[review] = insertTestimonial.review
            it[avatarAltText] = insertTestimonial.avatarAltText ?: ""
            it[jobPositionId] = UUID.fromString(insertTestimonial.jobPositionId)
            it[companyId] = UUID.fromString(insertTestimonial.companyId)
        }.value

    override fun updateById(id: UUID, updateTestimonial: UpdateTestimonial): Boolean =
        TestimonialsTable.update({ TestimonialsTable.id eq id }) {
            it[fullName] = updateTestimonial.fullName
            it[review] = updateTestimonial.review
            it[jobPositionId] = UUID.fromString(updateTestimonial.jobPositionId)
            it[companyId] = UUID.fromString(updateTestimonial.companyId)
            updateTestimonial.avatarAltText?.let { value -> it[avatarAltText] = value }

            it[updatedAt] = LocalDateTime.now()
        } > 0

    override fun deleteById(id: UUID): Boolean =
        TestimonialsTable.deleteWhere {
            with(it) {
                TestimonialsTable.id eq id
            }
        } > 0
}
