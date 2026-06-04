package com.cbconnectit.dao

import com.cbconnectit.data.database.dao.ProjectDaoImpl
import com.cbconnectit.data.database.tables.LinksProjectsPivotTable
import com.cbconnectit.data.database.tables.LinksTable
import com.cbconnectit.data.database.tables.ProjectsTable
import com.cbconnectit.data.database.tables.TagsProjectsPivotTable
import com.cbconnectit.data.database.tables.TagsTable
import com.cbconnectit.instrumentation.LinkInstrumentation
import com.cbconnectit.instrumentation.ProjectInstrumentation
import com.cbconnectit.instrumentation.ProjectInstrumentation.givenAValidInsertProject
import com.cbconnectit.instrumentation.ProjectInstrumentation.givenAValidUpdateProject
import com.cbconnectit.instrumentation.TagInstrumentation
import kotlinx.coroutines.delay
import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.insertIgnore
import org.junit.jupiter.api.Test
import java.util.*
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class ProjectDaoImplTest : BaseDaoTest() {

    private val dao = ProjectDaoImpl()

    override suspend fun seedData() {
        // Seed tags first (foreign key dependency)
        TagInstrumentation.givenTagList().take(3).forEachIndexed { index, data ->
            TagsTable.insert {
                it[id] = UUID.fromString("00000000-0000-0000-0000-00000000000${index + 1}")
                it[name] = data.name
                it[slug] = data.slug
                it[createdAt] = data.createdAt
                it[updatedAt] = data.updatedAt
            }
        }

        // Seed links
        LinkInstrumentation.givenLinkList().take(3).forEachIndexed { index, data ->
            LinksTable.insert {
                it[id] = UUID.fromString("00000000-0000-0000-0000-00000000000${index + 1}")
                it[url] = data.url
                it[type] = data.type
                it[createdAt] = data.createdAt
                it[updatedAt] = data.updatedAt
            }
        }

        // Seed projects
        ProjectInstrumentation.givenProjectList().forEachIndexed { index, data ->
            ProjectsTable.insert {
                it[id] = data.id
                it[title] = data.title
                it[description] = data.description
                it[shortDescription] = data.shortDescription
                it[imageUrl] = data.imageUrl
                it[bannerImageUrl] = data.bannerImageUrl
                it[createdAt] = data.createdAt
                it[updatedAt] = data.updatedAt
            }
        }

        // Seed project-link relationships
        listOf(
            Pair("00000000-0000-0000-0000-000000000001", "00000000-0000-0000-0000-000000000001"),
            Pair("00000000-0000-0000-0000-000000000001", "00000000-0000-0000-0000-000000000002"),
            Pair("00000000-0000-0000-0000-000000000002", "00000000-0000-0000-0000-000000000002"),
            Pair("00000000-0000-0000-0000-000000000003", "00000000-0000-0000-0000-000000000001"),
            Pair("00000000-0000-0000-0000-000000000004", "00000000-0000-0000-0000-000000000002"),
        ).forEach { data ->
            LinksProjectsPivotTable.insertIgnore {
                it[projectId] = UUID.fromString(data.first)
                it[linkId] = UUID.fromString(data.second)
            }
        }

        // Seed project-tag relationships
        listOf(
            Pair("00000000-0000-0000-0000-000000000001", "00000000-0000-0000-0000-000000000001"),
            Pair("00000000-0000-0000-0000-000000000002", "00000000-0000-0000-0000-000000000002"),
            Pair("00000000-0000-0000-0000-000000000002", "00000000-0000-0000-0000-000000000001"),
            Pair("00000000-0000-0000-0000-000000000003", "00000000-0000-0000-0000-000000000001"),
            Pair("00000000-0000-0000-0000-000000000004", "00000000-0000-0000-0000-000000000002"),
        ).forEach { data ->
            TagsProjectsPivotTable.insertIgnore {
                it[projectId] = UUID.fromString(data.first)
                it[tagId] = UUID.fromString(data.second)
            }
        }
    }

    // <editor-fold desc="Get all Projects">
    @Test
    fun `getProjects but none exists, return empty list`() = runTest(shouldSeedData = false) {
        val list = dao.getProjects()
        assertThat(list).isEmpty()
    }

    @Test
    fun `getProjects return the list`() = runTest {
        val list = dao.getProjects()
        assertThat(list).hasSize(4)
    }
    // </editor-fold>

    // <editor-fold desc="Get specific Project by id">
    @Test
    fun `getProject where item exists, return correct Project`() = runTest {
        val project = dao.getProjectById(UUID.fromString("00000000-0000-0000-0000-000000000001"))

        assertThat(project).matches {
            it?.title == ProjectInstrumentation.givenProjectList()[0].title &&
                    it.links.count() == 2
        }
    }

    @Test
    fun `getProject where item does not exists, return 'null'`() = runTest {
        val project = dao.getProjectById(UUID.randomUUID())

        assertNull(project)
    }
    // </editor-fold>

    // <editor-fold desc="Create new Project">
    @Test
    fun `insertProject where information is correct, database is storing Project and returning correct content`() = runTest {
        val validProject = givenAValidInsertProject()
        val project = dao.insertProject(validProject)

        assertThat(project).matches {
            it?.title == validProject.title &&
                    it.createdAt == it.updatedAt
        }
    }
    // </editor-fold>

    // <editor-fold desc="Update Project">
    @Test
    fun `updateProject where information is correct, database is storing information and returning the correct content`() = runTest {
        // adding a delay so there is a clear difference between `updatedAt` and `createdAt`
        delay(1000)

        val validUpdateProject = givenAValidUpdateProject()
        val project = dao.updateProject(UUID.fromString("00000000-0000-0000-0000-000000000001"), validUpdateProject)

        assertThat(project).matches {
            it?.title == validUpdateProject.title &&
                    it.createdAt != it.updatedAt &&
                    it.links.count() == 1 &&
                    it.tags.count() == 1
        }
    }

    @Test
    fun `updateProject where information is correct but Project with id does not exist, database does nothing and returns 'null'`() = runTest {
        val validProject = givenAValidUpdateProject()
        val project = dao.updateProject(UUID.randomUUID(), validProject)

        assertNull(project)
    }
    // </editor-fold>

    // <editor-fold desc="Delete Project">
    @Test
    fun `deleteProject for id that exists, return true`() = runTest {
        val deleted = dao.deleteProject(UUID.fromString("00000000-0000-0000-0000-000000000001"))
        assertTrue(deleted)
    }

    @Test
    fun `deleteProject for id that does not exist, return false`() = runTest {
        val deleted = dao.deleteProject(UUID.randomUUID())
        assertFalse(deleted)
    }
    // </editor-fold>
}
