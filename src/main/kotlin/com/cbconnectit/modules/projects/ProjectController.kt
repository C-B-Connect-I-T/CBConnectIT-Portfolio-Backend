package com.cbconnectit.modules.projects

import com.cbconnectit.data.dto.requests.project.InsertNewProject
import com.cbconnectit.data.dto.requests.project.ProjectDto
import com.cbconnectit.data.dto.requests.project.UpdateProject
import com.cbconnectit.domain.interfaces.ILinkDao
import com.cbconnectit.domain.interfaces.IProjectDao
import com.cbconnectit.domain.interfaces.ITagDao
import com.cbconnectit.domain.models.project.toDto
import com.cbconnectit.plugins.dbQuery
import com.cbconnectit.statuspages.ErrorFailedCreate
import com.cbconnectit.statuspages.ErrorFailedDelete
import com.cbconnectit.statuspages.ErrorFailedUpdate
import com.cbconnectit.statuspages.ErrorInvalidParameters
import com.cbconnectit.statuspages.ErrorNotFound
import com.cbconnectit.statuspages.ErrorUnknownLinkIdsForCreateProject
import com.cbconnectit.statuspages.ErrorUnknownLinkIdsForUpdateProject
import com.cbconnectit.statuspages.ErrorUnknownTagIdsForCreateProject
import com.cbconnectit.statuspages.ErrorUnknownTagIdsForUpdateProject
import java.util.*

class ProjectControllerImpl(
    private val projectDao: IProjectDao,
    private val tagDao: ITagDao,
    private val linkDao: ILinkDao
) : ProjectController {

    override suspend fun getProjects(): List<ProjectDto> = dbQuery {
        projectDao.getProjects().map { it.toDto() }
    }

    override suspend fun getProjectById(projectId: UUID): ProjectDto = dbQuery {
        projectDao.getProjectById(projectId)?.toDto() ?: throw ErrorNotFound
    }

    override suspend fun postProject(insertNewProject: InsertNewProject): ProjectDto = dbQuery {
        if (!insertNewProject.isValid) throw ErrorInvalidParameters

        val tagUUIDs = insertNewProject.tags?.map { UUID.fromString(it) } ?: emptyList()
        val existingTagUUIDs = tagDao.getListOfExistingTagIds(tagUUIDs)

        val linkUUIDS = insertNewProject.links?.map { UUID.fromString(it) } ?: emptyList()
        val existingLinkUUIDs = linkDao.getListOfExistingLinkIds(linkUUIDS)

        // A project can only be added when all the added tags exist
        if (tagUUIDs.isNotEmpty() && existingTagUUIDs.count() != insertNewProject.tags?.count()) {
            val nonExistingIds = tagUUIDs.filterNot { existingTagUUIDs.contains(it) }
            throw ErrorUnknownTagIdsForCreateProject(nonExistingIds)
        }

        // A project can only be added when all the added tags exist
        if (linkUUIDS.isNotEmpty() && existingLinkUUIDs.count() != insertNewProject.links?.count()) {
            val nonExistingIds = linkUUIDS.filterNot { existingLinkUUIDs.contains(it) }
            throw ErrorUnknownLinkIdsForCreateProject(nonExistingIds)
        }

        projectDao.insertProject(insertNewProject)?.toDto() ?: throw ErrorFailedCreate
    }

    override suspend fun updateProjectById(projectId: UUID, updateProject: UpdateProject): ProjectDto = dbQuery {
        if (!updateProject.isValid) throw ErrorInvalidParameters

        val tagUUIDs = updateProject.tags?.map { UUID.fromString(it) } ?: emptyList()
        val existingTagUUIDs = tagDao.getListOfExistingTagIds(tagUUIDs)

        val linkUUIDS = updateProject.links?.map { UUID.fromString(it) } ?: emptyList()
        val existingLinkUUIDs = linkDao.getListOfExistingLinkIds(linkUUIDS)

        // A project can only be added when all the added tags exist
        if (tagUUIDs.isNotEmpty() && existingTagUUIDs.count() != updateProject.tags?.count()) {
            val nonExistingIds = tagUUIDs.filterNot { existingTagUUIDs.contains(it) }
            throw ErrorUnknownTagIdsForUpdateProject(nonExistingIds)
        }

        // A project can only be added when all the added tags exist
        if (linkUUIDS.isNotEmpty() && existingLinkUUIDs.count() != updateProject.links?.count()) {
            val nonExistingIds = linkUUIDS.filterNot { existingLinkUUIDs.contains(it) }
            throw ErrorUnknownLinkIdsForUpdateProject(nonExistingIds)
        }

        projectDao.updateProject(projectId, updateProject)?.toDto() ?: throw ErrorFailedUpdate
    }

    override suspend fun deleteProjectById(projectId: UUID) = dbQuery {
        val deleted = projectDao.deleteProject(projectId)
        if (!deleted) throw ErrorFailedDelete
    }
}

interface ProjectController {
    suspend fun getProjects(): List<ProjectDto>
    suspend fun getProjectById(projectId: UUID): ProjectDto
    suspend fun postProject(insertNewProject: InsertNewProject): ProjectDto
    suspend fun updateProjectById(projectId: UUID, updateProject: UpdateProject): ProjectDto
    suspend fun deleteProjectById(projectId: UUID)
}
