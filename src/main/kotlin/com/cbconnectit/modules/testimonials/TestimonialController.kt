package com.cbconnectit.modules.testimonials

import com.cbconnectit.data.dto.requests.testimonial.InsertNewTestimonial
import com.cbconnectit.data.dto.requests.testimonial.TestimonialDto
import com.cbconnectit.data.dto.requests.testimonial.UpdateTestimonial
import com.cbconnectit.domain.interfaces.ICompanyDao
import com.cbconnectit.domain.interfaces.IJobPositionDao
import com.cbconnectit.domain.interfaces.ITestimonialDao
import com.cbconnectit.domain.models.testimonial.toDto
import com.cbconnectit.plugins.dbTransactionalQuery
import com.cbconnectit.plugins.statuspages.ErrorFailedCreate
import com.cbconnectit.plugins.statuspages.ErrorFailedDelete
import com.cbconnectit.plugins.statuspages.ErrorFailedUpdate
import com.cbconnectit.plugins.statuspages.ErrorInvalidParameters
import com.cbconnectit.plugins.statuspages.ErrorNotFound
import com.cbconnectit.plugins.statuspages.ErrorUnknownCompanyIdsForCreateTestimonial
import com.cbconnectit.plugins.statuspages.ErrorUnknownCompanyIdsForUpdateTestimonial
import com.cbconnectit.plugins.statuspages.ErrorUnknownJobPositionIdsForCreateTestimonial
import com.cbconnectit.plugins.statuspages.ErrorUnknownJobPositionIdsForUpdateTestimonial
import java.util.*

class TestimonialControllerImpl(
    private val testimonialDao: ITestimonialDao,
    private val companyDao: ICompanyDao,
    private val jobPositionDao: IJobPositionDao
) : TestimonialController {

    override suspend fun getTestimonials(): List<TestimonialDto> = dbTransactionalQuery {
        testimonialDao.getTestimonials().map { it.toDto() }
    }

    override suspend fun getTestimonialById(testimonialId: UUID): TestimonialDto = dbTransactionalQuery {
        testimonialDao.getTestimonialById(testimonialId)?.toDto() ?: throw ErrorNotFound
    }

    override suspend fun postTestimonial(insertNewTestimonial: InsertNewTestimonial): TestimonialDto = dbTransactionalQuery {
        if (!insertNewTestimonial.isValid) throw ErrorInvalidParameters

        val companyIds = insertNewTestimonial.companyUuid.let { companyDao.getListOfExistingCompanyIds(listOf(it)) }

        if (companyIds.count() != 1) {
            throw ErrorUnknownCompanyIdsForCreateTestimonial(listOf(insertNewTestimonial.companyUuid))
        }

        val jobPositionIds = insertNewTestimonial.jobPositionUuid.let { jobPositionDao.getListOfExistingJobPositionIds(listOf(it)) }

        if (jobPositionIds.count() != 1) {
            throw ErrorUnknownJobPositionIdsForCreateTestimonial(listOf(insertNewTestimonial.jobPositionUuid))
        }

        testimonialDao.insertTestimonial(insertNewTestimonial)?.toDto() ?: throw ErrorFailedCreate
    }

    override suspend fun updateTestimonialById(testimonialId: UUID, updateTestimonial: UpdateTestimonial): TestimonialDto = dbTransactionalQuery {
        if (!updateTestimonial.isValid) throw ErrorInvalidParameters

        val companyIds = updateTestimonial.companyUuid.let { companyDao.getListOfExistingCompanyIds(listOf(it)) }

        if (companyIds.count() != 1) {
            throw ErrorUnknownCompanyIdsForUpdateTestimonial(listOf(updateTestimonial.companyUuid))
        }

        val jobPositionIds = updateTestimonial.jobPositionUuid.let { jobPositionDao.getListOfExistingJobPositionIds(listOf(it)) }

        if (jobPositionIds.count() != 1) {
            throw ErrorUnknownJobPositionIdsForUpdateTestimonial(listOf(updateTestimonial.jobPositionUuid))
        }

        testimonialDao.updateTestimonial(testimonialId, updateTestimonial)?.toDto() ?: throw ErrorFailedUpdate
    }

    override suspend fun deleteTestimonialById(testimonialId: UUID) = dbTransactionalQuery {
        val deleted = testimonialDao.deleteTestimonial(testimonialId)
        if (!deleted) throw ErrorFailedDelete
    }
}

interface TestimonialController {
    suspend fun getTestimonials(): List<TestimonialDto>
    suspend fun getTestimonialById(testimonialId: UUID): TestimonialDto
    suspend fun postTestimonial(insertNewTestimonial: InsertNewTestimonial): TestimonialDto
    suspend fun updateTestimonialById(testimonialId: UUID, updateTestimonial: UpdateTestimonial): TestimonialDto
    suspend fun deleteTestimonialById(testimonialId: UUID)
}
