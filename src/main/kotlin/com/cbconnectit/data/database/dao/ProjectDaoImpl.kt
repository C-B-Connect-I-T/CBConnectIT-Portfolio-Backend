package com.cbconnectit.data.database.dao

import com.cbconnectit.data.database.tables.LinksProjectsPivotTable
import com.cbconnectit.data.database.tables.LinksTable
import com.cbconnectit.data.database.tables.MediaFilesTable
import com.cbconnectit.data.database.tables.ProjectsTable
import com.cbconnectit.data.database.tables.TagsProjectsPivotTable
import com.cbconnectit.data.database.tables.TagsTable
import com.cbconnectit.data.database.tables.toLink
import com.cbconnectit.data.database.tables.toMediaFile
import com.cbconnectit.data.database.tables.toProject
import com.cbconnectit.data.database.tables.toTag
import com.cbconnectit.data.dto.requests.project.InsertNewProject
import com.cbconnectit.data.dto.requests.project.UpdateProject
import com.cbconnectit.domain.interfaces.IProjectDao
import com.cbconnectit.domain.models.mediafile.MediaFile
import com.cbconnectit.domain.models.mediafile.MediaType
import com.cbconnectit.domain.models.mediafile.OwnerType
import com.cbconnectit.domain.models.project.Project
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.notInList
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.insertIgnore
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import java.time.LocalDateTime
import java.util.*

class ProjectDaoImpl : IProjectDao {
    override fun getProjectById(id: UUID): Project? {
        val results = ProjectsTable
            .selectAll()
            .where { ProjectsTable.id eq id }
            .toList()
            .firstOrNull() ?: return null

        val tags = TagsProjectsPivotTable.innerJoin(TagsTable)
            .selectAll()
            .where { TagsProjectsPivotTable.projectId eq id }
            .groupBy { it[TagsProjectsPivotTable.projectId].value }

        val links = LinksProjectsPivotTable.innerJoin(LinksTable)
            .selectAll()
            .where { LinksProjectsPivotTable.projectId eq id }
            .groupBy { it[LinksProjectsPivotTable.projectId].value }

        val mediaFiles = fetchMediaForProjects(listOf(id))

        return results.toProject().copy(
            tags = tags[id]?.map { it.toTag() } ?: emptyList(),
            links = links[id]?.map { it.toLink() } ?: emptyList(),
            image = mediaFiles[id]?.firstOrNull { it.mediaType == MediaType.IMAGE },
            bannerImage = mediaFiles[id]?.firstOrNull { it.mediaType == MediaType.BANNER }
        )
    }

    override fun getProjects(): List<Project> {
        val results = ProjectsTable
            .selectAll()
            .orderBy(ProjectsTable.updatedAt to SortOrder.DESC)
            .toList()

        val projectIds = results.map { it[ProjectsTable.id].value }

        val tags = TagsProjectsPivotTable.innerJoin(TagsTable)
            .selectAll()
            .where { TagsProjectsPivotTable.projectId inList projectIds }
            .groupBy { it[TagsProjectsPivotTable.projectId].value }

        val links = LinksProjectsPivotTable.innerJoin(LinksTable)
            .selectAll()
            .where { LinksProjectsPivotTable.projectId inList projectIds }
            .groupBy { it[LinksProjectsPivotTable.projectId].value }

        val mediaFiles = fetchMediaForProjects(projectIds)

        return results
            .distinctBy { it[ProjectsTable.id].value }
            .map { row ->
                val id = row[ProjectsTable.id].value
                row.toProject().copy(
                    tags = tags[id]?.map { it.toTag() } ?: emptyList(),
                    links = links[id]?.map { it.toLink() } ?: emptyList(),
                    image = mediaFiles[id]?.firstOrNull { it.mediaType == MediaType.IMAGE },
                    bannerImage = mediaFiles[id]?.firstOrNull { it.mediaType == MediaType.BANNER }
                )
            }
    }

    private fun fetchMediaForProjects(projectIds: List<UUID>): Map<UUID, List<MediaFile>> {
        if (projectIds.isEmpty()) return emptyMap()
        return MediaFilesTable
            .selectAll()
            .where {
                (MediaFilesTable.ownerId inList projectIds) and
                        (MediaFilesTable.ownerType eq OwnerType.PROJECT)
            }
            .map { it.toMediaFile() }
            .groupBy { it.ownerId }
    }

    override fun insertProject(insertNewProject: InsertNewProject): Project? {
        val id = ProjectsTable.insertAndGetId { body ->
            body[title] = insertNewProject.title
            body[shortDescription] = insertNewProject.shortDescription
            body[description] = insertNewProject.description
        }.value

        insertNewProject.tags?.forEach { tagId ->
            TagsProjectsPivotTable.insertIgnore {
                it[projectId] = id
                it[this.tagId] = UUID.fromString(tagId)
            }
        }

        insertNewProject.links?.forEach { linkId ->
            LinksProjectsPivotTable.insertIgnore {
                it[projectId] = id
                it[this.linkId] = UUID.fromString(linkId)
            }
        }

        return getProjectById(id)
    }

    override fun updateProject(id: UUID, updateProject: UpdateProject): Project? {
        val updateCount = ProjectsTable.update({ ProjectsTable.id eq id }) { body ->
            body[title] = updateProject.title
            body[shortDescription] = updateProject.shortDescription
            body[description] = updateProject.description
            body[updatedAt] = LocalDateTime.now()
        }

        if (updateCount == 0) return null

        TagsProjectsPivotTable.deleteWhere {
            projectId eq id and (tagId notInList (updateProject.tags?.map { stringId -> UUID.fromString(stringId) } ?: emptyList()))
        }
        updateProject.tags?.forEach { tagId ->
            TagsProjectsPivotTable.insertIgnore {
                it[projectId] = id
                it[this.tagId] = UUID.fromString(tagId)
            }
        }

        LinksProjectsPivotTable.deleteWhere {
            projectId eq id and (linkId notInList (updateProject.links?.map { stringId -> UUID.fromString(stringId) } ?: emptyList()))
        }
        updateProject.links?.forEach { linkId ->
            LinksProjectsPivotTable.insertIgnore {
                it[projectId] = id
                it[this.linkId] = UUID.fromString(linkId)
            }
        }

        return getProjectById(id)
    }

    override fun deleteProject(id: UUID): Boolean {
        return ProjectsTable.deleteWhere { ProjectsTable.id eq id } > 0
    }
}
