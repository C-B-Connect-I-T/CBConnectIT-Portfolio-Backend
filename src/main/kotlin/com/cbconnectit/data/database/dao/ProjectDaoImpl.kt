package com.cbconnectit.data.database.dao

import com.cbconnectit.data.database.tables.LinksProjectsPivotTable
import com.cbconnectit.data.database.tables.LinksTable
import com.cbconnectit.data.database.tables.ProjectsTable
import com.cbconnectit.data.database.tables.TagsProjectsPivotTable
import com.cbconnectit.data.database.tables.TagsTable
import com.cbconnectit.data.database.tables.parseLinks
import com.cbconnectit.data.database.tables.parseTags
import com.cbconnectit.data.database.tables.toProject
import com.cbconnectit.data.dto.requests.project.InsertNewProject
import com.cbconnectit.data.dto.requests.project.UpdateProject
import com.cbconnectit.domain.interfaces.IProjectDao
import com.cbconnectit.domain.models.link.Link
import com.cbconnectit.domain.models.project.Project
import com.cbconnectit.domain.models.tag.Tag
import org.jetbrains.exposed.sql.Query
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.notInList
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import java.time.LocalDateTime
import java.util.*

class ProjectDaoImpl : IProjectDao {
    override fun getProjectById(id: UUID): Project? {
        val projectWithRelations = (ProjectsTable leftJoin TagsProjectsPivotTable leftJoin TagsTable leftJoin LinksProjectsPivotTable leftJoin LinksTable)

        val results = projectWithRelations.select { ProjectsTable.id eq id }
            .orderBy(ProjectsTable.createdAt to SortOrder.DESC)

        val tags = parseTags(results)
        val links = parseLinks(results)

        return results
            .distinctBy { it[ProjectsTable.id].value }
            .map { row ->
                row.toProject().copy(
                    tags = tags[id]?.distinctBy { it.id } ?: emptyList(),
                    links = links[id]?.distinctBy { it.id } ?: emptyList()
                )
            }
            .firstOrNull()
    }

    override fun getProjects(): List<Project> {
        val projectWithRelations = (ProjectsTable leftJoin TagsProjectsPivotTable leftJoin TagsTable leftJoin LinksProjectsPivotTable leftJoin LinksTable)

        val results = projectWithRelations.selectAll()
            .orderBy(ProjectsTable.createdAt to SortOrder.DESC)

        val tags = parseTags(results)
        val links = parseLinks(results)

        return results
            .distinctBy { it[ProjectsTable.id].value }
            .map { row ->
                val id = row[ProjectsTable.id].value
                row.toProject().copy(
                    tags = tags[id]?.distinctBy { it.id } ?: emptyList(),
                    links = links[id]?.distinctBy { it.id } ?: emptyList()
                )
            }
    }

    private fun parseTags(results: Query): MutableMap<UUID, List<Tag>> {
        return parseTags(results) {
            it[ProjectsTable.id].value
        }
    }

    private fun parseLinks(results: Query): MutableMap<UUID, List<Link>> {
        return parseLinks(results) {
            it[ProjectsTable.id].value
        }
    }

    override fun insertProject(insertNewProject: InsertNewProject): Project? {
        val id = ProjectsTable.insertAndGetId { body ->
            body[title] = insertNewProject.title
            body[shortDescription] = insertNewProject.shortDescription
            body[description] = insertNewProject.description
            insertNewProject.bannerImageUrl?.let { body[bannerImageUrl] = it }
            insertNewProject.imageUrl?.let { body[imageUrl] = it }
        }.value

        insertNewProject.tags?.forEach { tagId ->
            TagsProjectsPivotTable.insert {
                it[projectId] = id
                it[this.tagId] = UUID.fromString(tagId)
            }
        }

        insertNewProject.links?.forEach { linkId ->
            LinksProjectsPivotTable.insert {
                it[projectId] = id
                it[this.linkId] = UUID.fromString(linkId)
            }
        }

        return getProjectById(id)
    }

    override fun updateProject(id: UUID, updateProject: UpdateProject): Project? {
        ProjectsTable.update({ ProjectsTable.id eq id }) { body ->
            body[title] = updateProject.title
            body[shortDescription] = updateProject.shortDescription
            body[description] = updateProject.description
            updateProject.bannerImageUrl?.let { body[bannerImageUrl] = it }
            updateProject.imageUrl?.let { body[imageUrl] = it }

            body[updatedAt] = LocalDateTime.now()
        }

        TagsProjectsPivotTable.deleteWhere {
            projectId eq id and (tagId notInList (updateProject.tags?.map { stringId -> UUID.fromString(stringId) } ?: emptyList()))
        }
        updateProject.tags?.forEach { tagId ->
            TagsProjectsPivotTable.insert {
                it[projectId] = id
                it[this.tagId] = UUID.fromString(tagId)
            }
        }

        LinksProjectsPivotTable.deleteWhere {
            projectId eq id and (linkId notInList (updateProject.links?.map { stringId -> UUID.fromString(stringId) } ?: emptyList()))
        }
        updateProject.links?.forEach { linkId ->
            LinksProjectsPivotTable.insert {
                it[projectId] = id
                it[this.linkId] = UUID.fromString(linkId)
            }
        }

        return getProjectById(id)
    }

    override fun deleteProject(id: UUID): Boolean {
        val result = ProjectsTable.deleteWhere { ProjectsTable.id eq id }
        val result2 = TagsProjectsPivotTable.deleteWhere { projectId eq id }
        val result3 = LinksProjectsPivotTable.deleteWhere { projectId eq id }

        return result > 0 && result2 > 0 && result3 > 0
    }
}
