package com.cbconnectit.controllers.projects

import com.cbconnectit.controllers.BaseControllerTest
import com.cbconnectit.controllers.projects.ProjectInstrumentation.givenAProject
import com.cbconnectit.controllers.projects.ProjectInstrumentation.givenAValidInsertProject
import com.cbconnectit.controllers.projects.ProjectInstrumentation.givenAValidUpdateProject
import com.cbconnectit.controllers.projects.ProjectInstrumentation.givenAnInvalidInsertProject
import com.cbconnectit.controllers.projects.ProjectInstrumentation.givenAnInvalidUpdateProject
import com.cbconnectit.data.dto.requests.project.ProjectDto
import com.cbconnectit.domain.interfaces.ILinkDao
import com.cbconnectit.domain.interfaces.IProjectDao
import com.cbconnectit.domain.interfaces.ITagDao
import com.cbconnectit.modules.projects.ProjectController
import com.cbconnectit.modules.projects.ProjectControllerImpl
import com.cbconnectit.statuspages.ErrorFailedCreate
import com.cbconnectit.statuspages.ErrorFailedDelete
import com.cbconnectit.statuspages.ErrorFailedUpdate
import com.cbconnectit.statuspages.ErrorInvalidParameters
import com.cbconnectit.statuspages.ErrorNotFound
import com.cbconnectit.statuspages.ErrorUnknownLinkIdsForCreateProject
import com.cbconnectit.statuspages.ErrorUnknownLinkIdsForUpdateProject
import com.cbconnectit.statuspages.ErrorUnknownTagIdsForCreateProject
import com.cbconnectit.statuspages.ErrorUnknownTagIdsForUpdateProject
import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.koin.dsl.module
import java.util.*
import kotlin.test.assertNotNull

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ProjectControllerTest : BaseControllerTest() {

    private val projectDao: IProjectDao = mockk()
    private val tagDao: ITagDao = mockk()
    private val linkDao: ILinkDao = mockk()
    private val controller: ProjectController by lazy { ProjectControllerImpl() }

    init {
        startInjection(
            module {
                single { projectDao }
                single { tagDao }
                single { linkDao }
            }
        )
    }

    @BeforeEach
    override fun before() {
        super.before()
        clearMocks(projectDao, tagDao, linkDao)
    }

    // <editor-fold desc="Get all projects">
    @Test
    fun `when requesting all projects, we return valid list`() {
        val createdProject = givenAProject()

        coEvery { projectDao.getProjects() } returns listOf(createdProject)

        runBlocking {
            val responseProjects = controller.getProjects()

            assertThat(responseProjects).hasSize(1)
            assertThat(responseProjects).allMatch { it is ProjectDto }
        }
    }
    // </editor-fold>

    // <editor-fold desc="Get specific project">
    @Test
    fun `when requesting specific project by ID, we return valid projectDto`() {
        val createdProject = givenAProject()

        coEvery { projectDao.getProjectById(any() as UUID) } returns createdProject

        runBlocking {
            val responseProject = controller.getProjectById(UUID.randomUUID())

            assertThat(responseProject.title).isEqualTo(createdProject.title)
            assertNotNull(responseProject.createdAt)
            assertNotNull(responseProject.updatedAt)
        }
    }

    @Test
    fun `when requesting specific project by ID where the ID does not exist, we throw exception`() {
        coEvery { projectDao.getProjectById(any() as UUID) } throws ErrorNotFound

        assertThrows<ErrorNotFound> {
            runBlocking { controller.getProjectById(UUID.randomUUID()) }
        }
    }
    // </editor-fold>

    // <editor-fold desc="Create new project">
    @Test
    fun `when creating project with incorrect information, we throw exception`() {
        val postProject = givenAnInvalidInsertProject()

        assertThrows<ErrorInvalidParameters> {
            runBlocking { controller.postProject(postProject) }
        }
    }

    @Test
    fun `when creating project with correct information, we return valid projectDto`() {
        val postProject = givenAValidInsertProject()
        val createdProject = givenAProject()

        coEvery { tagDao.getListOfExistingTagIds(any()) } returns listOf(UUID.fromString("00000000-0000-0000-0000-000000000001"))
        coEvery { linkDao.getListOfExistingLinkIds(any()) } returns listOf(UUID.fromString("00000000-0000-0000-0000-000000000001"))
        coEvery { projectDao.insertProject(any()) } returns createdProject

        runBlocking {
            val responseProject = controller.postProject(postProject)

            assertThat(responseProject.title).isEqualTo(createdProject.title)
        }
    }

    @Test
    fun `when creating project with correct information but tagId does not exist, we throw exception`() {
        val postProject = givenAValidInsertProject()
        val createdProject = givenAProject()

        coEvery { tagDao.getListOfExistingTagIds(any()) } returns listOf()
        coEvery { linkDao.getListOfExistingLinkIds(any()) } returns listOf(UUID.fromString("00000000-0000-0000-0000-000000000001"))
        coEvery { projectDao.insertProject(any()) } returns createdProject

        assertThrows<ErrorUnknownTagIdsForCreateProject> {
            runBlocking { controller.postProject(postProject) }
        }
    }

    @Test
    fun `when creating project with correct information but linkId does not exist, we throw exception`() {
        val postProject = givenAValidInsertProject()
        val createdProject = givenAProject()

        coEvery { tagDao.getListOfExistingTagIds(any()) } returns listOf(UUID.fromString("00000000-0000-0000-0000-000000000001"))
        coEvery { linkDao.getListOfExistingLinkIds(any()) } returns listOf()
        coEvery { projectDao.insertProject(any()) } returns createdProject

        assertThrows<ErrorUnknownLinkIdsForCreateProject> {
            runBlocking { controller.postProject(postProject) }
        }
    }

    @Test
    fun `when creating project and database returns error, we throw exception`() {
        val postProject = givenAValidInsertProject()

        coEvery { tagDao.getListOfExistingTagIds(any()) } returns listOf(UUID.fromString("00000000-0000-0000-0000-000000000001"))
        coEvery { linkDao.getListOfExistingLinkIds(any()) } returns listOf(UUID.fromString("00000000-0000-0000-0000-000000000001"))
        coEvery { projectDao.insertProject(any()) } returns null

        assertThrows<ErrorFailedCreate> {
            runBlocking { controller.postProject(postProject) }
        }
    }
    // </editor-fold>

    // <editor-fold desc="Update specific project">
    @Test
    fun `when updating specific project, we return valid projectDto`() {
        val updateProject = givenAValidUpdateProject()
        val createdProject = givenAProject()

        coEvery { tagDao.getListOfExistingTagIds(any()) } returns listOf(UUID.fromString("00000000-0000-0000-0000-000000000001"))
        coEvery { linkDao.getListOfExistingLinkIds(any()) } returns listOf(UUID.fromString("00000000-0000-0000-0000-000000000001"))
        coEvery { projectDao.updateProject(any(), any()) } returns createdProject

        runBlocking {
            val responseProject = controller.updateProjectById(UUID.randomUUID(), updateProject)

            // Assertion
            assertThat(responseProject.title).isEqualTo(createdProject.title)
        }
    }

    @Test
    fun `when updating specific project but tagId does not exist, we throw exception`() {
        val updateProject = givenAValidUpdateProject()
        val createdProject = givenAProject()

        coEvery { tagDao.getListOfExistingTagIds(any()) } returns listOf()
        coEvery { linkDao.getListOfExistingLinkIds(any()) } returns listOf(UUID.fromString("00000000-0000-0000-0000-000000000001"))
        coEvery { projectDao.updateProject(any(), any()) } returns createdProject

        assertThrows<ErrorUnknownTagIdsForUpdateProject> {
            runBlocking { controller.updateProjectById(UUID.randomUUID(), updateProject) }
        }
    }

    @Test
    fun `when updating specific project but linkId does not exist, we throw exception`() {
        val updateProject = givenAValidUpdateProject()
        val createdProject = givenAProject()

        coEvery { tagDao.getListOfExistingTagIds(any()) } returns listOf(UUID.fromString("00000000-0000-0000-0000-000000000001"))
        coEvery { linkDao.getListOfExistingLinkIds(any()) } returns listOf()
        coEvery { projectDao.updateProject(any(), any()) } returns createdProject

        assertThrows<ErrorUnknownLinkIdsForUpdateProject> {
            runBlocking { controller.updateProjectById(UUID.randomUUID(), updateProject) }
        }
    }

    @Test
    fun `when updating specific project which has invalid data, we throw exception`() {
        val updateProject = givenAnInvalidUpdateProject()

        assertThrows<ErrorInvalidParameters> {
            runBlocking { controller.updateProjectById(UUID.randomUUID(), updateProject) }
        }
    }

    @Test
    fun `when updating specific project which does not exist, we throw exception`() {
        val updateProject = givenAValidUpdateProject()

        coEvery { tagDao.getListOfExistingTagIds(any()) } returns listOf(UUID.fromString("00000000-0000-0000-0000-000000000001"))
        coEvery { linkDao.getListOfExistingLinkIds(any()) } returns listOf(UUID.fromString("00000000-0000-0000-0000-000000000001"))
        coEvery { projectDao.updateProject(any(), any()) } throws ErrorFailedUpdate

        assertThrows<ErrorFailedUpdate> {
            runBlocking { controller.updateProjectById(UUID.randomUUID(), updateProject) }
        }
    }
    // </editor-fold>

    // <editor-fold desc="Delete project">
    @Test
    fun `when deleting specific project, we return valid projectDto`() {
        coEvery { projectDao.deleteProject(any()) } returns true

        assertDoesNotThrow {
            runBlocking {
                controller.deleteProjectById(UUID.randomUUID())
            }
        }
    }

    @Test
    fun `when deleting specific project which does not exist, we throw exception`() {
        coEvery { projectDao.deleteProject(any()) } returns false

        assertThrows<ErrorFailedDelete> {
            runBlocking { controller.deleteProjectById(UUID.randomUUID()) }
        }
    }
    // </editor-fold>
}
