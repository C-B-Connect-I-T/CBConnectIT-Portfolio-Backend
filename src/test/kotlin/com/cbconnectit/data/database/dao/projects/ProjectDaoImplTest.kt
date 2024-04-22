package com.cbconnectit.data.database.dao.projects

import com.cbconnectit.data.database.dao.BaseDaoTest
import com.cbconnectit.data.database.dao.ProjectDaoImpl
import com.cbconnectit.data.database.dao.projects.ProjectInstrumentation.givenAValidInsertProjectBody
import com.cbconnectit.data.database.dao.projects.ProjectInstrumentation.givenAValidUpdateProjectBody
import com.cbconnectit.data.database.tables.LinksProjectsPivotTable
import com.cbconnectit.data.database.tables.LinksTable
import com.cbconnectit.data.database.tables.ProjectsTable
import com.cbconnectit.data.database.tables.TagsProjectsPivotTable
import com.cbconnectit.data.database.tables.TagsTable
import com.cbconnectit.domain.models.link.Link
import com.cbconnectit.domain.models.project.Project
import com.cbconnectit.domain.models.tag.Tag
import kotlinx.coroutines.delay
import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.insertIgnore
import org.junit.jupiter.api.Test
import java.util.*
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class ProjectDaoImplTest : BaseDaoTest() {

    private val dao = ProjectDaoImpl()

    // <editor-fold desc="Get all Projects">
    @Test
    fun `getProjects but none exists, return empty list`() {
        withTables(
            TagsTable,
            LinksTable,
            TagsProjectsPivotTable,
            LinksProjectsPivotTable,
            ProjectsTable
        ) {
            val list = dao.getProjects()
            assertThat(list).isEmpty()
        }
    }

    @Test
    fun `getProjects return the list`() {
        baseTest {
            val list = dao.getProjects()
            assertThat(list).hasSize(4)
        }
    }
    // </editor-fold>

    // <editor-fold desc="Get specific Project by id">
    @Test
    fun `getProject where item exists, return correct Project`() {
        baseTest {
            val project = dao.getProjectById(UUID.fromString("00000000-0000-0000-0000-000000000001"))

            assertThat(project).matches {
                it?.title == "First parent project" &&
                        it.links.count() == 2
            }
        }
    }

    @Test
    fun `getProject where item does not exists, return 'null'`() {
        baseTest {
            val project = dao.getProjectById(UUID.randomUUID())

            assertNull(project)
        }
    }
    // </editor-fold>

    // <editor-fold desc="Create new Project">
    @Test
    fun `insertProject where information is correct, database is storing Project and returning correct content`() {
        baseTest {
            val validProject = givenAValidInsertProjectBody()
            val project = dao.insertProject(validProject)

            assertThat(project).matches {
                it?.title == validProject.title &&
                        it.createdAt == it.updatedAt
            }
        }
    }
    // </editor-fold>

    // <editor-fold desc="Update Project">
    @Test
    fun `updateProject where information is correct, database is storing information and returning the correct content`() {
        baseTest {
            // adding a delay so there is a clear difference between `updatedAt` and `createdAt`
            delay(1000)

            val validUpdateProject = givenAValidUpdateProjectBody()
            val project = dao.updateProject(UUID.fromString("00000000-0000-0000-0000-000000000001"), validUpdateProject)

            assertThat(project).matches {
                it?.title == validUpdateProject.title &&
                        it.createdAt != it.updatedAt &&
                        it.links.count() == 1 &&
                        it.tags.count() == 1
            }
        }
    }

    @Test
    fun `updateProject where information is correct but Project with id does not exist, database does nothing and returns 'null'`() {
        baseTest {
            val validProject = givenAValidUpdateProjectBody()
            val project = dao.updateProject(UUID.randomUUID(), validProject)

            assertNull(project)
        }
    }
    // </editor-fold>

    // <editor-fold desc="Delete Project">
    @Test
    fun `deleteProject for id that exists, return true`() {
        baseTest {
            val deleted = dao.deleteProject(UUID.fromString("00000000-0000-0000-0000-000000000001"))
            assertTrue(deleted)
        }
    }

    @Test
    fun `deleteProject for id that does not exist, return false`() {
        baseTest {
            val deleted = dao.deleteProject(UUID.randomUUID())
            assertFalse(deleted)
        }
    }
    // </editor-fold>

    @SuppressWarnings("LongMethod")
    private fun baseTest(
        test: suspend Transaction.() -> Unit
    ) {
        withTables(
            TagsTable,
            LinksTable,
            TagsProjectsPivotTable,
            LinksProjectsPivotTable,
            ProjectsTable
        ) {
            listOf(
                Tag(UUID.fromString("00000000-0000-0000-0000-000000000001"), name = "First tag", slug = "first-tag"),
                Tag(UUID.fromString("00000000-0000-0000-0000-000000000002"), name = "Second tag", slug = "second-tag"),
                Tag(UUID.fromString("00000000-0000-0000-0000-000000000003"), name = "Third tag", slug = "third-tag"),
            ).forEach { data ->
                TagsTable.insert {
                    it[id] = data.id
                    it[name] = data.name
                    it[slug] = data.slug
                }
            }

            listOf(
                Link(UUID.fromString("00000000-0000-0000-0000-000000000001"), url = "https://www.google.be"),
                Link(UUID.fromString("00000000-0000-0000-0000-000000000002"), url = "https://www.google.be/second"),
                Link(UUID.fromString("00000000-0000-0000-0000-000000000003"), url = "https://www.google.be/third"),
            ).forEach { data ->
                LinksTable.insert {
                    it[id] = data.id
                    it[url] = data.url
                }
            }

            listOf(
                Project(id = UUID.fromString("00000000-0000-0000-0000-000000000001"), title = "First parent project"),
                Project(id = UUID.fromString("00000000-0000-0000-0000-000000000002"), title = "Sub project of First parent project"),
                Project(id = UUID.fromString("00000000-0000-0000-0000-000000000003"), title = "Second parent project"),
                Project(id = UUID.fromString("00000000-0000-0000-0000-000000000004"), title = "Sub project of Sub project of First parent project")
            ).forEach { data ->
                ProjectsTable.insert {
                    it[id] = data.id
                    it[title] = data.title
                    it[description] = data.description
                    it[shortDescription] = data.shortDescription
                    it[imageUrl] = data.imageUrl
                    it[bannerImageUrl] = data.bannerImageUrl
                }
            }

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

            test()
        }
    }
}
