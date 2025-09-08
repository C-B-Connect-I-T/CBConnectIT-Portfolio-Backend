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
class JobPositionRoutingTest : BaseRoutingTest() {

    private val jobPositionController: JobPositionController = mockk()

    @BeforeAll
    fun setup() {
        koinModules = module {
            single { jobPositionController }
        }
        moduleList = {
            routing {
                jobPositionRouting(jobPositionController)
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

        val response = doCall(HttpMethod.Get, "/job_positions")

        assertThat(response.status).isEqualTo(HttpStatusCode.OK)
        assertThat(response.parseBody<List<*>>()).hasSize(4)
    }
    // </editor-fold>

    // <editor-fold desc="Get specific jobPosition">
    @Test
    fun `when fetching a specific jobPosition that exists by id, we return that jobPosition`() = withBaseTestApplication(
        AuthenticationInstrumentation()
    ) {
        val jobPositionResponse = givenAJobPosition()
        coEvery { jobPositionController.getJobPositionById(any()) } returns jobPositionResponse

        val response = doCall(HttpMethod.Get, "/job_positions/a63a20c4-14dd-4e11-9e87-5ab361a51f65")

        assertThat(response.status).isEqualTo(HttpStatusCode.OK)
        assertThat(response.parseBody<JobPositionDto>()).isEqualTo(jobPositionResponse)
    }

    @Test
    fun `when fetching a specific jobPosition by id that does not exists, we return error`() = withBaseTestApplication(
        AuthenticationInstrumentation()
    ) {
        val exception = ErrorNotFound
        coEvery { jobPositionController.getJobPositionById(any()) } throws exception

        val response = doCall(HttpMethod.Get, "/job_positions/a63a20c4-14dd-4e11-9e87-5ab361a51f65")

        assertThat(response.status).isEqualTo(exception.statusCode)
        assertThat(response.parseBody<ErrorResponse>()).isEqualTo(exception.toErrorResponse())
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
        val response = doCall(HttpMethod.Post, "/job_positions", body)

        assertThat(response.status).isEqualTo(HttpStatusCode.Created)
        assertThat(response.parseBody<JobPositionDto>()).isEqualTo(jobPositionResponse)
    }

    @Test
    fun `when creating jobPosition already created, we return 409 error`() = withBaseTestApplication(
        AuthenticationInstrumentation()
    ) {
        val exception = ErrorDuplicateEntity
        coEvery { jobPositionController.postJobPosition(any()) } throws exception

        val body = toJsonBody(givenAValidInsertJobPosition())
        val response = doCall(HttpMethod.Post, "/job_positions", body)

        assertThat(response.status).isEqualTo(exception.statusCode)
        assertThat(response.parseBody<ErrorResponse>()).isEqualTo(exception.toErrorResponse())
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
        val response = doCall(HttpMethod.Put, "/job_positions/a63a20c4-14dd-4e11-9e87-5ab361a51f65", body)

        assertThat(response.status).isEqualTo(HttpStatusCode.OK)
        assertThat(response.parseBody<JobPositionDto>()).isEqualTo(jobPositionResponse)
    }

    @Test
    fun `when updating jobPosition with wrong jobPositionId, we return error`() = withBaseTestApplication(
        AuthenticationInstrumentation()
    ) {
        val exception = ErrorNotFound
        coEvery { jobPositionController.updateJobPositionById(any(), any()) } throws exception

        val body = toJsonBody(givenAValidUpdateJobPositionBody())
        val response = doCall(HttpMethod.Put, "/job_positions/a63a20c4-14dd-4e11-9e87-5ab361a51f65", body)

        assertThat(response.status).isEqualTo(exception.statusCode)
        assertThat(response.parseBody<ErrorResponse>()).isEqualTo(exception.toErrorResponse())
    }
    // </editor-fold>

    // <editor-fold desc="Delete jobPosition">
    @Test
    fun `when deleting jobPosition successful, we return Ok response`() = withBaseTestApplication(
        AuthenticationInstrumentation()
    ) {
        coEvery { jobPositionController.deleteJobPositionById(any()) } returns Unit

        val response = doCall(HttpMethod.Delete, "/job_positions/a63a20c4-14dd-4e11-9e87-5ab361a51f65")

        assertThat(response.status).isEqualTo(HttpStatusCode.OK)
    }

    @Test
    fun `when deleting jobPosition with wrong jobPositionId, we return error`() = withBaseTestApplication(
        AuthenticationInstrumentation()
    ) {
        val exception = ErrorFailedDelete
        coEvery { jobPositionController.deleteJobPositionById(any()) } throws exception

        val response = doCall(HttpMethod.Delete, "/job_positions/a63a20c4-14dd-4e11-9e87-5ab361a51f65")

        assertThat(response.status).isEqualTo(exception.statusCode)
        assertThat(response.parseBody<ErrorResponse>()).isEqualTo(exception.toErrorResponse())
    }
    // </editor-fold>
}
