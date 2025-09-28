package com.cbconnectit.data.database.dao

import com.cbconnectit.data.database.tables.LinksProjectsPivotTable
import com.cbconnectit.data.database.tables.LinksTable
import com.cbconnectit.data.database.tables.ProjectsTable
import com.cbconnectit.data.database.tables.TagsProjectsPivotTable
import com.cbconnectit.data.database.tables.TagsTable
import com.cbconnectit.data.database.tables.toLink
import com.cbconnectit.data.database.tables.toProject
import com.cbconnectit.data.database.tables.toTag
import com.cbconnectit.data.dto.requests.project.InsertNewProject
import com.cbconnectit.data.dto.requests.project.UpdateProject
import com.cbconnectit.domain.interfaces.IProjectDao
import com.cbconnectit.domain.models.project.Project
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.notInList
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import java.time.LocalDateTime
import java.util.*

class ProjectDaoImpl : IProjectDao {
    override fun getProjectById(id: UUID): Project? {
        val results = ProjectsTable
            .selectAll()
            .where { ProjectsTable.id eq id }
            .orderBy(ProjectsTable.createdAt to SortOrder.DESC)
            .toList()
            .firstOrNull()

        if (results == null) return null

        val projectIds = results[ProjectsTable.id].value

        val tags = TagsProjectsPivotTable.innerJoin(TagsTable)
            .selectAll()
            .where { TagsProjectsPivotTable.projectId eq projectIds }
            .groupBy { it[TagsProjectsPivotTable.projectId].value }

        val links = LinksProjectsPivotTable.innerJoin(LinksTable)
            .selectAll()
            .where { LinksProjectsPivotTable.projectId eq projectIds }
            .groupBy { it[LinksProjectsPivotTable.projectId].value }

        return results.toProject().copy(
            tags = tags[id]?.map { it.toTag() } ?: emptyList(),
            links = links[id]?.map { it.toLink() } ?: emptyList()
        )
    }

    override fun getProjects(): List<Project> {
        // Don't join all tables at once to avoid row multiplication
        // e.g. if a project has 3 tags and 2 links, joining all tables would produce 6 rows for that project
        // instead, fetch projects first, then fetch tags and links separately and map them back to projects
        val results = ProjectsTable
            .selectAll()
            .orderBy(ProjectsTable.updatedAt to SortOrder.DESC)
            .toList()

        val projectIds = results.map { it[ProjectsTable.id].value }

        // Fetch tags and links in bulk to avoid N+1 query problem
        val tags = TagsProjectsPivotTable.innerJoin(TagsTable)
            .selectAll()
            .where { TagsProjectsPivotTable.projectId inList projectIds }
            .groupBy { it[TagsProjectsPivotTable.projectId].value }

        val links = LinksProjectsPivotTable.innerJoin(LinksTable)
            .selectAll()
            .where { LinksProjectsPivotTable.projectId inList projectIds }
            .groupBy { it[LinksProjectsPivotTable.projectId].value }

        return results
            .distinctBy { it[ProjectsTable.id].value }
            .map { row ->
                val id = row[ProjectsTable.id].value
                row.toProject().copy(
                    tags = tags[id]?.map { it.toTag() } ?: emptyList(),
                    links = links[id]?.map { it.toLink() } ?: emptyList()
                )
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
