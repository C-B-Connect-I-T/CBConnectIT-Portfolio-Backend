package com.cbconnectit.routing.jobPositions

import com.cbconnectit.data.dto.requests.jobPosition.JobPositionDto
import com.cbconnectit.modules.jobPositions.JobPositionController
import com.cbconnectit.modules.jobPositions.jobPositionRouting
import com.cbconnectit.routing.AuthenticationInstrumentation
import com.cbconnectit.routing.BaseRoutingTest
import com.cbconnectit.routing.jobPositions.JobPositionInstrumentation.givenAJobPosition
import com.cbconnectit.routing.jobPositions.JobPositionInstrumentation.givenAValidInsertJobPosition
import com.cbconnectit.routing.jobPositions.JobPositionInstrumentation.givenAValidUpdateJobPositionBody
import com.cbconnectit.routing.jobPositions.JobPositionInstrumentation.givenJobPositionList
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
class JobPositionRoutingTest : BaseRoutingTest() {

    private val jobPositionController: JobPositionController = mockk()

    @BeforeAll
    fun setup() {
        koinModules = module {
            single { jobPositionController }
        }
        moduleList = {
            install(Routing) {
                jobPositionRouting()
            }
        }
    }

    @BeforeEach
    fun clearMocks() {
        io.mockk.clearMocks(jobPositionController)
    }

    // <editor-fold desc="Get all jobPositions">
    @Test
    fun `when fetching all jobPositions, we return a list`() = withBaseTestApplication(
        AuthenticationInstrumentation()
    ) {
        coEvery { jobPositionController.getJobPositions() } returns givenJobPositionList()

        val call = doCall(HttpMethod.Get, "/job_positions")

        call.also {
            assertThat(HttpStatusCode.OK).isEqualTo(it.response.status())
            val responseBody = it.response.parseBody(List::class.java)
            assertThat(responseBody).hasSize(4)
        }
    }
    // </editor-fold>

    // <editor-fold desc="Get specific jobPosition">
    @Test
    fun `when fetching a specific jobPosition that exists by id, we return that jobPosition`() = withBaseTestApplication(
        AuthenticationInstrumentation()
    ) {
        val jobPositionResponse = givenAJobPosition()
        coEvery { jobPositionController.getJobPositionById(any()) } returns jobPositionResponse

        val call = doCall(HttpMethod.Get, "/job_positions/a63a20c4-14dd-4e11-9e87-5ab361a51f65")

        call.also {
            assertThat(HttpStatusCode.OK).isEqualTo(it.response.status())
            val responseBody = it.response.parseBody(JobPositionDto::class.java)
            assertThat(jobPositionResponse).isEqualTo(responseBody)
        }
    }

    @Test
    fun `when fetching a specific jobPosition by id that does not exists, we return error`() = withBaseTestApplication(
        AuthenticationInstrumentation()
    ) {
        coEvery { jobPositionController.getJobPositionById(any()) } throws Exception()

        val exception = assertThrows<Exception> {
            doCall(HttpMethod.Get, "/job_positions/a63a20c4-14dd-4e11-9e87-5ab361a51f65")
        }

        assertThat(exception.message).isEqualTo(null)
    }
    // </editor-fold>

    // <editor-fold desc="Create new jobPosition">
    @Test
    fun `when creating jobPosition with successful insertion, we return response jobPosition body`() = withBaseTestApplication(
        AuthenticationInstrumentation()
    ) {
        val jobPositionResponse = givenAJobPosition()
        coEvery { jobPositionController.postJobPosition(any()) } returns jobPositionResponse

        val body = toJsonBody(givenAValidInsertJobPosition())
        val call = doCall(HttpMethod.Post, "/job_positions", body)

        call.also {
            assertThat(HttpStatusCode.Created).isEqualTo(it.response.status())
            val responseBody = it.response.parseBody(JobPositionDto::class.java)
            assertThat(jobPositionResponse).isEqualTo(responseBody)
        }
    }

    @Test
    fun `when creating jobPosition already created, we return 409 error`() = withBaseTestApplication(
        AuthenticationInstrumentation()
    ) {
        coEvery { jobPositionController.postJobPosition(any()) } throws ErrorDuplicateEntity

        val body = toJsonBody(givenAValidInsertJobPosition())
        val exception = assertThrows<ErrorDuplicateEntity> {
            doCall(HttpMethod.Post, "/job_positions", body)
        }
        assertThat(exception.message).isEqualTo(null)
    }
    // </editor-fold>

    // <editor-fold desc="Update jobPosition">
    @Test
    fun `when updating jobPosition with successful insertion, we return response jobPosition body`() = withBaseTestApplication(
        AuthenticationInstrumentation()
    ) {
        val jobPositionResponse = givenAJobPosition()
        coEvery { jobPositionController.updateJobPositionById(any(), any()) } returns jobPositionResponse

        val body = toJsonBody(givenAValidUpdateJobPositionBody())
        val call = doCall(HttpMethod.Put, "/job_positions/a63a20c4-14dd-4e11-9e87-5ab361a51f65", body)

        call.also {
            assertThat(HttpStatusCode.OK).isEqualTo(it.response.status())
            val responseBody = it.response.parseBody(JobPositionDto::class.java)
            assertThat(jobPositionResponse).isEqualTo(responseBody)
        }
    }

    @Test
    fun `when updating jobPosition with wrong jobPositionId, we return error`() = withBaseTestApplication(
        AuthenticationInstrumentation()
    ) {
        coEvery { jobPositionController.updateJobPositionById(any(), any()) } throws Exception()

        val body = toJsonBody(givenAValidUpdateJobPositionBody())
        val exception = assertThrows<Exception> {
            doCall(HttpMethod.Put, "/job_positions/a63a20c4-14dd-4e11-9e87-5ab361a51f65", body)
        }
        assertThat(exception.message).isEqualTo(null)
    }
    // </editor-fold>

    // <editor-fold desc="Delete jobPosition">
    @Test
    fun `when deleting jobPosition successful, we return Ok response`() = withBaseTestApplication(
        AuthenticationInstrumentation()
    ) {
        coEvery { jobPositionController.deleteJobPositionById(any()) } returns Unit

        val call = doCall(HttpMethod.Delete, "/job_positions/a63a20c4-14dd-4e11-9e87-5ab361a51f65")

        call.also {
            assertThat(HttpStatusCode.OK).isEqualTo(it.response.status())
        }
    }

    @Test
    fun `when deleting jobPosition with wrong jobPositionId, we return error`() = withBaseTestApplication(
        AuthenticationInstrumentation()
    ) {
        coEvery { jobPositionController.deleteJobPositionById(any()) } throws Exception()

        val exception = assertThrows<Exception> {
            doCall(HttpMethod.Delete, "/job_positions/a63a20c4-14dd-4e11-9e87-5ab361a51f65")
        }
        assertThat(exception.message).isEqualTo(null)
    }
    // </editor-fold>
}