package com.cbconnectit.data.database.dao

import com.cbconnectit.data.database.tables.CompaniesLinksPivotTable
import com.cbconnectit.data.database.tables.CompaniesTable
import com.cbconnectit.data.database.tables.JobPositionsTable
import com.cbconnectit.data.database.tables.LinksTable
import com.cbconnectit.data.database.tables.TestimonialsTable
import com.cbconnectit.data.database.tables.parseLinks
import com.cbconnectit.data.database.tables.toTestimonial
import com.cbconnectit.data.dto.requests.testimonial.InsertNewTestimonial
import com.cbconnectit.data.dto.requests.testimonial.UpdateTestimonial
import com.cbconnectit.domain.interfaces.ITestimonialDao
import com.cbconnectit.domain.models.link.Link
import com.cbconnectit.domain.models.testimonial.Testimonial
import org.jetbrains.exposed.sql.Query
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import java.time.LocalDateTime
import java.util.*

class TestimonialDaoImpl : ITestimonialDao {

    override fun getTestimonialById(id: UUID): Testimonial? {

        val testimonialWithRelations = TestimonialsTable leftJoin JobPositionsTable leftJoin CompaniesTable leftJoin CompaniesLinksPivotTable leftJoin LinksTable

        val results = testimonialWithRelations.selectAll().where { TestimonialsTable.id eq id }
        val links = parseLinks(results)

        return results
            .distinctBy { it[TestimonialsTable.companyId]?.value }
            .map { row ->
                val temp = row.toTestimonial()
                temp.copy(
                    company = temp.company?.copy(
                        links = links[id]?.distinctBy { it.id } ?: emptyList()
                    )
                )
            }
            .firstOrNull()
    }

    override fun getTestimonials(): List<Testimonial> {
        val testimonialWithRelations = TestimonialsTable leftJoin JobPositionsTable leftJoin CompaniesTable leftJoin CompaniesLinksPivotTable leftJoin LinksTable

        val results = testimonialWithRelations.selectAll()
        val links = parseLinks(results)

        return results
            // TODO: big issue!! Unit Test fails with this, but removing this results in duplicate data when fetching
            .distinctBy { it[TestimonialsTable.id].value }
            .map { row ->
                val id = row[TestimonialsTable.companyId]?.value
                val temp = row.toTestimonial()
                temp.copy(
                    company = temp.company?.copy(
                        links = links[id]?.distinctBy { it.id } ?: emptyList()
                    )
                )
            }
    }

    private fun parseLinks(results: Query): MutableMap<UUID, List<Link>> {
        return parseLinks(results) {
            it[TestimonialsTable.companyId]?.value
        }
    }

    override fun insertTestimonial(insertNewTestimonial: InsertNewTestimonial): Testimonial? {
        val id = TestimonialsTable.insertAndGetId {
            it[imageUrl] = insertNewTestimonial.imageUrl
            it[fullName] = insertNewTestimonial.fullName
            it[review] = insertNewTestimonial.review
            it[jobPositionId] = UUID.fromString(insertNewTestimonial.jobPositionId)
            it[companyId] = UUID.fromString(insertNewTestimonial.companyId)
        }.value

        return getTestimonialById(id)
    }

    override fun updateTestimonial(id: UUID, updateTestimonial: UpdateTestimonial): Testimonial? {
        TestimonialsTable.update({ TestimonialsTable.id eq id }) {
            it[imageUrl] = updateTestimonial.imageUrl
            it[fullName] = updateTestimonial.fullName
            it[review] = updateTestimonial.review
            it[jobPositionId] = UUID.fromString(updateTestimonial.jobPositionId)
            it[companyId] = UUID.fromString(updateTestimonial.companyId)

            it[updatedAt] = LocalDateTime.now()
        }

        return getTestimonialById(id)
    }

    override fun deleteTestimonial(id: UUID): Boolean =
        TestimonialsTable.deleteWhere { TestimonialsTable.id eq id } > 0
}
