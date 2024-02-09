package com.cbconnectit.routing.links

import com.cbconnectit.data.dto.requests.link.LinkDto
import com.cbconnectit.modules.links.LinkController
import com.cbconnectit.modules.links.linkRouting
import com.cbconnectit.routing.AuthenticationInstrumentation
import com.cbconnectit.routing.BaseRoutingTest
import com.cbconnectit.routing.links.LinkInstrumentation.givenALink
import com.cbconnectit.routing.links.LinkInstrumentation.givenAValidInsertLink
import com.cbconnectit.routing.links.LinkInstrumentation.givenAValidUpdateLinkBody
import com.cbconnectit.routing.links.LinkInstrumentation.givenLinkList
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
class LinkRoutingTest: BaseRoutingTest() {

    private val linkController: LinkController = mockk()

    @BeforeAll
    fun setup() {
        koinModules = module {
            single { linkController }
        }
        moduleList = {
            install(Routing) {
                linkRouting()
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
        coEvery { linkController.getLinks() } returns givenLinkList()

        val call = doCall(HttpMethod.Get, "/links")

        call.also {
            assertThat(HttpStatusCode.OK).isEqualTo(it.response.status())
            val responseBody = it.response.parseBody(List::class.java)
            assertThat(responseBody).hasSize(4)
        }
    }
    // </editor-fold>

    // <editor-fold desc="Get specific link">
    @Test
    fun `when fetching a specific link that exists by id, we return that link`() = withBaseTestApplication(
        AuthenticationInstrumentation()
    ) {
        val linkResponse = givenALink()
        coEvery { linkController.getLinkById(any()) } returns linkResponse

        val call = doCall(HttpMethod.Get, "/links/a63a20c4-14dd-4e11-9e87-5ab361a51f65")

        call.also {
            assertThat(HttpStatusCode.OK).isEqualTo(it.response.status())
            val responseBody = it.response.parseBody(LinkDto::class.java)
            assertThat(linkResponse).isEqualTo(responseBody)
        }
    }

    @Test
    fun `when fetching a specific link by id that does not exists, we return error`() = withBaseTestApplication(
        AuthenticationInstrumentation()
    ) {
        coEvery { linkController.getLinkById(any()) } throws Exception()

        val exception = assertThrows<Exception>{
            doCall(HttpMethod.Get, "/links/a63a20c4-14dd-4e11-9e87-5ab361a51f65")
        }

        assertThat(exception.message).isEqualTo(null)
    }
    // </editor-fold>

    // <editor-fold desc="Create new link">
    @Test
    fun `when creating link with successful insertion, we return response link body`() = withBaseTestApplication(
        AuthenticationInstrumentation()
    ) {
        val linkResponse = givenALink()
        coEvery { linkController.postLink(any()) } returns linkResponse

        val body = toJsonBody(givenAValidInsertLink())
        val call = doCall(HttpMethod.Post, "/links", body)

        call.also {
            assertThat(HttpStatusCode.Created).isEqualTo(it.response.status())
            val responseBody = it.response.parseBody(LinkDto::class.java)
            assertThat(linkResponse).isEqualTo(responseBody)
        }
    }

    @Test
    fun `when creating link already created, we return 409 error`() = withBaseTestApplication(
        AuthenticationInstrumentation()
    ) {
        coEvery { linkController.postLink(any()) } throws ErrorDuplicateEntity

        val body = toJsonBody(givenAValidInsertLink())
        val exception = assertThrows<ErrorDuplicateEntity> {
            doCall(HttpMethod.Post, "/links", body)
        }
        assertThat(exception.message).isEqualTo(null)
    }
    // </editor-fold>

    // <editor-fold desc="Update link">
    @Test
    fun `when updating link with successful insertion, we return response link body`() = withBaseTestApplication(
        AuthenticationInstrumentation()
    ) {
        val linkResponse = givenALink()
        coEvery { linkController.updateLinkById(any(), any()) } returns linkResponse

        val body = toJsonBody(givenAValidUpdateLinkBody())
        val call = doCall(HttpMethod.Put, "/links/a63a20c4-14dd-4e11-9e87-5ab361a51f65", body)

        call.also {
            assertThat(HttpStatusCode.OK).isEqualTo(it.response.status())
            val responseBody = it.response.parseBody(LinkDto::class.java)
            assertThat(linkResponse).isEqualTo(responseBody)
        }
    }

    @Test
    fun `when updating link with wrong linkId, we return error`() = withBaseTestApplication(
        AuthenticationInstrumentation()
    ) {
        coEvery { linkController.updateLinkById(any(), any()) } throws Exception()

        val body = toJsonBody(givenAValidUpdateLinkBody())
        val exception = assertThrows<Exception> {
            doCall(HttpMethod.Put, "/links/a63a20c4-14dd-4e11-9e87-5ab361a51f65", body)
        }
        assertThat(exception.message).isEqualTo(null)
    }
    // </editor-fold>

    // <editor-fold desc="Delete link">
    @Test
    fun `when deleting link successful, we return Ok response`() = withBaseTestApplication(
        AuthenticationInstrumentation()
    ) {
        coEvery { linkController.deleteLinkById(any()) } returns Unit

        val call = doCall(HttpMethod.Delete, "/links/a63a20c4-14dd-4e11-9e87-5ab361a51f65")

        call.also {
            assertThat(HttpStatusCode.OK).isEqualTo(it.response.status())
        }
    }

    @Test
    fun `when deleting link with wrong linkId, we return error`() = withBaseTestApplication(
        AuthenticationInstrumentation()
    ) {
        coEvery { linkController.deleteLinkById(any()) } throws Exception()

        val exception = assertThrows<Exception> {
            doCall(HttpMethod.Delete, "/links/a63a20c4-14dd-4e11-9e87-5ab361a51f65")
        }
        assertThat(exception.message).isEqualTo(null)
    }
    // </editor-fold>
}