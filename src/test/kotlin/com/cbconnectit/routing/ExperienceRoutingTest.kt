package com.cbconnectit.routing

import com.cbconnectit.data.dto.requests.experience.ExperienceDto
import com.cbconnectit.domain.models.experience.toDto
import com.cbconnectit.instrumentation.ExperienceInstrumentation.givenAExperience
import com.cbconnectit.instrumentation.ExperienceInstrumentation.givenAValidInsertExperience
import com.cbconnectit.instrumentation.ExperienceInstrumentation.givenAValidUpdateExperience
import com.cbconnectit.instrumentation.ExperienceInstrumentation.givenExperienceList
import com.cbconnectit.modules.experiences.ExperienceController
import com.cbconnectit.modules.experiences.experienceRouting
import com.cbconnectit.plugins.statuspages.ErrorDuplicateEntity
import com.cbconnectit.plugins.statuspages.ErrorFailedDelete
import com.cbconnectit.plugins.statuspages.ErrorNotFound
import com.cbconnectit.plugins.statuspages.ErrorResponse
import com.cbconnectit.plugins.statuspages.toErrorResponse
import io.ktor.http.*
import io.ktor.server.routing.*
import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.koin.dsl.module

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ExperienceRoutingTest : BaseRoutingTest() {

    private val experienceController: ExperienceController = mockk()

    @BeforeAll
    fun setup() {
        koinModules = module {
            single { experienceController }
        }
        moduleList = {
            routing {
                experienceRouting(experienceController)
            }
        }
    }

    @BeforeEach
    fun clearMocks() {
        clearMocks(experienceController)
    }

    // <editor-fold desc="Get all experiences">
    @Test
    fun `when fetching all experiences, we return a list`() = withBaseTestApplication(
        AuthenticationInstrumentation()
    ) {
        coEvery { experienceController.getExperiences() } returns givenExperienceList().map { it.toDto() }

        val response = doCall(HttpMethod.Get, "/experiences")

        assertThat(response.status).isEqualTo(HttpStatusCode.OK)
        assertThat(response.parseBody<List<ExperienceDto>>()).hasSize(4)
    }
    // </editor-fold>

    // <editor-fold desc="Get specific experience">
    @Test
    fun `when fetching a specific experience that exists by id, we return that experience`() = withBaseTestApplication(
        AuthenticationInstrumentation()
    ) {
        val experienceResponse = givenAExperience().toDto()
        coEvery { experienceController.getExperienceById(any()) } returns experienceResponse

        val response = doCall(HttpMethod.Get, "/experiences/a63a20c4-14dd-4e11-9e87-5ab361a51f65")

        assertThat(response.status).isEqualTo(HttpStatusCode.OK)
        assertThat(response.parseBody<ExperienceDto>()).isEqualTo(experienceResponse)
    }

    @Test
    fun `when fetching a specific experience by id that does not exists, we return error`() = withBaseTestApplication(
        AuthenticationInstrumentation()
    ) {
        val exception = ErrorNotFound
        coEvery { experienceController.getExperienceById(any()) } throws exception

        val response = doCall(HttpMethod.Get, "/experiences/a63a20c4-14dd-4e11-9e87-5ab361a51f65")

        assertThat(response.status).isEqualTo(exception.statusCode)
        assertThat(response.parseBody<ErrorResponse>()).isEqualTo(exception.toErrorResponse())
    }
    // </editor-fold>

    // <editor-fold desc="Create new experience">
    @Test
    fun `when creating experience with successful insertion, we return response experience body`() = withBaseTestApplication(
        AuthenticationInstrumentation()
    ) {
        val experienceResponse = givenAExperience().toDto()
        coEvery { experienceController.postExperience(any()) } returns experienceResponse

        val body = toJsonBody(givenAValidInsertExperience())
        val response = doCall(HttpMethod.Post, "/experiences", body)

        assertThat(response.status).isEqualTo(HttpStatusCode.Created)
        assertThat(response.parseBody<ExperienceDto>()).isEqualTo(experienceResponse)
    }

    @Test
    fun `when creating experience already created, we return 409 error`() = withBaseTestApplication(
        AuthenticationInstrumentation()
    ) {
        val exception = ErrorDuplicateEntity
        coEvery { experienceController.postExperience(any()) } throws exception

        val body = toJsonBody(givenAValidInsertExperience())
        val response = doCall(HttpMethod.Post, "/experiences", body)

        assertThat(response.status).isEqualTo(exception.statusCode)
        assertThat(response.parseBody<ErrorResponse>()).isEqualTo(exception.toErrorResponse())
    }
    // </editor-fold>

    // <editor-fold desc="Update experience">
    @Test
    fun `when updating experience with successful insertion, we return response experience body`() = withBaseTestApplication(
        AuthenticationInstrumentation()
    ) {
        val experienceResponse = givenAExperience().toDto()
        coEvery { experienceController.updateExperienceById(any(), any()) } returns experienceResponse

        val body = toJsonBody(givenAValidUpdateExperience())
        val response = doCall(HttpMethod.Put, "/experiences/a63a20c4-14dd-4e11-9e87-5ab361a51f65", body)

        assertThat(response.status).isEqualTo(HttpStatusCode.OK)
        assertThat(response.parseBody<ExperienceDto>()).isEqualTo(experienceResponse)
    }

    @Test
    fun `when updating experience with wrong experienceId, we return error`() = withBaseTestApplication(
        AuthenticationInstrumentation()
    ) {
        val exception = ErrorNotFound
        coEvery { experienceController.updateExperienceById(any(), any()) } throws exception

        val body = toJsonBody(givenAValidUpdateExperience())
        val response = doCall(HttpMethod.Put, "/experiences/a63a20c4-14dd-4e11-9e87-5ab361a51f65", body)

        assertThat(response.status).isEqualTo(exception.statusCode)
        assertThat(response.parseBody<ErrorResponse>()).isEqualTo(exception.toErrorResponse())
    }
    // </editor-fold>

    // <editor-fold desc="Delete experience">
    @Test
    fun `when deleting experience successful, we return Ok response`() = withBaseTestApplication(
        AuthenticationInstrumentation()
    ) {
        coEvery { experienceController.deleteExperienceById(any()) } returns Unit

        val response = doCall(HttpMethod.Delete, "/experiences/a63a20c4-14dd-4e11-9e87-5ab361a51f65")

        assertThat(response.status).isEqualTo(HttpStatusCode.OK)
    }

    @Test
    fun `when deleting experience with wrong experienceId, we return error`() = withBaseTestApplication(
        AuthenticationInstrumentation()
    ) {
        val exception = ErrorFailedDelete
        coEvery { experienceController.deleteExperienceById(any()) } throws exception

        val response = doCall(HttpMethod.Delete, "/experiences/a63a20c4-14dd-4e11-9e87-5ab361a51f65")

        assertThat(response.status).isEqualTo(exception.statusCode)
        assertThat(response.parseBody<ErrorResponse>()).isEqualTo(exception.toErrorResponse())
    }
    // </editor-fold>
}
