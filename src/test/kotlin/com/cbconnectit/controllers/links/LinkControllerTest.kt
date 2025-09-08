package com.cbconnectit.controllers.links

import com.cbconnectit.controllers.BaseControllerTest
import com.cbconnectit.controllers.links.LinkInstrumentation.givenALink
import com.cbconnectit.controllers.links.LinkInstrumentation.givenAValidInsertLink
import com.cbconnectit.controllers.links.LinkInstrumentation.givenAValidUpdateLink
import com.cbconnectit.controllers.links.LinkInstrumentation.givenAnInvalidInsertLink
import com.cbconnectit.controllers.links.LinkInstrumentation.givenAnInvalidUpdateLink
import com.cbconnectit.data.dto.requests.link.LinkDto
import com.cbconnectit.domain.interfaces.ILinkDao
import com.cbconnectit.modules.links.LinkController
import com.cbconnectit.modules.links.LinkControllerImpl
import com.cbconnectit.statuspages.ErrorFailedCreate
import com.cbconnectit.statuspages.ErrorFailedDelete
import com.cbconnectit.statuspages.ErrorFailedUpdate
import com.cbconnectit.statuspages.ErrorInvalidParameters
import com.cbconnectit.statuspages.ErrorNotFound
import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import java.util.*
import kotlin.test.assertNotNull

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class LinkControllerTest : BaseControllerTest() {

    private val linkDao: ILinkDao = mockk()
    private val controller: LinkController by lazy { LinkControllerImpl(linkDao) }

    @BeforeEach
    override fun before() {
        super.before()
        clearMocks(linkDao)
    }

    // <editor-fold desc="Get all links">
    @Test
    fun `when requesting all links, we return valid list`() {
        val createdLink = givenALink()

        coEvery { linkDao.getLinks() } returns listOf(createdLink)

        runBlocking {
            val responseLinks = controller.getLinks()

            assertThat(responseLinks).hasSize(1)
            assertThat(responseLinks).allMatch { it is LinkDto }
        }
    }
    // </editor-fold>

    // <editor-fold desc="Get specific link">
    @Test
    fun `when requesting specific link by ID, we return valid linkDto`() {
        val createdLink = givenALink()

        coEvery { linkDao.getLinkById(any() as UUID) } returns createdLink

        runBlocking {
            val responseLink = controller.getLinkById(UUID.randomUUID())

            assertThat(responseLink.url).isEqualTo(createdLink.url)
            assertNotNull(responseLink.createdAt)
            assertNotNull(responseLink.updatedAt)
        }
    }

    @Test
    fun `when requesting specific link by ID where the ID does not exist, we throw exception`() {
        coEvery { linkDao.getLinkById(any() as UUID) } throws ErrorNotFound

        assertThrows<ErrorNotFound> {
            runBlocking { controller.getLinkById(UUID.randomUUID()) }
        }
    }
    // </editor-fold>

    // <editor-fold desc="Create new link">
    @Test
    fun `when creating link with incorrect information, we throw exception`() {
        val postLink = givenAnInvalidInsertLink()

        assertThrows<ErrorInvalidParameters> {
            runBlocking { controller.postLink(postLink) }
        }
    }

    @Test
    fun `when creating link with correct information and link not taken, we return valid linkDto`() {
        val postLink = givenAValidInsertLink()
        val createdLink = givenALink()

        coEvery { linkDao.insertLink(any(), any()) } returns createdLink

        runBlocking {
            val responseLink = controller.postLink(postLink)

            assertThat(responseLink.url).isEqualTo(createdLink.url)
        }
    }

    @Test
    fun `when creating link and database returns error, we throw exception`() {
        val postLink = givenAValidInsertLink()

        coEvery { linkDao.insertLink(any(), any()) } returns null

        assertThrows<ErrorFailedCreate> {
            runBlocking { controller.postLink(postLink) }
        }
    }
    // </editor-fold>

    // <editor-fold desc="Update specific link">
    @Test
    fun `when updating specific link, we return valid linkDto`() {
        val updateLink = givenAValidUpdateLink()
        val createdLink = givenALink()

        coEvery { linkDao.updateLink(any(), any(), any()) } returns createdLink

        runBlocking {
            val responseLink = controller.updateLinkById(UUID.randomUUID(), updateLink)

            // Assertion
            assertThat(responseLink.url).isEqualTo(createdLink.url)
        }
    }

    @Test
    fun `when updating specific link which has invalid data, we throw exception`() {
        val updateLink = givenAnInvalidUpdateLink()

        assertThrows<ErrorInvalidParameters> {
            runBlocking { controller.updateLinkById(UUID.randomUUID(), updateLink) }
        }
    }

    @Test
    fun `when updating specific link which does not exist, we throw exception`() {
        val updateLink = givenAValidUpdateLink()

        coEvery { linkDao.updateLink(any(), any(), any()) } throws ErrorFailedUpdate

        assertThrows<ErrorFailedUpdate> {
            runBlocking { controller.updateLinkById(UUID.randomUUID(), updateLink) }
        }
    }
    // </editor-fold>

    // <editor-fold desc="Delete link">
    @Test
    fun `when deleting specific link, we return valid linkDto`() {
        coEvery { linkDao.deleteLink(any()) } returns true

        assertDoesNotThrow {
            runBlocking {
                controller.deleteLinkById(UUID.randomUUID())
            }
        }
    }

    @Test
    fun `when deleting specific link which does not exist, we throw exception`() {
        coEvery { linkDao.deleteLink(any()) } returns false

        assertThrows<ErrorFailedDelete> {
            runBlocking { controller.deleteLinkById(UUID.randomUUID()) }
        }
    }
    // </editor-fold>
}
