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
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.mockk.coEvery
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*
import org.koin.dsl.module

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ServiceRoutingTest: BaseRoutingTest() {

    private val serviceController: ServiceController = mockk()

    @BeforeAll
    fun setup() {
        koinModules = module {
            single { serviceController }
        }
        moduleList = {
            install(Routing) {
                serviceRouting()
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

        val call = doCall(HttpMethod.Get, "/services")

        call.also {
            assertThat(HttpStatusCode.OK).isEqualTo(it.response.status())
            val responseBody = it.response.parseBody(List::class.java)
            assertThat(responseBody).hasSize(4)
        }
    }
    // </editor-fold>

    // <editor-fold desc="Get specific service">
    @Test
    fun `when fetching a specific service that exists by id, we return that service`() = withBaseTestApplication(
        AuthenticationInstrumentation()
    ) {
        val serviceResponse = givenAService()
        coEvery { serviceController.getServiceById(any()) } returns serviceResponse

        val call = doCall(HttpMethod.Get, "/services/a63a20c4-14dd-4e11-9e87-5ab361a51f65")

        call.also {
            assertThat(HttpStatusCode.OK).isEqualTo(it.response.status())
            val responseBody = it.response.parseBody(ServiceDto::class.java)
            assertThat(serviceResponse).isEqualTo(responseBody)
        }
    }

    @Test
    fun `when fetching a specific service by id that does not exists, we return error`() = withBaseTestApplication(
        AuthenticationInstrumentation()
    ) {
        coEvery { serviceController.getServiceById(any()) } throws Exception()

        val exception = assertThrows<Exception>{
            doCall(HttpMethod.Get, "/services/a63a20c4-14dd-4e11-9e87-5ab361a51f65")
        }

        assertThat(exception.message).isEqualTo(null)
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
        val call = doCall(HttpMethod.Post, "/services", body)

        call.also {
            assertThat(HttpStatusCode.Created).isEqualTo(it.response.status())
            val responseBody = it.response.parseBody(ServiceDto::class.java)
            assertThat(serviceResponse).isEqualTo(responseBody)
        }
    }

    @Test
    fun `when creating service already created, we return 409 error`() = withBaseTestApplication(
        AuthenticationInstrumentation()
    ) {
        coEvery { serviceController.postService(any()) } throws ErrorDuplicateEntity

        val body = toJsonBody(givenAValidInsertService())
        val exception = assertThrows<ErrorDuplicateEntity> {
            doCall(HttpMethod.Post, "/services", body)
        }
        assertThat(exception.message).isEqualTo(null)
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
        val call = doCall(HttpMethod.Put, "/services/a63a20c4-14dd-4e11-9e87-5ab361a51f65", body)

        call.also {
            assertThat(HttpStatusCode.OK).isEqualTo(it.response.status())
            val responseBody = it.response.parseBody(ServiceDto::class.java)
            assertThat(serviceResponse).isEqualTo(responseBody)
        }
    }

    @Test
    fun `when updating service with wrong serviceId, we return error`() = withBaseTestApplication(
        AuthenticationInstrumentation()
    ) {
        coEvery { serviceController.updateServiceById(any(), any()) } throws Exception()

        val body = toJsonBody(givenAValidUpdateServiceBody())
        val exception = assertThrows<Exception> {
            doCall(HttpMethod.Put, "/services/a63a20c4-14dd-4e11-9e87-5ab361a51f65", body)
        }
        assertThat(exception.message).isEqualTo(null)
    }
    // </editor-fold>

    // <editor-fold desc="Delete service">
    @Test
    fun `when deleting service successful, we return Ok response`() = withBaseTestApplication(
        AuthenticationInstrumentation()
    ) {
        coEvery { serviceController.deleteServiceById(any()) } returns Unit

        val call = doCall(HttpMethod.Delete, "/services/a63a20c4-14dd-4e11-9e87-5ab361a51f65")

        call.also {
            assertThat(HttpStatusCode.OK).isEqualTo(it.response.status())
        }
    }

    @Test
    fun `when deleting service with wrong serviceId, we return error`() = withBaseTestApplication(
        AuthenticationInstrumentation()
    ) {
        coEvery { serviceController.deleteServiceById(any()) } throws Exception()

        val exception = assertThrows<Exception> {
            doCall(HttpMethod.Delete, "/services/a63a20c4-14dd-4e11-9e87-5ab361a51f65")
        }
        assertThat(exception.message).isEqualTo(null)
    }
    // </editor-fold>
}