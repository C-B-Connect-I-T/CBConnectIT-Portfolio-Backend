package com.cbconnectit.routing

import com.cbconnectit.data.dto.requests.service.ServiceAdminDto
import com.cbconnectit.data.dto.requests.service.ServiceDto
import com.cbconnectit.domain.models.service.toDto
import com.cbconnectit.domain.models.user.User
import com.cbconnectit.instrumentation.ServiceInstrumentation.givenAService
import com.cbconnectit.instrumentation.ServiceInstrumentation.givenAValidInsertService
import com.cbconnectit.instrumentation.ServiceInstrumentation.givenAValidUpdateService
import com.cbconnectit.instrumentation.ServiceInstrumentation.givenServiceAdminList
import com.cbconnectit.instrumentation.ServiceInstrumentation.givenServiceList
import com.cbconnectit.modules.services.ServiceController
import com.cbconnectit.modules.services.serviceRouting
import com.cbconnectit.plugins.statuspages.ErrorDuplicateEntity
import com.cbconnectit.plugins.statuspages.ErrorFailedDelete
import com.cbconnectit.plugins.statuspages.ErrorMissingRequiredMedia
import com.cbconnectit.plugins.statuspages.ErrorNotFound
import com.cbconnectit.plugins.statuspages.ErrorResponse
import com.cbconnectit.plugins.statuspages.toErrorResponse
import com.cbconnectit.utils.ParamConstants.ADMIN_AUTHENTICATE_KEY
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
class ServiceRoutingTest : BaseRoutingTest() {

    private val serviceController: ServiceController = mockk()

    @BeforeAll
    fun setup() {
        koinModules = module {
            single { serviceController }
            single { json }
        }
        moduleList = {
            routing {
                serviceRouting(json, serviceController)
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
        AuthenticationInstrumentation(ADMIN_AUTHENTICATE_KEY),
        AuthenticationInstrumentation()
    ) {
        coEvery { serviceController.getServices() } returns givenServiceList().map { it.toDto() }

        val response = doCall(HttpMethod.Get, "/services")

        assertThat(response.status).isEqualTo(HttpStatusCode.OK)
        assertThat(response.parseBody<List<ServiceDto>>()).hasSize(4)
    }
    // </editor-fold>

    // <editor-fold desc="Get specific service">
    @Test
    fun `when fetching a specific service that exists by id, we return that service`() = withBaseTestApplication(
        AuthenticationInstrumentation(ADMIN_AUTHENTICATE_KEY),
        AuthenticationInstrumentation()
    ) {
        val serviceResponse = givenAService().toDto()
        coEvery { serviceController.getServiceById(any()) } returns serviceResponse

        val response = doCall(HttpMethod.Get, "/services/a63a20c4-14dd-4e11-9e87-5ab361a51f65")

        assertThat(response.status).isEqualTo(HttpStatusCode.OK)
        assertThat(response.parseBody<ServiceDto>()).isEqualTo(serviceResponse)
    }

    @Test
    fun `when fetching a specific service by id that does not exists, we return error`() = withBaseTestApplication(
        AuthenticationInstrumentation(ADMIN_AUTHENTICATE_KEY),
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
    fun `when creating service with multipart and successful insertion, we return response service body`() = withBaseTestApplication(
        AuthenticationInstrumentation(ADMIN_AUTHENTICATE_KEY),
        AuthenticationInstrumentation()
    ) {
        val serviceResponse = givenAService().toDto()
        coEvery { serviceController.postService(any(), any(), any()) } returns serviceResponse

        val response = client.post("/services") {
            header(HttpHeaders.Authorization, "Bearer ${buildBearerToken()}")
            header("X-Client-Type", "web")
            setBody(
                MultiPartFormDataContent(
                    formData {
                        append("payload", toJsonBody(givenAValidInsertService()))
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
        assertThat(response.parseBody<ServiceDto>()).isEqualTo(serviceResponse)
    }

    @Test
    fun `when creating service without banner image, we return response service body`() = withBaseTestApplication(
        AuthenticationInstrumentation(ADMIN_AUTHENTICATE_KEY),
        AuthenticationInstrumentation()
    ) {
        val serviceResponse = givenAService().toDto()
        coEvery { serviceController.postService(any(), any(), any()) } returns serviceResponse

        val response = client.post("/services") {
            header(HttpHeaders.Authorization, "Bearer ${buildBearerToken()}")
            header("X-Client-Type", "web")
            setBody(
                MultiPartFormDataContent(
                    formData {
                        append("payload", toJsonBody(givenAValidInsertService()))
                        append("image", ByteArray(100), Headers.build {
                            append(HttpHeaders.ContentType, "image/jpeg")
                            append(HttpHeaders.ContentDisposition, "filename=\"image.jpg\"")
                        })
                    }
                )
            )
        }

        assertThat(response.status).isEqualTo(HttpStatusCode.Created)
        assertThat(response.parseBody<ServiceDto>()).isEqualTo(serviceResponse)
    }

    @Test
    fun `when creating service and controller throws missing required media, we return 400 error`() = withBaseTestApplication(
        AuthenticationInstrumentation(ADMIN_AUTHENTICATE_KEY),
        AuthenticationInstrumentation()
    ) {
        val exception = ErrorMissingRequiredMedia
        coEvery { serviceController.postService(any(), any(), any()) } throws exception

        val response = client.post("/services") {
            header(HttpHeaders.Authorization, "Bearer ${buildBearerToken()}")
            header("X-Client-Type", "web")
            setBody(
                MultiPartFormDataContent(
                    formData {
                        append("payload", toJsonBody(givenAValidInsertService()))
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

    @Test
    fun `when creating service without image files in multipart, we return missing required media error`() = withBaseTestApplication(
        AuthenticationInstrumentation(ADMIN_AUTHENTICATE_KEY),
        AuthenticationInstrumentation()
    ) {
        // No image/bannerImage parts — routing throws ErrorMissingRequiredMedia
        val response = client.post("/services") {
            header(HttpHeaders.Authorization, "Bearer ${buildBearerToken()}")
            header("X-Client-Type", "web")
            setBody(
                MultiPartFormDataContent(
                    formData {
                        append("payload", toJsonBody(givenAValidInsertService()))
                        // intentionally omitted image and bannerImage
                    }
                )
            )
        }

        assertThat(response.status).isEqualTo(HttpStatusCode.BadRequest)
    }

    @Test
    fun `when creating service already created, we return 409 error`() = withBaseTestApplication(
        AuthenticationInstrumentation(ADMIN_AUTHENTICATE_KEY),
        AuthenticationInstrumentation()
    ) {
        val exception = ErrorDuplicateEntity
        coEvery { serviceController.postService(any(), any(), any()) } throws exception

        val response = client.post("/services") {
            header(HttpHeaders.Authorization, "Bearer ${buildBearerToken()}")
            header("X-Client-Type", "web")
            setBody(
                MultiPartFormDataContent(
                    formData {
                        append("payload", toJsonBody(givenAValidInsertService()))
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

    // <editor-fold desc="Update service">
    @Test
    fun `when updating service with successful update via JSON, we return response service body`() = withBaseTestApplication(
        AuthenticationInstrumentation(ADMIN_AUTHENTICATE_KEY),
        AuthenticationInstrumentation()
    ) {
        val serviceResponse = givenAService().toDto()
        coEvery { serviceController.updateServiceById(any(), any(), any(), any()) } returns serviceResponse

        val body = toJsonBody(givenAValidUpdateService())
        val response = doCall(HttpMethod.Put, "/services/a63a20c4-14dd-4e11-9e87-5ab361a51f65", body)

        assertThat(response.status).isEqualTo(HttpStatusCode.OK)
        assertThat(response.parseBody<ServiceDto>()).isEqualTo(serviceResponse)
    }

    @Test
    fun `when updating service with wrong serviceId, we return error`() = withBaseTestApplication(
        AuthenticationInstrumentation(ADMIN_AUTHENTICATE_KEY),
        AuthenticationInstrumentation()
    ) {
        val exception = ErrorNotFound
        coEvery { serviceController.updateServiceById(any(), any(), any(), any()) } throws exception

        val body = toJsonBody(givenAValidUpdateService())
        val response = doCall(HttpMethod.Put, "/services/a63a20c4-14dd-4e11-9e87-5ab361a51f65", body)

        assertThat(response.status).isEqualTo(exception.statusCode)
        assertThat(response.parseBody<ErrorResponse>()).isEqualTo(exception.toErrorResponse())
    }
    // </editor-fold>

    // <editor-fold desc="Delete service">
    @Test
    fun `when deleting service successful, we return Ok response`() = withBaseTestApplication(
        AuthenticationInstrumentation(ADMIN_AUTHENTICATE_KEY),
        AuthenticationInstrumentation()
    ) {
        coEvery { serviceController.deleteServiceById(any()) } returns Unit

        val response = doCall(HttpMethod.Delete, "/services/a63a20c4-14dd-4e11-9e87-5ab361a51f65")

        assertThat(response.status).isEqualTo(HttpStatusCode.OK)
    }

    @Test
    fun `when deleting service with wrong serviceId, we return error`() = withBaseTestApplication(
        AuthenticationInstrumentation(ADMIN_AUTHENTICATE_KEY),
        AuthenticationInstrumentation()
    ) {
        val exception = ErrorFailedDelete
        coEvery { serviceController.deleteServiceById(any()) } throws exception

        val response = doCall(HttpMethod.Delete, "/services/a63a20c4-14dd-4e11-9e87-5ab361a51f65")

        assertThat(response.status).isEqualTo(exception.statusCode)
        assertThat(response.parseBody<ErrorResponse>()).isEqualTo(exception.toErrorResponse())
    }
    // </editor-fold>

    // <editor-fold desc="Get service overview (admin)">
    @Test
    fun `when fetching service overview as admin, we return a flat list`() = withBaseTestApplication(
        AuthenticationInstrumentation(ADMIN_AUTHENTICATE_KEY, User.Role.Admin),
        AuthenticationInstrumentation()
    ) {
        coEvery { serviceController.getServicesOverview() } returns givenServiceAdminList()

        val response = doCall(HttpMethod.Get, "/services/overview")

        assertThat(response.status).isEqualTo(HttpStatusCode.OK)
        assertThat(response.parseBody<List<ServiceAdminDto>>()).hasSize(2)
    }

    @Test
    fun `when fetching service overview without admin role, we return 403 error`() = withBaseTestApplication(
        AuthenticationInstrumentation(ADMIN_AUTHENTICATE_KEY, User.Role.User),
        AuthenticationInstrumentation()
    ) {
        val response = doCall(HttpMethod.Get, "/services/overview")

        assertThat(response.status).isEqualTo(HttpStatusCode.Forbidden)
    }
    // </editor-fold>
}
