package com.cbconnectit.modules.jobPositions

import com.cbconnectit.data.dto.requests.jobPosition.InsertNewJobPosition
import com.cbconnectit.data.dto.requests.jobPosition.JobPositionDto
import com.cbconnectit.data.dto.requests.jobPosition.UpdateJobPosition
import com.cbconnectit.domain.interfaces.IJobPositionDao
import com.cbconnectit.domain.models.jobPosition.toDto
import com.cbconnectit.modules.BaseController
import com.cbconnectit.plugins.dbQuery
import com.cbconnectit.statuspages.ErrorDuplicateEntity
import com.cbconnectit.statuspages.ErrorFailedCreate
import com.cbconnectit.statuspages.ErrorFailedDelete
import com.cbconnectit.statuspages.ErrorFailedUpdate
import com.cbconnectit.statuspages.ErrorInvalidParameters
import com.cbconnectit.statuspages.ErrorNotFound
import org.koin.core.component.inject
import java.util.*

class JobPositionControllerImpl : BaseController(), JobPositionController {

    private val jobPositionDao by inject<IJobPositionDao>()

    override suspend fun getJobPositions(): List<JobPositionDto> = dbQuery {
        jobPositionDao.getJobPositions().map { it.toDto() }
    }

    override suspend fun getJobPositionById(jobPositionId: UUID): JobPositionDto = dbQuery {
        jobPositionDao.getJobPositionById(jobPositionId)?.toDto() ?: throw ErrorNotFound
    }

    override suspend fun postJobPosition(insertNewJobPosition: InsertNewJobPosition): JobPositionDto = dbQuery {
        if (!insertNewJobPosition.isValid) throw ErrorInvalidParameters

        val positionUnique = jobPositionDao.jobPositionUnique(insertNewJobPosition.name)
        if (!positionUnique) throw ErrorDuplicateEntity

        jobPositionDao.insertJobPosition(insertNewJobPosition)?.toDto() ?: throw ErrorFailedCreate
    }

    override suspend fun updateJobPositionById(jobPositionId: UUID, updateJobPosition: UpdateJobPosition): JobPositionDto = dbQuery {
        if (!updateJobPosition.isValid) throw ErrorInvalidParameters

        val positionUnique = jobPositionDao.jobPositionUnique(updateJobPosition.name)
        if (!positionUnique) throw ErrorDuplicateEntity

        jobPositionDao.updateJobPosition(jobPositionId, updateJobPosition)?.toDto() ?: throw ErrorFailedUpdate
    }

    override suspend fun deleteJobPositionById(jobPositionId: UUID) = dbQuery {
        val deleted = jobPositionDao.deleteJobPosition(jobPositionId)
        if (!deleted) throw ErrorFailedDelete
    }
}

interface JobPositionController {
    suspend fun getJobPositions(): List<JobPositionDto>
    suspend fun getJobPositionById(jobPositionId: UUID): JobPositionDto
    suspend fun postJobPosition(insertNewJobPosition: InsertNewJobPosition): JobPositionDto
    suspend fun updateJobPositionById(jobPositionId: UUID, updateJobPosition: UpdateJobPosition): JobPositionDto
    suspend fun deleteJobPositionById(jobPositionId: UUID)
}
