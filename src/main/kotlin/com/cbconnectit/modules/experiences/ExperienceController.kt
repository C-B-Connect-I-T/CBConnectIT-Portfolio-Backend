package com.cbconnectit.modules.experiences

import com.cbconnectit.data.dto.requests.experience.ExperienceDto
import com.cbconnectit.data.dto.requests.experience.InsertNewExperience
import com.cbconnectit.data.dto.requests.experience.UpdateExperience
import com.cbconnectit.domain.interfaces.ICompanyDao
import com.cbconnectit.domain.interfaces.IExperienceDao
import com.cbconnectit.domain.interfaces.IJobPositionDao
import com.cbconnectit.domain.interfaces.ITagDao
import com.cbconnectit.domain.models.experience.toDto
import com.cbconnectit.plugins.dbTransactionalQuery
import com.cbconnectit.plugins.statuspages.ErrorFailedCreate
import com.cbconnectit.plugins.statuspages.ErrorFailedDelete
import com.cbconnectit.plugins.statuspages.ErrorFailedUpdate
import com.cbconnectit.plugins.statuspages.ErrorInvalidParameters
import com.cbconnectit.plugins.statuspages.ErrorNotFound
import com.cbconnectit.plugins.statuspages.ErrorUnknownCompanyIdsForCreateExperience
import com.cbconnectit.plugins.statuspages.ErrorUnknownCompanyIdsForUpdateExperience
import com.cbconnectit.plugins.statuspages.ErrorUnknownJobPositionIdsForCreateExperience
import com.cbconnectit.plugins.statuspages.ErrorUnknownJobPositionIdsForUpdateExperience
import com.cbconnectit.plugins.statuspages.ErrorUnknownTagIdsForCreateExperience
import com.cbconnectit.plugins.statuspages.ErrorUnknownTagIdsForUpdateExperience
import java.util.*

class ExperienceControllerImpl(
    private val experienceDao: IExperienceDao,
    private val companyDao: ICompanyDao,
    private val jobPositionDao: IJobPositionDao,
    private val tagDao: ITagDao
) : ExperienceController {

    override suspend fun getExperiences(): List<ExperienceDto> = dbTransactionalQuery {
        experienceDao.getExperiences().map { it.toDto() }
    }

    override suspend fun getExperienceById(experienceId: UUID): ExperienceDto = dbTransactionalQuery {
        experienceDao.getExperienceById(experienceId)?.toDto() ?: throw ErrorNotFound
    }

    override suspend fun postExperience(insertNewExperience: InsertNewExperience): ExperienceDto = dbTransactionalQuery {
        if (!insertNewExperience.isValid) throw ErrorInvalidParameters

        val companyIds = insertNewExperience.companyUuid.let { companyDao.getListOfExistingCompanyIds(listOf(it)) }

        if (companyIds.count() != 1) {
            throw ErrorUnknownCompanyIdsForCreateExperience(listOf(insertNewExperience.companyUuid))
        }

        val jobPositionIds = insertNewExperience.jobPositionUuid.let { jobPositionDao.getListOfExistingJobPositionIds(listOf(it)) }

        if (jobPositionIds.count() != 1) {
            throw ErrorUnknownJobPositionIdsForCreateExperience(listOf(insertNewExperience.jobPositionUuid))
        }

        val tags = insertNewExperience.tags
        val tagUUIDS = tags?.map { UUID.fromString(it) } ?: emptyList()
        val existingLinkUUIDs = tagDao.getListOfExistingTagIds(tagUUIDS)

        // A project can only be added when all the added tags exist
        if (existingLinkUUIDs.count() != tagUUIDS.count()) {
            val nonExistingIds = tagUUIDS.filterNot { existingLinkUUIDs.contains(it) }
            throw ErrorUnknownTagIdsForCreateExperience(nonExistingIds)
        }

        experienceDao.insertExperience(insertNewExperience)?.toDto() ?: throw ErrorFailedCreate
    }

    override suspend fun updateExperienceById(experienceId: UUID, updateExperience: UpdateExperience): ExperienceDto = dbTransactionalQuery {
        if (!updateExperience.isValid) throw ErrorInvalidParameters

        val companyIds = updateExperience.companyUuid.let { companyDao.getListOfExistingCompanyIds(listOf(it)) }

        if (companyIds.count() != 1) {
            throw ErrorUnknownCompanyIdsForUpdateExperience(listOf(updateExperience.companyUuid))
        }

        val jobPositionIds = updateExperience.jobPositionUuid.let { jobPositionDao.getListOfExistingJobPositionIds(listOf(it)) }

        if (jobPositionIds.count() != 1) {
            throw ErrorUnknownJobPositionIdsForUpdateExperience(listOf(updateExperience.jobPositionUuid))
        }

        val tags = updateExperience.tags
        val tagUUIDS = tags?.map { UUID.fromString(it) } ?: emptyList()
        val existingLinkUUIDs = tagDao.getListOfExistingTagIds(tagUUIDS)

        // A project can only be added when all the added tags exist
        if (existingLinkUUIDs.count() != tagUUIDS.count()) {
            val nonExistingIds = tagUUIDS.filterNot { existingLinkUUIDs.contains(it) }
            throw ErrorUnknownTagIdsForUpdateExperience(nonExistingIds)
        }

        experienceDao.updateExperience(experienceId, updateExperience)?.toDto() ?: throw ErrorFailedUpdate
    }

    override suspend fun deleteExperienceById(experienceId: UUID) = dbTransactionalQuery {
        val deleted = experienceDao.deleteExperience(experienceId)
        if (!deleted) throw ErrorFailedDelete
    }
}

interface ExperienceController {
    suspend fun getExperiences(): List<ExperienceDto>
    suspend fun getExperienceById(experienceId: UUID): ExperienceDto
    suspend fun postExperience(insertNewExperience: InsertNewExperience): ExperienceDto
    suspend fun updateExperienceById(experienceId: UUID, updateExperience: UpdateExperience): ExperienceDto
    suspend fun deleteExperienceById(experienceId: UUID)
}
