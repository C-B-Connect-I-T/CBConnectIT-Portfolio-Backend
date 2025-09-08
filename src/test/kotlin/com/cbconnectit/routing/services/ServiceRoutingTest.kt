package com.cbconnectit.routing.services

import com.cbconnectit.data.dto.requests.service.ServiceDto
import com.cbconnectit.modules.services.ServiceController
import com.cbconnectit.modules.services.serviceRouting
import com.cbconnectit.routing.AuthenticationInstrumentation
import com.cbconnectit.routing.BaseRoutingTest
import com.cbconnectit.routing.services.ServiceInstrumentation.givenAService
import com.cbconnectit.routing.services.ServiceInstrumentation.givenAValidInsertService
import com.cbconnectit.routing.services.ServiceInstrumentation.givenAValidUpdateServiceBody
import com.cbconnectit.routing.services.ServiceInstrumentation.givenServiceList
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
class ServiceRoutingTest : BaseRoutingTest() {

    private val serviceController: ServiceController = mockk()

    @BeforeAll
    fun setup() {
        koinModules = module {
            single { serviceController }
        }
        moduleList = {
            routing {
                serviceRouting(serviceController)
            }
        }
    }

    @BeforeEach
    fun clearMocks() {
        io.mockk.clearMocks(serviceController)
    }

    // <editor-fold desc="Get all services">
    @Test
    fun `when fetching all services, we return a list`() = withBaseTestApplication(
        AuthenticationInstrumentation()
    ) {
        coEvery { serviceController.getServices() } returns givenServiceList()

        val response = doCall(HttpMethod.Get, "/services")

        assertThat(response.status).isEqualTo(HttpStatusCode.OK)
        assertThat(response.parseBody<List<*>>()).hasSize(4)
    }
    // </editor-fold>

    // <editor-fold desc="Get specific service">
    @Test
    fun `when fetching a specific service that exists by id, we return that service`() = withBaseTestApplication(
        AuthenticationInstrumentation()
    ) {
        val serviceResponse = givenAService()
        coEvery { serviceController.getServiceById(any()) } returns serviceResponse

        val response = doCall(HttpMethod.Get, "/services/a63a20c4-14dd-4e11-9e87-5ab361a51f65")

        assertThat(response.status).isEqualTo(HttpStatusCode.OK)
        assertThat(response.parseBody<ServiceDto>()).isEqualTo(serviceResponse)
    }

    @Test
    fun `when fetching a specific service by id that does not exists, we return error`() = withBaseTestApplication(
        AuthenticationInstrumentation()
    ) {
        val exception = ErrorNotFound
        coEvery { serviceController.getServiceById(any()) } throws exception

        val response = doCall(HttpMethod.Get, "/services/a63a20c4-14dd-4e11-9e87-5ab361a51f65")

        assertThat(response.status).isEqualTo(exception.statusCode)
        assertThat(response.parseBody<ErrorResponse>()).isEqualTo(exception.toErrorResponse())
    }
    // </editor-fold>

    // <editor-fold desc="Create new service">
    @Test
    fun `when creating service with successful insertion, we return response service body`() = withBaseTestApplication(
        AuthenticationInstrumentation()
    ) {
        val serviceResponse = givenAService()
        coEvery { serviceController.postService(any()) } returns serviceResponse

        val body = toJsonBody(givenAValidInsertService())
        val response = doCall(HttpMethod.Post, "/services", body)

        assertThat(response.status).isEqualTo(HttpStatusCode.Created)
        assertThat(response.parseBody<ServiceDto>()).isEqualTo(serviceResponse)
    }

    @Test
    fun `when creating service already created, we return 409 error`() = withBaseTestApplication(
        AuthenticationInstrumentation()
    ) {
        val exception = ErrorDuplicateEntity
        coEvery { serviceController.postService(any()) } throws exception

        val body = toJsonBody(givenAValidInsertService())
        val response = doCall(HttpMethod.Post, "/services", body)

        assertThat(response.status).isEqualTo(exception.statusCode)
        assertThat(response.parseBody<ErrorResponse>()).isEqualTo(exception.toErrorResponse())
    }
    // </editor-fold>

    // <editor-fold desc="Update service">
    @Test
    fun `when updating service with successful insertion, we return response service body`() = withBaseTestApplication(
        AuthenticationInstrumentation()
    ) {
        val serviceResponse = givenAService()
        coEvery { serviceController.updateServiceById(any(), any()) } returns serviceResponse

        val body = toJsonBody(givenAValidUpdateServiceBody())
        val response = doCall(HttpMethod.Put, "/services/a63a20c4-14dd-4e11-9e87-5ab361a51f65", body)

        assertThat(response.status).isEqualTo(HttpStatusCode.OK)
        assertThat(response.parseBody<ServiceDto>()).isEqualTo(serviceResponse)
    }

    @Test
    fun `when updating service with wrong serviceId, we return error`() = withBaseTestApplication(
        AuthenticationInstrumentation()
    ) {
        val exception = ErrorNotFound
        coEvery { serviceController.updateServiceById(any(), any()) } throws exception

        val body = toJsonBody(givenAValidUpdateServiceBody())
        val response = doCall(HttpMethod.Put, "/services/a63a20c4-14dd-4e11-9e87-5ab361a51f65", body)

        assertThat(response.status).isEqualTo(exception.statusCode)
        assertThat(response.parseBody<ErrorResponse>()).isEqualTo(exception.toErrorResponse())
    }
    // </editor-fold>

    // <editor-fold desc="Delete service">
    @Test
    fun `when deleting service successful, we return Ok response`() = withBaseTestApplication(
        AuthenticationInstrumentation()
    ) {
        coEvery { serviceController.deleteServiceById(any()) } returns Unit

        val response = doCall(HttpMethod.Delete, "/services/a63a20c4-14dd-4e11-9e87-5ab361a51f65")

        assertThat(response.status).isEqualTo(HttpStatusCode.OK)
    }

    @Test
    fun `when deleting service with wrong serviceId, we return error`() = withBaseTestApplication(
        AuthenticationInstrumentation()
    ) {
        val exception = ErrorFailedDelete
        coEvery { serviceController.deleteServiceById(any()) } throws exception

        val response = doCall(HttpMethod.Delete, "/services/a63a20c4-14dd-4e11-9e87-5ab361a51f65")

        assertThat(response.status).isEqualTo(exception.statusCode)
        assertThat(response.parseBody<ErrorResponse>()).isEqualTo(exception.toErrorResponse())
    }
    // </editor-fold>
}
