package com.cbconnectit.data.database.dao.links

import com.cbconnectit.data.database.dao.BaseDaoTest
import com.cbconnectit.data.database.dao.LinkDaoImpl
import com.cbconnectit.data.database.dao.links.LinkInstrumentation.givenAValidInsertLinkBody
import com.cbconnectit.data.database.dao.links.LinkInstrumentation.givenAValidSecondInsertLinkBody
import com.cbconnectit.data.database.dao.links.LinkInstrumentation.givenAValidUpdateLinkBody
import com.cbconnectit.data.database.tables.LinksTable
import com.cbconnectit.domain.models.link.LinkType
import kotlinx.coroutines.delay
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.*
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class LinkDaoImplTest : BaseDaoTest() {

    private val dao = LinkDaoImpl()

    // <editor-fold desc="Get all links">
    @Test
    fun `getLinks but none exists, return empty list`() {
        withTables(LinksTable) {
            val list = dao.getLinks()
            assertThat(list).isEmpty()
        }
    }

    @Test
    fun `getLinks return the list`() {
        withTables(LinksTable) {
            dao.insertLink(givenAValidInsertLinkBody(), LinkType.Unknown)
            dao.insertLink(givenAValidSecondInsertLinkBody(), LinkType.Unknown)
            val list = dao.getLinks()
            assertThat(list).hasSize(2)
        }
    }
    // </editor-fold>

    // <editor-fold desc="Get specific link by id">
    @Test
    fun `getLink where item exists, return correct link`() {
        withTables(LinksTable) {
            val validLink = givenAValidInsertLinkBody()
            val linkId = dao.insertLink(validLink, LinkType.Unknown)?.id
            val link = dao.getLinkById(linkId!!)

            assertThat(link).matches {
                it?.url == validLink.url
            }
        }
    }

    @Test
    fun `getLink where item does not exists, return 'null'`() {
        withTables(LinksTable) {
            val link = dao.getLinkById(UUID.randomUUID())

            assertNull(link)
        }
    }
    // </editor-fold>

    // <editor-fold desc="Create new link">
    @Test
    fun `insertLink where information is correct, database is storing link and returning correct content`() {
        withTables(LinksTable) {
            val validLink = givenAValidInsertLinkBody()
            val link = dao.insertLink(validLink, LinkType.Unknown)

            assertThat(link).matches {
                it?.url == validLink.url &&
                        it.createdAt == it.updatedAt
            }
        }
    }
    // </editor-fold>

    // <editor-fold desc="Update link">
    @Test
    fun `updateLink where information is correct, database is storing information and returning the correct content`() {
        withTables(LinksTable) {
            val validLink = givenAValidInsertLinkBody()
            val linkId = dao.insertLink(validLink, LinkType.Unknown)?.id

            // adding a delay so there is a clear difference between `updatedAt` and `createdAt`
            delay(1000)

            val validUpdateLink = givenAValidUpdateLinkBody()
            val link = dao.updateLink(linkId!!, validUpdateLink, LinkType.Unknown)

            assertThat(link).matches {
                it?.url != validLink.url &&
                        it?.url == validUpdateLink.url &&
                        it.createdAt != it.updatedAt
            }
        }
    }

    @Test
    fun `updateLink where information is correct but link with id does not exist, database does nothing and returns 'null'`() {
        withTables(LinksTable) {
            val validLink = givenAValidUpdateLinkBody()
            val link = dao.updateLink(UUID.randomUUID(), validLink, LinkType.Unknown)

            assertNull(link)
        }
    }
    // </editor-fold>

    // <editor-fold desc="Delete link">
    @Test
    fun `deleteLink for id that exists, return true`() {
        withTables(LinksTable) {
            val id = dao.insertLink(givenAValidInsertLinkBody(), LinkType.Unknown)?.id
            val deleted = dao.deleteLink(id!!)
            assertTrue(deleted)
        }
    }

    @Test
    fun `deleteLink for id that does not exist, return false`() {
        withTables(LinksTable) {
            val deleted = dao.deleteLink(UUID.randomUUID())
            assertFalse(deleted)
        }
    }
    // </editor-fold>
}