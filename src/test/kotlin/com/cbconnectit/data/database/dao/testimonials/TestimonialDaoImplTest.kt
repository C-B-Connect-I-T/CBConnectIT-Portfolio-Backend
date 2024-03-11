package com.cbconnectit.data.database.dao.testimonials

import com.cbconnectit.data.database.dao.BaseDaoTest
import com.cbconnectit.data.database.dao.TestimonialDaoImpl
import com.cbconnectit.data.database.dao.testimonials.TestimonialInstrumentation.givenAValidInsertTestimonialBody
import com.cbconnectit.data.database.dao.testimonials.TestimonialInstrumentation.givenAValidUpdateTestimonialBody
import com.cbconnectit.data.database.tables.CompaniesTable
import com.cbconnectit.data.database.tables.JobPositionsTable
import com.cbconnectit.data.database.tables.TestimonialsTable
import com.cbconnectit.domain.models.company.Company
import com.cbconnectit.domain.models.jobPosition.JobPosition
import com.cbconnectit.domain.models.testimonial.Testimonial
import kotlinx.coroutines.delay
import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.insert
import org.junit.jupiter.api.Test
import java.util.*
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class TestimonialDaoImplTest : BaseDaoTest() {

    private val dao = TestimonialDaoImpl()

    // <editor-fold desc="Get all Testimonials">
    @Test
    fun `getTestimonials but none exists, return empty list`() {
        withTables(
            CompaniesTable,
            JobPositionsTable,
            TestimonialsTable
        ) {
            val list = dao.getTestimonials()
            assertThat(list).isEmpty()
        }
    }

    @Test
    fun `getTestimonials return the list`() {
        baseTest {
            val list = dao.getTestimonials()
            assertThat(list).hasSize(4)
        }
    }
    // </editor-fold>

    // <editor-fold desc="Get specific Testimonial by id">
    @Test
    fun `getTestimonial where item exists, return correct Testimonial`() {
        baseTest {
            val testimonial = dao.getTestimonialById(UUID.fromString("00000000-0000-0000-0000-000000000001"))

            assertThat(testimonial).matches {
                it?.review == "First Testimonial"
            }
        }
    }

    @Test
    fun `getTestimonial where item does not exists, return 'null'`() {
        baseTest {
            val testimonial = dao.getTestimonialById(UUID.randomUUID())

            assertNull(testimonial)
        }
    }
    // </editor-fold>

    // <editor-fold desc="Create new Testimonial">
    @Test
    fun `insertTestimonial where information is correct, database is storing Testimonial and returning correct content`() {
        baseTest {
            val validTestimonial = givenAValidInsertTestimonialBody()
            val testimonial = dao.insertTestimonial(validTestimonial)

            assertThat(testimonial).matches {
                it?.review == validTestimonial.review &&
                        it.createdAt == it.updatedAt
            }
        }
    }
    // </editor-fold>

    // <editor-fold desc="Update Testimonial">
    @Test
    fun `updateTestimonial where information is correct, database is storing information and returning the correct content`() {
        baseTest {
            // adding a delay so there is a clear difference between `updatedAt` and `createdAt`
            delay(1000)

            val validUpdateTestimonial = givenAValidUpdateTestimonialBody()
            val testimonial = dao.updateTestimonial(UUID.fromString("00000000-0000-0000-0000-000000000001"), validUpdateTestimonial)

            assertThat(testimonial).matches {
                it?.review == validUpdateTestimonial.review &&
                        it.createdAt != it.updatedAt
            }
        }
    }

    @Test
    fun `updateTestimonial where information is correct but Testimonial with id does not exist, database does nothing and returns 'null'`() {
        baseTest {
            val validTestimonial = givenAValidUpdateTestimonialBody()
            val testimonial = dao.updateTestimonial(UUID.randomUUID(), validTestimonial)

            assertNull(testimonial)
        }
    }
    // </editor-fold>

    // <editor-fold desc="Delete Testimonial">
    @Test
    fun `deleteTestimonial for id that exists, return true`() {
        baseTest {
            val deleted = dao.deleteTestimonial(UUID.fromString("00000000-0000-0000-0000-000000000001"))
            assertTrue(deleted)
        }
    }

    @Test
    fun `deleteTestimonial for id that does not exist, return false`() {
        baseTest {
            val deleted = dao.deleteTestimonial(UUID.randomUUID())
            assertFalse(deleted)
        }
    }
    // </editor-fold>

    private fun baseTest(
        test: suspend Transaction.() -> Unit
    ) {
        withTables(
            CompaniesTable,
            JobPositionsTable,
            TestimonialsTable
        ) {
            listOf(
                Company(UUID.fromString("00000000-0000-0000-0000-000000000001"), name = "First company"),
                Company(UUID.fromString("00000000-0000-0000-0000-000000000002"), name = "Second company"),
            ).forEach { data ->
                CompaniesTable.insert {
                    it[id] = data.id
                    it[name] = data.name
                }
            }

            listOf(
                JobPosition(UUID.fromString("00000000-0000-0000-0000-000000000001"), name = "First jobPosition"),
                JobPosition(UUID.fromString("00000000-0000-0000-0000-000000000002"), name = "Second jobPosition"),
            ).forEach { data ->
                JobPositionsTable.insert {
                    it[id] = data.id
                    it[name] = data.name
                }
            }

            listOf(
                Testimonial(
                    id = UUID.fromString("00000000-0000-0000-0000-000000000001"),
                    review = "First Testimonial",
                    company = Company(UUID.fromString("00000000-0000-0000-0000-000000000001")),
                    jobPosition = JobPosition(UUID.fromString("00000000-0000-0000-0000-000000000002"))
                ),
                Testimonial(
                    id = UUID.fromString("00000000-0000-0000-0000-000000000002"),
                    review = "Second Testimonial",
                    company = Company(UUID.fromString("00000000-0000-0000-0000-000000000002")),
                    jobPosition = JobPosition(UUID.fromString("00000000-0000-0000-0000-000000000001"))
                ),
                Testimonial(
                    id = UUID.fromString("00000000-0000-0000-0000-000000000003"),
                    review = "Third Testimonial",
                    company = Company(UUID.fromString("00000000-0000-0000-0000-000000000001")),
                    jobPosition = JobPosition(UUID.fromString("00000000-0000-0000-0000-000000000002"))
                ),
                Testimonial(
                    id = UUID.fromString("00000000-0000-0000-0000-000000000004"),
                    review = "Fourth Testimonial",
                    company = Company(UUID.fromString("00000000-0000-0000-0000-000000000002")),
                    jobPosition = JobPosition(UUID.fromString("00000000-0000-0000-0000-000000000001"))
                )
            ).forEach { data ->
                TestimonialsTable.insert {
                    it[id] = data.id
                    it[review] = data.review
                    it[companyId] = data.company.id
                    it[jobPositionId] = data.jobPosition.id
                    it[imageUrl] = ""
                    it[fullName] = ""
                }
            }

            test()
        }
    }
}