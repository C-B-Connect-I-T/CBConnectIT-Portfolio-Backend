package com.cbconnectit.modules.experiences

import com.cbconnectit.data.dto.requests.experience.InsertNewExperience
import com.cbconnectit.data.dto.requests.experience.ExperienceDto
import com.cbconnectit.data.dto.requests.experience.UpdateExperience
import com.cbconnectit.domain.interfaces.ICompanyDao
import com.cbconnectit.domain.interfaces.IJobPositionDao
import com.cbconnectit.domain.interfaces.IExperienceDao
import com.cbconnectit.domain.interfaces.ITagDao
import com.cbconnectit.domain.models.experience.toDto
import com.cbconnectit.modules.BaseController
import com.cbconnectit.plugins.dbQuery
import com.cbconnectit.statuspages.*
import org.koin.core.component.inject
import java.util.*

class ExperienceControllerImpl: BaseController(), ExperienceController {

    private val experienceDao by inject<IExperienceDao>()
    private val companyDao by inject<ICompanyDao>()
    private val jobPositionDao by inject<IJobPositionDao>()
    private val tagDao by inject<ITagDao>()

    override suspend fun getExperiences(): List<ExperienceDto> = dbQuery{
        experienceDao.getExperiences().map { it.toDto() }
    }

    override suspend fun getExperienceById(experienceId: UUID): ExperienceDto = dbQuery{
        experienceDao.getExperienceById(experienceId)?.toDto() ?: throw ErrorNotFound
    }

    override suspend fun postExperience(insertNewExperience: InsertNewExperience): ExperienceDto = dbQuery{
        if (!insertNewExperience.isValid) throw ErrorInvalidParameters

        val companyIds = insertNewExperience.companyUuid.let {  companyDao.getListOfExistingCompanyIds(listOf(it)) }

        if (companyIds.count() != 1) {
            throw ErrorUnknownCompanyIdsForCreateExperience(listOf(insertNewExperience.companyUuid))
        }

        val jobPositionIds = insertNewExperience.jobPositionUuid.let {  jobPositionDao.getListOfExistingJobPositionIds(listOf(it)) }

        if (jobPositionIds.count() != 1) {
            throw ErrorUnknownJobPositionIdsForCreateExperience(listOf(insertNewExperience.jobPositionUuid))
        }

        val tags = insertNewExperience.tags
        val tagUUIDS = tags.map { UUID.fromString(it) }
        val existingLinkUUIDs = tagDao.getListOfExistingTagIds(tagUUIDS)

        // A project can only be added when all the added tags exist
        if (existingLinkUUIDs.count() != tags.count()) {
            val nonExistingIds = tagUUIDS.filterNot { existingLinkUUIDs.contains(it) }
            throw ErrorUnknownTagIdsForCreateExperience(nonExistingIds)
        }

        experienceDao.insertExperience(insertNewExperience)?.toDto() ?: throw  ErrorFailedCreate
    }

    override suspend fun updateExperienceById(experienceId: UUID, updateExperience: UpdateExperience): ExperienceDto = dbQuery {
        if (!updateExperience.isValid) throw ErrorInvalidParameters

        val companyIds = updateExperience.companyUuid.let {  companyDao.getListOfExistingCompanyIds(listOf(it)) }

        if (companyIds.count() != 1) {
            throw ErrorUnknownCompanyIdsForUpdateExperience(listOf(updateExperience.companyUuid))
        }

        val jobPositionIds = updateExperience.jobPositionUuid.let {  jobPositionDao.getListOfExistingJobPositionIds(listOf(it)) }

        if (jobPositionIds.count() != 1) {
            throw ErrorUnknownJobPositionIdsForUpdateExperience(listOf(updateExperience.jobPositionUuid))
        }

        val tags = updateExperience.tags
        val tagUUIDS = tags.map { UUID.fromString(it) }
        val existingLinkUUIDs = tagDao.getListOfExistingTagIds(tagUUIDS)

        // A project can only be added when all the added tags exist
        if (existingLinkUUIDs.count() != tags.count()) {
            val nonExistingIds = tagUUIDS.filterNot { existingLinkUUIDs.contains(it) }
            throw ErrorUnknownTagIdsForUpdateExperience(nonExistingIds)
        }

        experienceDao.updateExperience(experienceId, updateExperience)?.toDto() ?: throw ErrorFailedUpdate
    }

    override suspend fun deleteExperienceById(experienceId: UUID) = dbQuery{
        val deleted = experienceDao.deleteExperience(experienceId)
        if (!deleted) throw ErrorFailedDelete
    }
}

interface ExperienceController  {
    suspend fun getExperiences(): List<ExperienceDto>
    suspend fun getExperienceById(experienceId: UUID): ExperienceDto
    suspend fun postExperience(insertNewExperience: InsertNewExperience): ExperienceDto
    suspend fun updateExperienceById(experienceId: UUID, updateExperience: UpdateExperience): ExperienceDto
    suspend fun deleteExperienceById(experienceId: UUID)
}