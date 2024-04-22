package com.cbconnectit.routing.tags

import com.cbconnectit.data.dto.requests.tag.TagDto
import com.cbconnectit.modules.tags.TagController
import com.cbconnectit.modules.tags.tagRouting
import com.cbconnectit.routing.AuthenticationInstrumentation
import com.cbconnectit.routing.BaseRoutingTest
import com.cbconnectit.routing.tags.TagInstrumentation.givenATag
import com.cbconnectit.routing.tags.TagInstrumentation.givenAValidInsertTag
import com.cbconnectit.routing.tags.TagInstrumentation.givenAValidUpdateTagBody
import com.cbconnectit.routing.tags.TagInstrumentation.givenTagList
import com.cbconnectit.statuspages.ErrorDuplicateEntity
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.mockk.coEvery
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import org.koin.dsl.module

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TagRoutingTest : BaseRoutingTest() {

    private val tagController: TagController = mockk()

    @BeforeAll
    fun setup() {
        koinModules = module {
            single { tagController }
        }
        moduleList = {
            install(Routing) {
                tagRouting()
            }
        }
    }

    @BeforeEach
    fun clearMocks() {
        io.mockk.clearMocks(tagController)
    }

    // <editor-fold desc="Get all tags">
    @Test
    fun `when fetching all tags, we return a list`() = withBaseTestApplication(
        AuthenticationInstrumentation()
    ) {
        coEvery { tagController.getTags(any()) } returns givenTagList()

        val call = doCall(HttpMethod.Get, "/tags")

        call.also {
            assertThat(HttpStatusCode.OK).isEqualTo(it.response.status())
            val responseBody = it.response.parseBody(List::class.java)
            assertThat(responseBody).hasSize(4)
        }
    }

    @Test
    fun `when fetching all tags by query, we return a list dependent on said query`() = withBaseTestApplication(
        AuthenticationInstrumentation()
    ) {
        val query = "tag"

        coEvery { tagController.getTags(any()) } returns givenTagList().filter { it.name.contains(query, true) }

        val call = doCall(HttpMethod.Get, "/tags?query=tag")

        call.also {
            assertThat(HttpStatusCode.OK).isEqualTo(it.response.status())
            val responseBody = it.response.parseBody(List::class.java)
            assertThat(responseBody).hasSize(3)
        }
    }
    // </editor-fold>

    // <editor-fold desc="Get specific tag">
    @Test
    fun `when fetching a specific tag that exists by id, we return that tag`() = withBaseTestApplication(
        AuthenticationInstrumentation()
    ) {
        val tagResponse = givenATag()
        coEvery { tagController.getTagByIdentifier(any()) } returns tagResponse

        val call = doCall(HttpMethod.Get, "/tags/a63a20c4-14dd-4e11-9e87-5ab361a51f65")

        call.also {
            assertThat(HttpStatusCode.OK).isEqualTo(it.response.status())
            val responseBody = it.response.parseBody(TagDto::class.java)
            assertThat(tagResponse).isEqualTo(responseBody)
        }
    }

    @Test
    fun `when fetching a specific tag by id that does not exists, we return error`() = withBaseTestApplication(
        AuthenticationInstrumentation()
    ) {
        coEvery { tagController.getTagByIdentifier(any()) } throws Exception()

        val exception = assertThrows<Exception> {
            doCall(HttpMethod.Get, "/tags/a63a20c4-14dd-4e11-9e87-5ab361a51f65")
        }

        assertThat(exception.message).isEqualTo(null)
    }

    @Test
    fun `when fetching a specific tag that exists by slug, we return that tag`() = withBaseTestApplication(
        AuthenticationInstrumentation()
    ) {
        val tagResponse = givenATag()
        coEvery { tagController.getTagByIdentifier(any()) } returns tagResponse

        val call = doCall(HttpMethod.Get, "/tags/this-is-a-slug")

        call.also {
            assertThat(HttpStatusCode.OK).isEqualTo(it.response.status())
            val responseBody = it.response.parseBody(TagDto::class.java)
            assertThat(tagResponse).isEqualTo(responseBody)
        }
    }

    @Test
    fun `when fetching a specific tag by slug that does not exists, we return error`() = withBaseTestApplication(
        AuthenticationInstrumentation()
    ) {
        coEvery { tagController.getTagByIdentifier(any()) } throws Exception()

        val exception = assertThrows<Exception> {
            doCall(HttpMethod.Get, "/tags/this-is-a-slug")
        }

        assertThat(exception.message).isEqualTo(null)
    }
    // </editor-fold>

    // <editor-fold desc="Create new tag">
    @Test
    fun `when creating tag with successful insertion, we return response tag body`() = withBaseTestApplication(
        AuthenticationInstrumentation()
    ) {
        val tagResponse = givenATag()
        coEvery { tagController.postTag(any()) } returns tagResponse

        val body = toJsonBody(givenAValidInsertTag())
        val call = doCall(HttpMethod.Post, "/tags", body)

        call.also {
            assertThat(HttpStatusCode.Created).isEqualTo(it.response.status())
            val responseBody = it.response.parseBody(TagDto::class.java)
            assertThat(tagResponse).isEqualTo(responseBody)
        }
    }

    @Test
    fun `when creating tag already created, we return 409 error`() = withBaseTestApplication(
        AuthenticationInstrumentation()
    ) {
        coEvery { tagController.postTag(any()) } throws ErrorDuplicateEntity

        val body = toJsonBody(givenAValidInsertTag())
        val exception = assertThrows<ErrorDuplicateEntity> {
            doCall(HttpMethod.Post, "/tags", body)
        }
        assertThat(exception.message).isEqualTo(null)
    }
    // </editor-fold>

    // <editor-fold desc="Update tag">
    @Test
    fun `when updating tag with successful insertion, we return response tag body`() = withBaseTestApplication(
        AuthenticationInstrumentation()
    ) {
        val tagResponse = givenATag()
        coEvery { tagController.updateTagById(any(), any()) } returns tagResponse

        val body = toJsonBody(givenAValidUpdateTagBody())
        val call = doCall(HttpMethod.Put, "/tags/a63a20c4-14dd-4e11-9e87-5ab361a51f65", body)

        call.also {
            assertThat(HttpStatusCode.OK).isEqualTo(it.response.status())
            val responseBody = it.response.parseBody(TagDto::class.java)
            assertThat(tagResponse).isEqualTo(responseBody)
        }
    }

    @Test
    fun `when updating tag with wrong tagId, we return error`() = withBaseTestApplication(
        AuthenticationInstrumentation()
    ) {
        coEvery { tagController.updateTagById(any(), any()) } throws Exception()

        val body = toJsonBody(givenAValidUpdateTagBody())
        val exception = assertThrows<Exception> {
            doCall(HttpMethod.Put, "/tags/a63a20c4-14dd-4e11-9e87-5ab361a51f65", body)
        }
        assertThat(exception.message).isEqualTo(null)
    }
    // </editor-fold>

    // <editor-fold desc="Delete tag">
    @Test
    fun `when deleting tag successful, we return Ok response`() = withBaseTestApplication(
        AuthenticationInstrumentation()
    ) {
        coEvery { tagController.deleteTagById(any()) } returns Unit

        val call = doCall(HttpMethod.Delete, "/tags/a63a20c4-14dd-4e11-9e87-5ab361a51f65")

        call.also {
            assertThat(HttpStatusCode.OK).isEqualTo(it.response.status())
        }
    }

    @Test
    fun `when deleting tag with wrong tagId, we return error`() = withBaseTestApplication(
        AuthenticationInstrumentation()
    ) {
        coEvery { tagController.deleteTagById(any()) } throws Exception()

        val exception = assertThrows<Exception> {
            doCall(HttpMethod.Delete, "/tags/a63a20c4-14dd-4e11-9e87-5ab361a51f65")
        }
        assertThat(exception.message).isEqualTo(null)
    }
    // </editor-fold>
}
