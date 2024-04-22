package com.cbconnectit.controllers.testimonials

import com.cbconnectit.controllers.BaseControllerTest
import com.cbconnectit.controllers.testimonials.TestimonialInstrumentation.givenATestimonial
import com.cbconnectit.controllers.testimonials.TestimonialInstrumentation.givenAValidInsertTestimonial
import com.cbconnectit.controllers.testimonials.TestimonialInstrumentation.givenAValidUpdateTestimonial
import com.cbconnectit.controllers.testimonials.TestimonialInstrumentation.givenAnInvalidInsertTestimonial
import com.cbconnectit.controllers.testimonials.TestimonialInstrumentation.givenAnInvalidUpdateTestimonial
import com.cbconnectit.data.dto.requests.testimonial.TestimonialDto
import com.cbconnectit.domain.interfaces.ICompanyDao
import com.cbconnectit.domain.interfaces.IJobPositionDao
import com.cbconnectit.domain.interfaces.ITestimonialDao
import com.cbconnectit.modules.testimonials.TestimonialController
import com.cbconnectit.modules.testimonials.TestimonialControllerImpl
import com.cbconnectit.statuspages.ErrorFailedCreate
import com.cbconnectit.statuspages.ErrorFailedDelete
import com.cbconnectit.statuspages.ErrorFailedUpdate
import com.cbconnectit.statuspages.ErrorInvalidParameters
import com.cbconnectit.statuspages.ErrorNotFound
import com.cbconnectit.statuspages.ErrorUnknownCompanyIdsForCreateTestimonial
import com.cbconnectit.statuspages.ErrorUnknownCompanyIdsForUpdateTestimonial
import com.cbconnectit.statuspages.ErrorUnknownJobPositionIdsForCreateTestimonial
import com.cbconnectit.statuspages.ErrorUnknownJobPositionIdsForUpdateTestimonial
import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.koin.dsl.module
import java.util.*
import kotlin.test.assertNotNull

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestimonialControllerTest : BaseControllerTest() {

    private val testimonialDao: ITestimonialDao = mockk()
    private val companyDao: ICompanyDao = mockk()
    private val jobPositionDao: IJobPositionDao = mockk()
    private val controller: TestimonialController by lazy { TestimonialControllerImpl() }

    init {
        startInjection(
            module {
                single { testimonialDao }
                single { companyDao }
                single { jobPositionDao }
            }
        )
    }

    @BeforeEach
    override fun before() {
        super.before()
        clearMocks(testimonialDao, companyDao, jobPositionDao)
    }

    // <editor-fold desc="Get all testimonials">
    @Test
    fun `when requesting all testimonials, we return valid list`() {
        val createdTestimonial = givenATestimonial()

        coEvery { testimonialDao.getTestimonials() } returns listOf(createdTestimonial)

        runBlocking {
            val responseTestimonials = controller.getTestimonials()

            assertThat(responseTestimonials).hasSize(1)
            assertThat(responseTestimonials).allMatch { it is TestimonialDto }
        }
    }
    // </editor-fold>

    // <editor-fold desc="Get specific testimonial">
    @Test
    fun `when requesting specific testimonial by ID, we return valid testimonialDto`() {
        val createdTestimonial = givenATestimonial()

        coEvery { testimonialDao.getTestimonialById(any() as UUID) } returns createdTestimonial

        runBlocking {
            val responseTestimonial = controller.getTestimonialById(UUID.randomUUID())

            assertThat(responseTestimonial.review).isEqualTo(createdTestimonial.review)
            assertNotNull(responseTestimonial.createdAt)
            assertNotNull(responseTestimonial.updatedAt)
        }
    }

    @Test
    fun `when requesting specific testimonial by ID where the ID does not exist, we throw exception`() {
        coEvery { testimonialDao.getTestimonialById(any() as UUID) } throws ErrorNotFound

        assertThrows<ErrorNotFound> {
            runBlocking { controller.getTestimonialById(UUID.randomUUID()) }
        }
    }
    // </editor-fold>

    // <editor-fold desc="Create new testimonial">
    @Test
    fun `when creating testimonial with incorrect information, we throw exception`() {
        val postTestimonial = givenAnInvalidInsertTestimonial()

        assertThrows<ErrorInvalidParameters> {
            runBlocking { controller.postTestimonial(postTestimonial) }
        }
    }

    @Test
    fun `when creating testimonial with correct information, we return valid testimonialDto`() {
        val postTestimonial = givenAValidInsertTestimonial()
        val createdTestimonial = givenATestimonial()

        coEvery { companyDao.getListOfExistingCompanyIds(any()) } returns listOf(UUID.fromString("00000000-0000-0000-0000-000000000001"))
        coEvery { jobPositionDao.getListOfExistingJobPositionIds(any()) } returns listOf(UUID.fromString("00000000-0000-0000-0000-000000000001"))
        coEvery { testimonialDao.insertTestimonial(any()) } returns createdTestimonial

        runBlocking {
            val responseTestimonial = controller.postTestimonial(postTestimonial)

            assertThat(responseTestimonial.review).isEqualTo(createdTestimonial.review)
        }
    }

    @Test
    fun `when creating testimonial with correct information but companyId does not exist, we throw exception`() {
        val postTestimonial = givenAValidInsertTestimonial()
        val createdTestimonial = givenATestimonial()

        coEvery { companyDao.getListOfExistingCompanyIds(any()) } returns listOf()
        coEvery { jobPositionDao.getListOfExistingJobPositionIds(any()) } returns listOf(UUID.fromString("00000000-0000-0000-0000-000000000001"))
        coEvery { testimonialDao.insertTestimonial(any()) } returns createdTestimonial

        assertThrows<ErrorUnknownCompanyIdsForCreateTestimonial> {
            runBlocking { controller.postTestimonial(postTestimonial) }
        }
    }

    @Test
    fun `when creating testimonial with correct information but jobPositionId does not exist, we throw exception`() {
        val postTestimonial = givenAValidInsertTestimonial()
        val createdTestimonial = givenATestimonial()

        coEvery { companyDao.getListOfExistingCompanyIds(any()) } returns listOf(UUID.fromString("00000000-0000-0000-0000-000000000001"))
        coEvery { jobPositionDao.getListOfExistingJobPositionIds(any()) } returns listOf()
        coEvery { testimonialDao.insertTestimonial(any()) } returns createdTestimonial

        assertThrows<ErrorUnknownJobPositionIdsForCreateTestimonial> {
            runBlocking { controller.postTestimonial(postTestimonial) }
        }
    }

    @Test
    fun `when creating testimonial and database returns error, we throw exception`() {
        val postTestimonial = givenAValidInsertTestimonial()

        coEvery { companyDao.getListOfExistingCompanyIds(any()) } returns listOf(UUID.fromString("00000000-0000-0000-0000-000000000001"))
        coEvery { jobPositionDao.getListOfExistingJobPositionIds(any()) } returns listOf(UUID.fromString("00000000-0000-0000-0000-000000000001"))
        coEvery { testimonialDao.insertTestimonial(any()) } returns null

        assertThrows<ErrorFailedCreate> {
            runBlocking { controller.postTestimonial(postTestimonial) }
        }
    }
    // </editor-fold>

    // <editor-fold desc="Update specific testimonial">
    @Test
    fun `when updating specific testimonial, we return valid testimonialDto`() {
        val updateTestimonial = givenAValidUpdateTestimonial()
        val createdTestimonial = givenATestimonial()

        coEvery { companyDao.getListOfExistingCompanyIds(any()) } returns listOf(UUID.fromString("00000000-0000-0000-0000-000000000001"))
        coEvery { jobPositionDao.getListOfExistingJobPositionIds(any()) } returns listOf(UUID.fromString("00000000-0000-0000-0000-000000000001"))
        coEvery { testimonialDao.updateTestimonial(any(), any()) } returns createdTestimonial

        runBlocking {
            val responseTestimonial = controller.updateTestimonialById(UUID.randomUUID(), updateTestimonial)

            // Assertion
            assertThat(responseTestimonial.review).isEqualTo(createdTestimonial.review)
        }
    }

    @Test
    fun `when updating specific testimonial but companyId does not exist, we throw exception`() {
        val updateTestimonial = givenAValidUpdateTestimonial()
        val createdTestimonial = givenATestimonial()

        coEvery { companyDao.getListOfExistingCompanyIds(any()) } returns listOf()
        coEvery { jobPositionDao.getListOfExistingJobPositionIds(any()) } returns listOf(UUID.fromString("00000000-0000-0000-0000-000000000001"))
        coEvery { testimonialDao.updateTestimonial(any(), any()) } returns createdTestimonial

        assertThrows<ErrorUnknownCompanyIdsForUpdateTestimonial> {
            runBlocking { controller.updateTestimonialById(UUID.randomUUID(), updateTestimonial) }
        }
    }

    @Test
    fun `when updating specific testimonial but jobPositionId does not exist, we throw exception`() {
        val updateTestimonial = givenAValidUpdateTestimonial()
        val createdTestimonial = givenATestimonial()

        coEvery { companyDao.getListOfExistingCompanyIds(any()) } returns listOf(UUID.fromString("00000000-0000-0000-0000-000000000001"))
        coEvery { jobPositionDao.getListOfExistingJobPositionIds(any()) } returns listOf()
        coEvery { testimonialDao.updateTestimonial(any(), any()) } returns createdTestimonial

        assertThrows<ErrorUnknownJobPositionIdsForUpdateTestimonial> {
            runBlocking { controller.updateTestimonialById(UUID.randomUUID(), updateTestimonial) }
        }
    }

    @Test
    fun `when updating specific testimonial which has invalid data, we throw exception`() {
        val updateTestimonial = givenAnInvalidUpdateTestimonial()

        assertThrows<ErrorInvalidParameters> {
            runBlocking { controller.updateTestimonialById(UUID.randomUUID(), updateTestimonial) }
        }
    }

    @Test
    fun `when updating specific testimonial which does not exist, we throw exception`() {
        val updateTestimonial = givenAValidUpdateTestimonial()

        coEvery { companyDao.getListOfExistingCompanyIds(any()) } returns listOf(UUID.fromString("00000000-0000-0000-0000-000000000001"))
        coEvery { jobPositionDao.getListOfExistingJobPositionIds(any()) } returns listOf(UUID.fromString("00000000-0000-0000-0000-000000000001"))
        coEvery { testimonialDao.updateTestimonial(any(), any()) } throws ErrorFailedUpdate

        assertThrows<ErrorFailedUpdate> {
            runBlocking { controller.updateTestimonialById(UUID.randomUUID(), updateTestimonial) }
        }
    }
    // </editor-fold>

    // <editor-fold desc="Delete testimonial">
    @Test
    fun `when deleting specific testimonial, we return valid testimonialDto`() {
        coEvery { testimonialDao.deleteTestimonial(any()) } returns true

        assertDoesNotThrow {
            runBlocking {
                controller.deleteTestimonialById(UUID.randomUUID())
            }
        }
    }

    @Test
    fun `when deleting specific testimonial which does not exist, we throw exception`() {
        coEvery { testimonialDao.deleteTestimonial(any()) } returns false

        assertThrows<ErrorFailedDelete> {
            runBlocking { controller.deleteTestimonialById(UUID.randomUUID()) }
        }
    }
    // </editor-fold>
}
