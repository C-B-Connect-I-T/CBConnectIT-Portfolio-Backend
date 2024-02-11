package com.cbconnectit.data.database.dao

import com.cbconnectit.data.database.tables.*
import com.cbconnectit.data.dto.requests.project.InsertNewProject
import com.cbconnectit.data.dto.requests.project.UpdateProject
import com.cbconnectit.domain.interfaces.IProjectDao
import com.cbconnectit.domain.models.link.Link
import com.cbconnectit.domain.models.project.Project
import com.cbconnectit.domain.models.tag.Tag
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.notInList
import java.time.LocalDateTime
import java.util.*

class ProjectDaoImpl : IProjectDao {
    override fun getProjectById(id: UUID): Project? {
        val projectWithRelations = (ProjectsTable innerJoin TagsProjectsPivotTable innerJoin TagsTable innerJoin LinksProjectsPivotTable innerJoin LinksTable)

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
        val projectWithRelations = (ProjectsTable innerJoin TagsProjectsPivotTable innerJoin TagsTable innerJoin LinksProjectsPivotTable innerJoin LinksTable)

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
        return results
            .distinctBy { it.getOrNull(TagsTable.id)?.value }
            .fold(mutableMapOf()) { map, resultRow ->
                val projectId = resultRow[ProjectsTable.id].value

                val tag = if (resultRow.getOrNull(TagsTable.id) != null) {
                    resultRow.toTag()
                } else null

                val current = map.getOrDefault(projectId, emptyList())
                map[projectId] = current.toMutableList() + listOfNotNull(tag)
                map
            }
    }

    private fun parseLinks(results: Query): MutableMap<UUID, List<Link>> {
        val newMap = results
            .distinctBy { it.getOrNull(LinksTable.id)?.value }
            .fold(mutableMapOf<UUID, List<Link>>()) { map, resultRow ->
                val projectId = resultRow[ProjectsTable.id].value

                val link = if (resultRow.getOrNull(LinksTable.id) != null) {
                    resultRow.toLink()
                } else null

                val current = map.getOrDefault(projectId, emptyList())
                map[projectId] = current.toMutableList() + listOfNotNull(link)
                map
            }

        return newMap
    }

    override fun insertProject(insertNewProject: InsertNewProject): Project? {
        val id = ProjectsTable.insertAndGetId { body ->
            body[title] = insertNewProject.title
            body[shortDescription] = insertNewProject.shortDescription
            body[description] = insertNewProject.description
            insertNewProject.bannerImage?.let { body[bannerImage] = it }
            insertNewProject.image?.let { body[image] = it }
        }.value

        insertNewProject.tags.forEach { tagId ->
            TagsProjectsPivotTable.insert {
                it[projectId] = id
                it[this.tagId] = UUID.fromString(tagId)
            }
        }

        insertNewProject.links.forEach { linkId ->
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
            updateProject.bannerImage?.let { body[bannerImage] = it }
            updateProject.image?.let { body[image] = it }

            body[updatedAt] = LocalDateTime.now()
        }

        TagsProjectsPivotTable.deleteWhere {
            projectId eq id and (tagId notInList updateProject.tags.map { stringId -> UUID.fromString(stringId) })
        }
        updateProject.tags.forEach { tagId ->
            TagsProjectsPivotTable.insert {
                it[projectId] = id
                it[this.tagId] = UUID.fromString(tagId)
            }
        }

        LinksProjectsPivotTable.deleteWhere {
            projectId eq id and (linkId notInList updateProject.links.map { stringId -> UUID.fromString(stringId) })
        }
        updateProject.links.forEach { linkId ->
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