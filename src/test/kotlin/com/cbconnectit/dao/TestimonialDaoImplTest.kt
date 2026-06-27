package com.cbconnectit.dao

import com.cbconnectit.data.database.dao.TestimonialDaoImpl
import com.cbconnectit.data.database.tables.CompaniesTable
import com.cbconnectit.data.database.tables.JobPositionsTable
import com.cbconnectit.data.database.tables.TestimonialsTable
import com.cbconnectit.instrumentation.CompanyInstrumentation
import com.cbconnectit.instrumentation.JobPositionInstrumentation
import com.cbconnectit.instrumentation.TestimonialInstrumentation
import com.cbconnectit.instrumentation.TestimonialInstrumentation.givenAValidInsertTestimonial
import com.cbconnectit.instrumentation.TestimonialInstrumentation.givenAValidUpdateTestimonial
import kotlinx.coroutines.delay
import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.exposed.sql.insert
import org.junit.jupiter.api.Test
import java.util.*
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class TestimonialDaoImplTest : BaseDaoTest() {

    private val dao = TestimonialDaoImpl()

    override suspend fun seedData() {
        // Seed companies first (foreign key dependency)
        CompanyInstrumentation.givenCompanyList().take(2).forEachIndexed { index, data ->
            CompaniesTable.insert {
                it[id] = UUID.fromString("00000000-0000-0000-0000-00000000000${index + 1}")
                it[name] = data.name
                it[createdAt] = data.createdAt
                it[updatedAt] = data.updatedAt
            }
        }

        // Seed job positions
        JobPositionInstrumentation.givenJobPositionList().take(2).forEachIndexed { index, data ->
            JobPositionsTable.insert {
                it[id] = UUID.fromString("00000000-0000-0000-0000-00000000000${index + 1}")
                it[name] = data.name
                it[createdAt] = data.createdAt
                it[updatedAt] = data.updatedAt
            }
        }

        // Seed testimonials with specific company/job position combinations
        // Note: fifth testimonial has null company
        val testimonials = TestimonialInstrumentation.givenTestimonialList()
        listOf(
            Triple(UUID.fromString("00000000-0000-0000-0000-000000000001"), UUID.fromString("00000000-0000-0000-0000-000000000001"), UUID.fromString("00000000-0000-0000-0000-000000000002")),
            Triple(UUID.fromString("00000000-0000-0000-0000-000000000002"), UUID.fromString("00000000-0000-0000-0000-000000000002"), UUID.fromString("00000000-0000-0000-0000-000000000001")),
            Triple(UUID.fromString("00000000-0000-0000-0000-000000000003"), UUID.fromString("00000000-0000-0000-0000-000000000001"), UUID.fromString("00000000-0000-0000-0000-000000000002")),
            Triple(UUID.fromString("00000000-0000-0000-0000-000000000004"), UUID.fromString("00000000-0000-0000-0000-000000000002"), UUID.fromString("00000000-0000-0000-0000-000000000001")),
        ).forEachIndexed { index, (testimonialId, compId, jobPosId) ->
            TestimonialsTable.insert {
                it[id] = testimonialId
                it[review] = testimonials[index].review
                it[companyId] = compId
                it[jobPositionId] = jobPosId
                it[fullName] = ""
                it[createdAt] = testimonials[index].createdAt
                it[updatedAt] = testimonials[index].updatedAt
            }
        }

        // Add fifth testimonial with null company
        TestimonialsTable.insert {
            it[id] = UUID.fromString("00000000-0000-0000-0000-000000000005")
            it[review] = "Fifth Testimonial"
            it[companyId] = null
            it[jobPositionId] = UUID.fromString("00000000-0000-0000-0000-000000000001")
            it[fullName] = ""
            it[createdAt] = testimonials[0].createdAt
            it[updatedAt] = testimonials[0].updatedAt
        }
    }

    // <editor-fold desc="Get all Testimonials">
    @Test
    fun `getTestimonials but none exists, return empty list`() = runTest(shouldSeedData = false) {
        val list = dao.readAll()
        assertThat(list).isEmpty()
    }

    @Test
    fun `getTestimonials return the list`() = runTest {
        val list = dao.readAll()
        assertThat(list).hasSize(5)
    }
    // </editor-fold>

    // <editor-fold desc="Get specific Testimonial by id">
    @Test
    fun `getTestimonial where item exists, return correct Testimonial`() = runTest {
        val testimonial = dao.readById(UUID.fromString("00000000-0000-0000-0000-000000000001"))

        assertThat(testimonial).matches {
            it?.review == "First Testimonial"
        }
    }

    @Test
    fun `getTestimonial where item does not exists, return 'null'`() = runTest {
        val testimonial = dao.readById(UUID.randomUUID())

        assertNull(testimonial)
    }
    // </editor-fold>

    // <editor-fold desc="Create new Testimonial">
    @Test
    fun `insertTestimonial where information is correct, database is storing Testimonial and returning correct content`() = runTest {
        val validTestimonial = givenAValidInsertTestimonial()
        val testimonialId = dao.create(UUID.randomUUID(), validTestimonial)
        val testimonial = dao.readById(testimonialId)

        assertThat(testimonial).matches {
            it?.review == validTestimonial.review &&
                    it.createdAt == it.updatedAt
        }
    }
    // </editor-fold>

    // <editor-fold desc="Update Testimonial">
    @Test
    fun `updateTestimonial where information is correct, database is storing information and returning the correct content`() = runTest {
        val validUpdateTestimonial = givenAValidUpdateTestimonial()
        val id = UUID.fromString("00000000-0000-0000-0000-000000000001")

        delay(1000) // to make sure timestamps differ
        val updated = dao.updateById(id, validUpdateTestimonial)

        assertTrue(updated)

        val testimonial = dao.readById(id)
        assertThat(testimonial?.fullName).isEqualTo(validUpdateTestimonial.fullName)
        assertThat(testimonial?.updatedAt).isAfter(testimonial?.createdAt)
    }

    @Test
    fun `updateTestimonial where information is correct but Testimonial with id does not exist, database does nothing and returns 'null'`() = runTest {
        val validTestimonial = givenAValidUpdateTestimonial()
        val updated = dao.updateById(UUID.randomUUID(), validTestimonial)

        assertFalse(updated)
    }
    // </editor-fold>

    // <editor-fold desc="Delete Testimonial">
    @Test
    fun `deleteTestimonial for id that exists, return true`() = runTest {
        val deleted = dao.deleteById(UUID.fromString("00000000-0000-0000-0000-000000000001"))
        assertTrue(deleted)
    }

    @Test
    fun `deleteTestimonial for id that does not exist, return false`() = runTest {
        val deleted = dao.deleteById(UUID.randomUUID())
        assertFalse(deleted)
    }
    // </editor-fold>
}
