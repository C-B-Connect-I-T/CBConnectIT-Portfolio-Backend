package com.cbconnectit.modules.jobPositions

import com.cbconnectit.data.dto.requests.jobPosition.InsertNewJobPosition
import com.cbconnectit.data.dto.requests.jobPosition.JobPositionDto
import com.cbconnectit.data.dto.requests.jobPosition.UpdateJobPosition
import com.cbconnectit.domain.interfaces.IJobPositionDao
import com.cbconnectit.domain.models.jobPosition.toDto
import com.cbconnectit.plugins.dbTransactionalQuery
import com.cbconnectit.plugins.statuspages.ErrorDuplicateEntity
import com.cbconnectit.plugins.statuspages.ErrorFailedCreate
import com.cbconnectit.plugins.statuspages.ErrorFailedDelete
import com.cbconnectit.plugins.statuspages.ErrorFailedUpdate
import com.cbconnectit.plugins.statuspages.ErrorInvalidParameters
import com.cbconnectit.plugins.statuspages.ErrorNotFound
import java.util.*

class JobPositionControllerImpl(
    private val jobPositionDao: IJobPositionDao
) : JobPositionController {

    override suspend fun getJobPositions(): List<JobPositionDto> = dbTransactionalQuery {
        jobPositionDao.getJobPositions().map { it.toDto() }
    }

    override suspend fun getJobPositionById(jobPositionId: UUID): JobPositionDto = dbTransactionalQuery {
        jobPositionDao.getJobPositionById(jobPositionId)?.toDto() ?: throw ErrorNotFound
    }

    override suspend fun postJobPosition(insertNewJobPosition: InsertNewJobPosition): JobPositionDto = dbTransactionalQuery {
        if (!insertNewJobPosition.isValid) throw ErrorInvalidParameters

        val positionUnique = jobPositionDao.jobPositionUnique(insertNewJobPosition.name)
        if (!positionUnique) throw ErrorDuplicateEntity

        jobPositionDao.insertJobPosition(insertNewJobPosition)?.toDto() ?: throw ErrorFailedCreate
    }

    override suspend fun updateJobPositionById(jobPositionId: UUID, updateJobPosition: UpdateJobPosition): JobPositionDto = dbTransactionalQuery {
        if (!updateJobPosition.isValid) throw ErrorInvalidParameters

        val positionUnique = jobPositionDao.jobPositionUnique(updateJobPosition.name)
        if (!positionUnique) throw ErrorDuplicateEntity

        jobPositionDao.updateJobPosition(jobPositionId, updateJobPosition)?.toDto() ?: throw ErrorFailedUpdate
    }

    override suspend fun deleteJobPositionById(jobPositionId: UUID) = dbTransactionalQuery {
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
