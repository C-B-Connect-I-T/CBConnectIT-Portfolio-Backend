package com.cbconnectit.routing.experiences

import com.cbconnectit.data.dto.requests.experience.ExperienceDto
import com.cbconnectit.modules.experiences.ExperienceController
import com.cbconnectit.modules.experiences.experienceRouting
import com.cbconnectit.routing.AuthenticationInstrumentation
import com.cbconnectit.routing.BaseRoutingTest
import com.cbconnectit.routing.experiences.ExperienceInstrumentation.givenAExperience
import com.cbconnectit.routing.experiences.ExperienceInstrumentation.givenAValidInsertExperience
import com.cbconnectit.routing.experiences.ExperienceInstrumentation.givenAValidUpdateExperienceBody
import com.cbconnectit.routing.experiences.ExperienceInstrumentation.givenExperienceList
import com.cbconnectit.statuspages.ErrorDuplicateEntity
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*
import org.koin.dsl.module

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ExperienceRoutingTest: BaseRoutingTest() {

    private val experienceController: ExperienceController = mockk()

    @BeforeAll
    fun setup() {
        koinModules = module {
            single { experienceController }
        }
        moduleList = {
            install(Routing) {
                experienceRouting()
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
        coEvery { experienceController.getExperiences() } returns givenExperienceList()

        val call = doCall(HttpMethod.Get, "/experiences")

        call.also {
            assertThat(HttpStatusCode.OK).isEqualTo(it.response.status())
            val responseBody = it.response.parseBody(List::class.java)
            assertThat(responseBody).hasSize(4)
        }
    }
    // </editor-fold>

    // <editor-fold desc="Get specific experience">
    @Test
    fun `when fetching a specific experience that exists by id, we return that experience`() = withBaseTestApplication(
        AuthenticationInstrumentation()
    ) {
        val experienceResponse = givenAExperience()
        coEvery { experienceController.getExperienceById(any()) } returns experienceResponse

        val call = doCall(HttpMethod.Get, "/experiences/a63a20c4-14dd-4e11-9e87-5ab361a51f65")

        call.also {
            assertThat(HttpStatusCode.OK).isEqualTo(it.response.status())
            val responseBody = it.response.parseBody(ExperienceDto::class.java)
            assertThat(experienceResponse).isEqualTo(responseBody)
        }
    }

    @Test
    fun `when fetching a specific experience by id that does not exists, we return error`() = withBaseTestApplication(
        AuthenticationInstrumentation()
    ) {
        coEvery { experienceController.getExperienceById(any()) } throws Exception()

        val exception = assertThrows<Exception>{
            doCall(HttpMethod.Get, "/experiences/a63a20c4-14dd-4e11-9e87-5ab361a51f65")
        }

        assertThat(exception.message).isEqualTo(null)
    }
    // </editor-fold>

    // <editor-fold desc="Create new experience">
    @Test
    fun `when creating experience with successful insertion, we return response experience body`() = withBaseTestApplication(
        AuthenticationInstrumentation()
    ) {
        val experienceResponse = givenAExperience()
        coEvery { experienceController.postExperience(any()) } returns experienceResponse

        val body = toJsonBody(givenAValidInsertExperience())
        val call = doCall(HttpMethod.Post, "/experiences", body)

        call.also {
            assertThat(HttpStatusCode.Created).isEqualTo(it.response.status())
            val responseBody = it.response.parseBody(ExperienceDto::class.java)
            assertThat(experienceResponse).isEqualTo(responseBody)
        }
    }

    @Test
    fun `when creating experience already created, we return 409 error`() = withBaseTestApplication(
        AuthenticationInstrumentation()
    ) {
        coEvery { experienceController.postExperience(any()) } throws ErrorDuplicateEntity

        val body = toJsonBody(givenAValidInsertExperience())
        val exception = assertThrows<ErrorDuplicateEntity> {
            doCall(HttpMethod.Post, "/experiences", body)
        }
        assertThat(exception.message).isEqualTo(null)
    }
    // </editor-fold>

    // <editor-fold desc="Update experience">
    @Test
    fun `when updating experience with successful insertion, we return response experience body`() = withBaseTestApplication(
        AuthenticationInstrumentation()
    ) {
        val experienceResponse = givenAExperience()
        coEvery { experienceController.updateExperienceById(any(), any()) } returns experienceResponse

        val body = toJsonBody(givenAValidUpdateExperienceBody())
        val call = doCall(HttpMethod.Put, "/experiences/a63a20c4-14dd-4e11-9e87-5ab361a51f65", body)

        call.also {
            assertThat(HttpStatusCode.OK).isEqualTo(it.response.status())
            val responseBody = it.response.parseBody(ExperienceDto::class.java)
            assertThat(experienceResponse).isEqualTo(responseBody)
        }
    }

    @Test
    fun `when updating experience with wrong experienceId, we return error`() = withBaseTestApplication(
        AuthenticationInstrumentation()
    ) {
        coEvery { experienceController.updateExperienceById(any(), any()) } throws Exception()

        val body = toJsonBody(givenAValidUpdateExperienceBody())
        val exception = assertThrows<Exception> {
            doCall(HttpMethod.Put, "/experiences/a63a20c4-14dd-4e11-9e87-5ab361a51f65", body)
        }
        assertThat(exception.message).isEqualTo(null)
    }
    // </editor-fold>

    // <editor-fold desc="Delete experience">
    @Test
    fun `when deleting experience successful, we return Ok response`() = withBaseTestApplication(
        AuthenticationInstrumentation()
    ) {
        coEvery { experienceController.deleteExperienceById(any()) } returns Unit

        val call = doCall(HttpMethod.Delete, "/experiences/a63a20c4-14dd-4e11-9e87-5ab361a51f65")

        call.also {
            assertThat(HttpStatusCode.OK).isEqualTo(it.response.status())
        }
    }

    @Test
    fun `when deleting experience with wrong experienceId, we return error`() = withBaseTestApplication(
        AuthenticationInstrumentation()
    ) {
        coEvery { experienceController.deleteExperienceById(any()) } throws Exception()

        val exception = assertThrows<Exception> {
            doCall(HttpMethod.Delete, "/experiences/a63a20c4-14dd-4e11-9e87-5ab361a51f65")
        }
        assertThat(exception.message).isEqualTo(null)
    }
    // </editor-fold>
}