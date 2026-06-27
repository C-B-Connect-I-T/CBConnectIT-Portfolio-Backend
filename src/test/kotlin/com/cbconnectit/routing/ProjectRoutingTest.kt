package com.cbconnectit.routing

import com.cbconnectit.data.dto.requests.project.ProjectDto
import com.cbconnectit.domain.models.project.toDto
import com.cbconnectit.instrumentation.ProjectInstrumentation.givenAProject
import com.cbconnectit.instrumentation.ProjectInstrumentation.givenAValidInsertProject
import com.cbconnectit.instrumentation.ProjectInstrumentation.givenAValidUpdateProject
import com.cbconnectit.instrumentation.ProjectInstrumentation.givenProjectList
import com.cbconnectit.modules.projects.ProjectController
import com.cbconnectit.modules.projects.projectRouting
import com.cbconnectit.plugins.statuspages.ErrorDuplicateEntity
import com.cbconnectit.plugins.statuspages.ErrorFailedDelete
import com.cbconnectit.plugins.statuspages.ErrorNotFound
import com.cbconnectit.plugins.statuspages.ErrorResponse
import com.cbconnectit.plugins.statuspages.toErrorResponse
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
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
            single { json }
        }
        moduleList = {
            routing {
                projectRouting(json, projectController)
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
        coEvery { projectController.getProjects() } returns givenProjectList().map { it.toDto() }

        val response = doCall(HttpMethod.Get, "/projects")

        assertThat(response.status).isEqualTo(HttpStatusCode.OK)
        assertThat(response.parseBody<List<ProjectDto>>()).hasSize(4)
    }
    // </editor-fold>

    // <editor-fold desc="Get specific project">
    @Test
    fun `when fetching a specific project that exists by id, we return that project`() = withBaseTestApplication(
        AuthenticationInstrumentation()
    ) {
        val projectResponse = givenAProject().toDto()
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
    fun `when creating project with multipart and successful insertion, we return response project body`() = withBaseTestApplication(
        AuthenticationInstrumentation()
    ) {
        val projectResponse = givenAProject().toDto()
        coEvery { projectController.postProject(any(), any(), any()) } returns projectResponse

        val response = client.post("/projects") {
            header(HttpHeaders.Authorization, "Bearer ${buildBearerToken()}")
            header("X-Client-Type", "web")
            setBody(
                MultiPartFormDataContent(
                    formData {
                        append("payload", toJsonBody(givenAValidInsertProject()))
                        append("image", ByteArray(100), Headers.build {
                            append(HttpHeaders.ContentType, "image/jpeg")
                            append(HttpHeaders.ContentDisposition, "filename=\"image.jpg\"")
                        })
                        append("bannerImage", ByteArray(100), Headers.build {
                            append(HttpHeaders.ContentType, "image/jpeg")
                            append(HttpHeaders.ContentDisposition, "filename=\"banner.jpg\"")
                        })
                    }
                )
            )
        }

        assertThat(response.status).isEqualTo(HttpStatusCode.Created)
        assertThat(response.parseBody<ProjectDto>()).isEqualTo(projectResponse)
    }

    @Test
    fun `when creating project without image files in multipart, we return missing required media error`() = withBaseTestApplication(
        AuthenticationInstrumentation()
    ) {
        val response = client.post("/projects") {
            header(HttpHeaders.Authorization, "Bearer ${buildBearerToken()}")
            header("X-Client-Type", "web")
            setBody(
                MultiPartFormDataContent(
                    formData {
                        append("payload", toJsonBody(givenAValidInsertProject()))
                    }
                )
            )
        }

        assertThat(response.status).isEqualTo(HttpStatusCode.BadRequest)
    }

    @Test
    fun `when creating project already created, we return 409 error`() = withBaseTestApplication(
        AuthenticationInstrumentation()
    ) {
        val exception = ErrorDuplicateEntity
        coEvery { projectController.postProject(any(), any(), any()) } throws exception

        val response = client.post("/projects") {
            header(HttpHeaders.Authorization, "Bearer ${buildBearerToken()}")
            header("X-Client-Type", "web")
            setBody(
                MultiPartFormDataContent(
                    formData {
                        append("payload", toJsonBody(givenAValidInsertProject()))
                        append("image", ByteArray(100), Headers.build {
                            append(HttpHeaders.ContentType, "image/jpeg")
                            append(HttpHeaders.ContentDisposition, "filename=\"image.jpg\"")
                        })
                        append("bannerImage", ByteArray(100), Headers.build {
                            append(HttpHeaders.ContentType, "image/jpeg")
                            append(HttpHeaders.ContentDisposition, "filename=\"banner.jpg\"")
                        })
                    }
                )
            )
        }

        assertThat(response.status).isEqualTo(exception.statusCode)
        assertThat(response.parseBody<ErrorResponse>()).isEqualTo(exception.toErrorResponse())
    }
    // </editor-fold>

    // <editor-fold desc="Update project">
    @Test
    fun `when updating project with successful update via JSON, we return response project body`() = withBaseTestApplication(
        AuthenticationInstrumentation()
    ) {
        val projectResponse = givenAProject().toDto()
        coEvery { projectController.updateProjectById(any(), any(), any(), any()) } returns projectResponse

        val body = toJsonBody(givenAValidUpdateProject())
        val response = doCall(HttpMethod.Put, "/projects/a63a20c4-14dd-4e11-9e87-5ab361a51f65", body)

        assertThat(response.status).isEqualTo(HttpStatusCode.OK)
        assertThat(response.parseBody<ProjectDto>()).isEqualTo(projectResponse)
    }

    @Test
    fun `when updating project with wrong projectId, we return error`() = withBaseTestApplication(
        AuthenticationInstrumentation()
    ) {
        val exception = ErrorNotFound
        coEvery { projectController.updateProjectById(any(), any(), any(), any()) } throws exception

        val body = toJsonBody(givenAValidUpdateProject())
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
