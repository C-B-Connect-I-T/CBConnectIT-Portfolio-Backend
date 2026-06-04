package com.cbconnectit.routing

import com.cbconnectit.data.dto.requests.link.LinkDto
import com.cbconnectit.domain.models.link.toDto
import com.cbconnectit.instrumentation.LinkInstrumentation.givenALink
import com.cbconnectit.instrumentation.LinkInstrumentation.givenAValidInsertLinkBody
import com.cbconnectit.instrumentation.LinkInstrumentation.givenAValidUpdateLinkBody
import com.cbconnectit.instrumentation.LinkInstrumentation.givenLinkList
import com.cbconnectit.modules.links.LinkController
import com.cbconnectit.modules.links.linkRouting
import com.cbconnectit.plugins.statuspages.ErrorDuplicateEntity
import com.cbconnectit.plugins.statuspages.ErrorFailedDelete
import com.cbconnectit.plugins.statuspages.ErrorNotFound
import com.cbconnectit.plugins.statuspages.ErrorResponse
import com.cbconnectit.plugins.statuspages.toErrorResponse
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
class LinkRoutingTest : BaseRoutingTest() {

    private val linkController: LinkController = mockk()

    @BeforeAll
    fun setup() {
        koinModules = module {
            single { linkController }
        }
        moduleList = {
            routing {
                linkRouting(linkController)
            }
        }
    }

    @BeforeEach
    fun clearMocks() {
        io.mockk.clearMocks(linkController)
    }

    // <editor-fold desc="Get all links">
    @Test
    fun `when fetching all links, we return a list`() = withBaseTestApplication(
        AuthenticationInstrumentation()
    ) {
        coEvery { linkController.getLinks() } returns givenLinkList().map { it.toDto() }

        val response = doCall(HttpMethod.Get, "/links")

        assertThat(response.status).isEqualTo(HttpStatusCode.OK)
        assertThat(response.parseBody<List<LinkDto>>()).hasSize(4)
    }
    // </editor-fold>

    // <editor-fold desc="Get specific link">
    @Test
    fun `when fetching a specific link that exists by id, we return that link`() = withBaseTestApplication(
        AuthenticationInstrumentation()
    ) {
        val linkResponse = givenALink().toDto()
        coEvery { linkController.getLinkById(any()) } returns linkResponse

        val response = doCall(HttpMethod.Get, "/links/a63a20c4-14dd-4e11-9e87-5ab361a51f65")

        assertThat(response.status).isEqualTo(HttpStatusCode.OK)
        assertThat(response.parseBody<LinkDto>()).isEqualTo(linkResponse)
    }

    @Test
    fun `when fetching a specific link by id that does not exists, we return error`() = withBaseTestApplication(
        AuthenticationInstrumentation()
    ) {
        val exception = ErrorNotFound
        coEvery { linkController.getLinkById(any()) } throws exception

        val response = doCall(HttpMethod.Get, "/links/a63a20c4-14dd-4e11-9e87-5ab361a51f65")

        assertThat(response.status).isEqualTo(exception.statusCode)
        assertThat(response.parseBody<ErrorResponse>()).isEqualTo(exception.toErrorResponse())
    }
    // </editor-fold>

    // <editor-fold desc="Create new link">
    @Test
    fun `when creating link with successful insertion, we return response link body`() = withBaseTestApplication(
        AuthenticationInstrumentation()
    ) {
        val linkResponse = givenALink().toDto()
        coEvery { linkController.postLink(any()) } returns linkResponse

        val body = toJsonBody(givenAValidInsertLinkBody())
        val response = doCall(HttpMethod.Post, "/links", body)

        assertThat(response.status).isEqualTo(HttpStatusCode.Created)
        assertThat(response.parseBody<LinkDto>()).isEqualTo(linkResponse)
    }

    @Test
    fun `when creating link already created, we return 409 error`() = withBaseTestApplication(
        AuthenticationInstrumentation()
    ) {
        val exception = ErrorDuplicateEntity
        coEvery { linkController.postLink(any()) } throws exception

        val body = toJsonBody(givenAValidInsertLinkBody())
        val response = doCall(HttpMethod.Post, "/links", body)

        assertThat(response.status).isEqualTo(exception.statusCode)
        assertThat(response.parseBody<ErrorResponse>()).isEqualTo(exception.toErrorResponse())
    }
    // </editor-fold>

    // <editor-fold desc="Update link">
    @Test
    fun `when updating link with successful insertion, we return response link body`() = withBaseTestApplication(
        AuthenticationInstrumentation()
    ) {
        val linkResponse = givenALink().toDto()
        coEvery { linkController.updateLinkById(any(), any()) } returns linkResponse

        val body = toJsonBody(givenAValidUpdateLinkBody())
        val response = doCall(HttpMethod.Put, "/links/a63a20c4-14dd-4e11-9e87-5ab361a51f65", body)

        assertThat(response.status).isEqualTo(HttpStatusCode.OK)
        assertThat(response.parseBody<LinkDto>()).isEqualTo(linkResponse)
    }

    @Test
    fun `when updating link with wrong linkId, we return error`() = withBaseTestApplication(
        AuthenticationInstrumentation()
    ) {
        val exception = ErrorNotFound
        coEvery { linkController.updateLinkById(any(), any()) } throws exception

        val body = toJsonBody(givenAValidUpdateLinkBody())
        val response = doCall(HttpMethod.Put, "/links/a63a20c4-14dd-4e11-9e87-5ab361a51f65", body)

        assertThat(response.status).isEqualTo(exception.statusCode)
        assertThat(response.parseBody<ErrorResponse>()).isEqualTo(exception.toErrorResponse())
    }
    // </editor-fold>

    // <editor-fold desc="Delete link">
    @Test
    fun `when deleting link successful, we return Ok response`() = withBaseTestApplication(
        AuthenticationInstrumentation()
    ) {
        coEvery { linkController.deleteLinkById(any()) } returns Unit

        val response = doCall(HttpMethod.Delete, "/links/a63a20c4-14dd-4e11-9e87-5ab361a51f65")

        assertThat(response.status).isEqualTo(HttpStatusCode.OK)
    }

    @Test
    fun `when deleting link with wrong linkId, we return error`() = withBaseTestApplication(
        AuthenticationInstrumentation()
    ) {
        val exception = ErrorFailedDelete
        coEvery { linkController.deleteLinkById(any()) } throws exception

        val response = doCall(HttpMethod.Delete, "/links/a63a20c4-14dd-4e11-9e87-5ab361a51f65")

        assertThat(response.status).isEqualTo(exception.statusCode)
        assertThat(response.parseBody<ErrorResponse>()).isEqualTo(exception.toErrorResponse())
    }
    // </editor-fold>
}
