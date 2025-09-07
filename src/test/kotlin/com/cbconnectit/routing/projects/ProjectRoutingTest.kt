package com.cbconnectit.routing.projects

import com.cbconnectit.data.dto.requests.project.ProjectDto
import com.cbconnectit.modules.projects.ProjectController
import com.cbconnectit.modules.projects.projectRouting
import com.cbconnectit.routing.AuthenticationInstrumentation
import com.cbconnectit.routing.BaseRoutingTest
import com.cbconnectit.routing.projects.ProjectInstrumentation.givenAProject
import com.cbconnectit.routing.projects.ProjectInstrumentation.givenAValidInsertProject
import com.cbconnectit.routing.projects.ProjectInstrumentation.givenAValidUpdateProjectBody
import com.cbconnectit.routing.projects.ProjectInstrumentation.givenProjectList
import com.cbconnectit.statuspages.ErrorDuplicateEntity
import com.cbconnectit.statuspages.ErrorFailedDelete
import com.cbconnectit.statuspages.ErrorNotFound
import com.cbconnectit.statuspages.ErrorResponse
import com.cbconnectit.statuspages.toErrorResponse
import io.ktor.http.*
import io.ktor.server.routing.*
import io.mockk.coEvery
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.koin.dsl.module

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ProjectRoutingTest : BaseRoutingTest() {

    private val projectController: ProjectController = mockk()

    @BeforeAll
    fun setup() {
        koinModules = module {
            single { projectController }
        }
        moduleList = {
            routing {
                projectRouting(projectController)
            }
        }
    }

    @BeforeEach
    fun clearMocks() {
        io.mockk.clearMocks(projectController)
    }

    // <editor-fold desc="Get all projects">
    @Test
    fun `when fetching all projects, we return a list`() = withBaseTestApplication(
        AuthenticationInstrumentation()
    ) {
        coEvery { projectController.getProjects() } returns givenProjectList()

        val response = doCall(HttpMethod.Get, "/projects")

        assertThat(response.status).isEqualTo(HttpStatusCode.OK)
        assertThat(response.parseBody<List<*>>()).hasSize(4)
    }
    // </editor-fold>

    // <editor-fold desc="Get specific project">
    @Test
    fun `when fetching a specific project that exists by id, we return that project`() = withBaseTestApplication(
        AuthenticationInstrumentation()
    ) {
        val projectResponse = givenAProject()
        coEvery { projectController.getProjectById(any()) } returns projectResponse

        val response = doCall(HttpMethod.Get, "/projects/a63a20c4-14dd-4e11-9e87-5ab361a51f65")

        assertThat(response.status).isEqualTo(HttpStatusCode.OK)
        assertThat(response.parseBody<ProjectDto>()).isEqualTo(projectResponse)
    }

    @Test
    fun `when fetching a specific project by id that does not exists, we return error`() = withBaseTestApplication(
        AuthenticationInstrumentation()
    ) {
        val exception = ErrorNotFound
        coEvery { projectController.getProjectById(any()) } throws exception

        val response = doCall(HttpMethod.Get, "/projects/a63a20c4-14dd-4e11-9e87-5ab361a51f65")

        assertThat(response.status).isEqualTo(exception.statusCode)
        assertThat(response.parseBody<ErrorResponse>()).isEqualTo(exception.toErrorResponse())
    }
    // </editor-fold>

    // <editor-fold desc="Create new project">
    @Test
    fun `when creating project with successful insertion, we return response project body`() = withBaseTestApplication(
        AuthenticationInstrumentation()
    ) {
        val projectResponse = givenAProject()
        coEvery { projectController.postProject(any()) } returns projectResponse

        val body = toJsonBody(givenAValidInsertProject())
        val response = doCall(HttpMethod.Post, "/projects", body)

        assertThat(response.status).isEqualTo(HttpStatusCode.Created)
        assertThat(response.parseBody<ProjectDto>()).isEqualTo(projectResponse)
    }

    @Test
    fun `when creating project already created, we return 409 error`() = withBaseTestApplication(
        AuthenticationInstrumentation()
    ) {
        val exception = ErrorDuplicateEntity
        coEvery { projectController.postProject(any()) } throws exception

        val body = toJsonBody(givenAValidInsertProject())
        val response = doCall(HttpMethod.Post, "/projects", body)

        assertThat(response.status).isEqualTo(exception.statusCode)
        assertThat(response.parseBody<ErrorResponse>()).isEqualTo(exception.toErrorResponse())
    }
    // </editor-fold>

    // <editor-fold desc="Update project">
    @Test
    fun `when updating project with successful insertion, we return response project body`() = withBaseTestApplication(
        AuthenticationInstrumentation()
    ) {
        val projectResponse = givenAProject()
        coEvery { projectController.updateProjectById(any(), any()) } returns projectResponse

        val body = toJsonBody(givenAValidUpdateProjectBody())
        val response = doCall(HttpMethod.Put, "/projects/a63a20c4-14dd-4e11-9e87-5ab361a51f65", body)

        assertThat(response.status).isEqualTo(HttpStatusCode.OK)
        assertThat(response.parseBody<ProjectDto>()).isEqualTo(projectResponse)
    }

    @Test
    fun `when updating project with wrong projectId, we return error`() = withBaseTestApplication(
        AuthenticationInstrumentation()
    ) {
        val exception = ErrorNotFound
        coEvery { projectController.updateProjectById(any(), any()) } throws exception

        val body = toJsonBody(givenAValidUpdateProjectBody())
        val response = doCall(HttpMethod.Put, "/projects/a63a20c4-14dd-4e11-9e87-5ab361a51f65", body)

        assertThat(response.status).isEqualTo(exception.statusCode)
        assertThat(response.parseBody<ErrorResponse>()).isEqualTo(exception.toErrorResponse())
    }
    // </editor-fold>

    // <editor-fold desc="Delete project">
    @Test
    fun `when deleting project successful, we return Ok response`() = withBaseTestApplication(
        AuthenticationInstrumentation()
    ) {
        coEvery { projectController.deleteProjectById(any()) } returns Unit

        val response = doCall(HttpMethod.Delete, "/projects/a63a20c4-14dd-4e11-9e87-5ab361a51f65")

        assertThat(response.status).isEqualTo(HttpStatusCode.OK)
    }

    @Test
    fun `when deleting project with wrong projectId, we return error`() = withBaseTestApplication(
        AuthenticationInstrumentation()
    ) {
        val exception = ErrorFailedDelete
        coEvery { projectController.deleteProjectById(any()) } throws exception

        val response = doCall(HttpMethod.Delete, "/projects/a63a20c4-14dd-4e11-9e87-5ab361a51f65")

        assertThat(response.status).isEqualTo(exception.statusCode)
        assertThat(response.parseBody<ErrorResponse>()).isEqualTo(exception.toErrorResponse())
    }
    // </editor-fold>
}
