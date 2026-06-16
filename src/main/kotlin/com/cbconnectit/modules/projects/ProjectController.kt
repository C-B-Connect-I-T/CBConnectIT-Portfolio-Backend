package com.cbconnectit.modules.projects

import com.cbconnectit.data.dto.requests.project.InsertNewProject
import com.cbconnectit.data.dto.requests.project.ProjectDto
import com.cbconnectit.data.dto.requests.project.UpdateProject
import com.cbconnectit.domain.interfaces.ILinkDao
import com.cbconnectit.domain.interfaces.IProjectDao
import com.cbconnectit.domain.interfaces.ITagDao
import com.cbconnectit.domain.models.link.LinkType
import com.cbconnectit.domain.models.project.toDto
import com.cbconnectit.plugins.dbTransactionalQuery
import com.cbconnectit.plugins.statuspages.ErrorFailedCreate
import com.cbconnectit.plugins.statuspages.ErrorFailedDelete
import com.cbconnectit.plugins.statuspages.ErrorFailedUpdate
import com.cbconnectit.plugins.statuspages.ErrorInvalidParameters
import com.cbconnectit.plugins.statuspages.ErrorNotFound
import com.cbconnectit.plugins.statuspages.ErrorUnknownTagIdsForCreateProject
import com.cbconnectit.plugins.statuspages.ErrorUnknownTagIdsForUpdateProject
import io.ktor.http.*
import java.util.*

class ProjectControllerImpl(
    private val projectDao: IProjectDao,
    private val tagDao: ITagDao,
    private val linkDao: ILinkDao
) : ProjectController {

    override suspend fun getProjects(): List<ProjectDto> = dbTransactionalQuery {
        projectDao.getProjects().map { it.toDto() }
    }

    override suspend fun getProjectById(projectId: UUID): ProjectDto = dbTransactionalQuery {
        projectDao.getProjectById(projectId)?.toDto() ?: throw ErrorNotFound
    }

    override suspend fun postProject(insertNewProject: InsertNewProject): ProjectDto = dbTransactionalQuery {
        if (!insertNewProject.isValid) throw ErrorInvalidParameters

        val tagUUIDs = insertNewProject.tags?.map { UUID.fromString(it) } ?: emptyList()
        val existingTagUUIDs = tagDao.getListOfExistingTagIds(tagUUIDs)

        if (tagUUIDs.isNotEmpty() && existingTagUUIDs.count() != insertNewProject.tags?.count()) {
            val nonExistingIds = tagUUIDs.filterNot { existingTagUUIDs.contains(it) }
            throw ErrorUnknownTagIdsForCreateProject(nonExistingIds)
        }

        val resolvedLinkIds = (insertNewProject.links ?: emptyList()).map { url ->
            val linkType = LinkType.getTypeByUrl(Url(url))
            linkDao.getOrInsertLinkByUrl(url, linkType).toString()
        }

        projectDao.insertProject(insertNewProject.copy(links = resolvedLinkIds))?.toDto() ?: throw ErrorFailedCreate
    }

    override suspend fun updateProjectById(projectId: UUID, updateProject: UpdateProject): ProjectDto = dbTransactionalQuery {
        if (!updateProject.isValid) throw ErrorInvalidParameters

        val tagUUIDs = updateProject.tags?.map { UUID.fromString(it) } ?: emptyList()
        val existingTagUUIDs = tagDao.getListOfExistingTagIds(tagUUIDs)

        if (tagUUIDs.isNotEmpty() && existingTagUUIDs.count() != updateProject.tags?.count()) {
            val nonExistingIds = tagUUIDs.filterNot { existingTagUUIDs.contains(it) }
            throw ErrorUnknownTagIdsForUpdateProject(nonExistingIds)
        }

        val resolvedLinkIds = (updateProject.links ?: emptyList()).map { url ->
            val linkType = LinkType.getTypeByUrl(Url(url))
            linkDao.getOrInsertLinkByUrl(url, linkType).toString()
        }

        projectDao.updateProject(projectId, updateProject.copy(links = resolvedLinkIds))?.toDto() ?: throw ErrorFailedUpdate
    }

    override suspend fun deleteProjectById(projectId: UUID) = dbTransactionalQuery {
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
