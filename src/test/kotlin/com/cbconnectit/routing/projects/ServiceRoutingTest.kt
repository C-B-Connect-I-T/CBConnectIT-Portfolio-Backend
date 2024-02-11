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
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.mockk.coEvery
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*
import org.koin.dsl.module

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ProjectRoutingTest: BaseRoutingTest() {

    private val projectController: ProjectController = mockk()

    @BeforeAll
    fun setup() {
        koinModules = module {
            single { projectController }
        }
        moduleList = {
            install(Routing) {
                projectRouting()
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

        val call = doCall(HttpMethod.Get, "/projects")

        call.also {
            assertThat(HttpStatusCode.OK).isEqualTo(it.response.status())
            val responseBody = it.response.parseBody(List::class.java)
            assertThat(responseBody).hasSize(4)
        }
    }
    // </editor-fold>

    // <editor-fold desc="Get specific project">
    @Test
    fun `when fetching a specific project that exists by id, we return that project`() = withBaseTestApplication(
        AuthenticationInstrumentation()
    ) {
        val projectResponse = givenAProject()
        coEvery { projectController.getProjectById(any()) } returns projectResponse

        val call = doCall(HttpMethod.Get, "/projects/a63a20c4-14dd-4e11-9e87-5ab361a51f65")

        call.also {
            assertThat(HttpStatusCode.OK).isEqualTo(it.response.status())
            val responseBody = it.response.parseBody(ProjectDto::class.java)
            assertThat(projectResponse).isEqualTo(responseBody)
        }
    }

    @Test
    fun `when fetching a specific project by id that does not exists, we return error`() = withBaseTestApplication(
        AuthenticationInstrumentation()
    ) {
        coEvery { projectController.getProjectById(any()) } throws Exception()

        val exception = assertThrows<Exception>{
            doCall(HttpMethod.Get, "/projects/a63a20c4-14dd-4e11-9e87-5ab361a51f65")
        }

        assertThat(exception.message).isEqualTo(null)
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
        val call = doCall(HttpMethod.Post, "/projects", body)

        call.also {
            assertThat(HttpStatusCode.Created).isEqualTo(it.response.status())
            val responseBody = it.response.parseBody(ProjectDto::class.java)
            assertThat(projectResponse).isEqualTo(responseBody)
        }
    }

    @Test
    fun `when creating project already created, we return 409 error`() = withBaseTestApplication(
        AuthenticationInstrumentation()
    ) {
        coEvery { projectController.postProject(any()) } throws ErrorDuplicateEntity

        val body = toJsonBody(givenAValidInsertProject())
        val exception = assertThrows<ErrorDuplicateEntity> {
            doCall(HttpMethod.Post, "/projects", body)
        }
        assertThat(exception.message).isEqualTo(null)
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
        val call = doCall(HttpMethod.Put, "/projects/a63a20c4-14dd-4e11-9e87-5ab361a51f65", body)

        call.also {
            assertThat(HttpStatusCode.OK).isEqualTo(it.response.status())
            val responseBody = it.response.parseBody(ProjectDto::class.java)
            assertThat(projectResponse).isEqualTo(responseBody)
        }
    }

    @Test
    fun `when updating project with wrong projectId, we return error`() = withBaseTestApplication(
        AuthenticationInstrumentation()
    ) {
        coEvery { projectController.updateProjectById(any(), any()) } throws Exception()

        val body = toJsonBody(givenAValidUpdateProjectBody())
        val exception = assertThrows<Exception> {
            doCall(HttpMethod.Put, "/projects/a63a20c4-14dd-4e11-9e87-5ab361a51f65", body)
        }
        assertThat(exception.message).isEqualTo(null)
    }
    // </editor-fold>

    // <editor-fold desc="Delete project">
    @Test
    fun `when deleting project successful, we return Ok response`() = withBaseTestApplication(
        AuthenticationInstrumentation()
    ) {
        coEvery { projectController.deleteProjectById(any()) } returns Unit

        val call = doCall(HttpMethod.Delete, "/projects/a63a20c4-14dd-4e11-9e87-5ab361a51f65")

        call.also {
            assertThat(HttpStatusCode.OK).isEqualTo(it.response.status())
        }
    }

    @Test
    fun `when deleting project with wrong projectId, we return error`() = withBaseTestApplication(
        AuthenticationInstrumentation()
    ) {
        coEvery { projectController.deleteProjectById(any()) } throws Exception()

        val exception = assertThrows<Exception> {
            doCall(HttpMethod.Delete, "/projects/a63a20c4-14dd-4e11-9e87-5ab361a51f65")
        }
        assertThat(exception.message).isEqualTo(null)
    }
    // </editor-fold>
}