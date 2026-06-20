package com.cbconnectit.dao

import com.cbconnectit.data.database.dao.LinkDaoImpl
import com.cbconnectit.data.database.tables.LinksTable
import com.cbconnectit.data.dto.requests.link.InsertNewLink
import com.cbconnectit.domain.models.link.LinkType
import com.cbconnectit.instrumentation.LinkInstrumentation
import com.cbconnectit.instrumentation.LinkInstrumentation.givenAValidInsertLinkBody
import com.cbconnectit.instrumentation.LinkInstrumentation.givenAValidSecondInsertLinkBody
import com.cbconnectit.instrumentation.LinkInstrumentation.givenAValidUpdateLinkBody
import kotlinx.coroutines.delay
import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.exposed.sql.insert
import org.junit.jupiter.api.Test
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class LinkDaoImplTest : BaseDaoTest() {

    private val dao = LinkDaoImpl()

    override suspend fun seedData() {
        LinkInstrumentation.givenLinkList().forEach { data ->
            LinksTable.insert {
                it[id] = data.id
                it[url] = data.url
                it[type] = data.type
                it[createdAt] = data.createdAt
                it[updatedAt] = data.updatedAt
            }
        }
    }

    // <editor-fold desc="readAll">
    @Test
    fun `getLinks but none exists, return empty list`() = runTest(shouldSeedData = false) {
        val list = dao.getLinks()
        assertThat(list).isEmpty()
    }

    @Test
    fun `getLinks return the list`() = runTest {
        val list = dao.getLinks()
        assertThat(list).hasSize(4)
    }
    // </editor-fold>

    // <editor-fold desc="ReadById">
    @Test
    fun `getLink where item exists, return correct link`() = runTest {
        val validLink = givenAValidSecondInsertLinkBody()
        val link = dao.getLinkById(UUID.fromString("00000000-0000-0000-0000-000000000002"))

        assertThat(link).matches {
            it?.url == validLink.url
        }
    }

    @Test
    fun `getLink where item does not exists, return 'null'`() = runTest {
        val link = dao.getLinkById(UUID.randomUUID())
        assertNull(link)
    }
    // </editor-fold>

    // <editor-fold desc="Create new link">
    @Test
    fun `insertLink where information is correct, database is storing link and returning correct content`() = runTest(shouldSeedData = false) {
        val validLink = givenAValidInsertLinkBody()
        val link = dao.insertLink(validLink, LinkType.Github)

        assertThat(link).matches {
            it?.url == validLink.url &&
                    it.type == LinkType.Github &&
                    it.createdAt == it.updatedAt
        }
    }
    // </editor-fold>

    // <editor-fold desc="Update link">
    @Test
    fun `updateLink where information is correct, database is storing information and returning the correct content`() = runTest {
        val validLink = givenAValidUpdateLinkBody()
        val id = UUID.fromString("00000000-0000-0000-0000-000000000001")

        // adding a delay so there is a clear difference between `updatedAt` and `createdAt`
        delay(1000)

        val link = dao.updateLink(id, validLink, LinkType.PlayStore)

        assertThat(link).matches {
            it?.url == validLink.url &&
                    it.type == LinkType.PlayStore &&
                    it.createdAt != it.updatedAt
        }
    }

    @Test
    fun `updateLink where information is correct but link with id does not exist, database does nothing and returns 'null'`() = runTest {
        val validLink = givenAValidUpdateLinkBody()
        val updated = dao.updateLink(UUID.fromString("00000000-0000-0000-0000-000000000203"), validLink, LinkType.LinkedIn)

        assertNull(updated)
    }
    // </editor-fold>

    // <editor-fold desc="Delete link">
    @Test
    fun `deleteLink for id that exists, return true`() = runTest {
        val deleted = dao.deleteLink(UUID.fromString("00000000-0000-0000-0000-000000000001"))
        assertTrue(deleted)
    }

    @Test
    fun `deleteLink for id that does not exist, return false`() = runTest {
        val deleted = dao.deleteLink(UUID.fromString("00000000-0000-0000-0000-000000000203"))
        assertFalse(deleted)
    }
    // </editor-fold>

    // <editor-fold desc="List of Existing Tag IDs">
    @Test
    fun `getListOfExistingLinkIds where ids do not exist, should return empty list`() = runTest {
        val list = dao.getListOfExistingLinkIds(listOf(UUID.fromString("10000000-0000-0000-0000-000000000000"), UUID.fromString("20000000-0000-0000-0000-000000000000")))
        assertThat(list).isEmpty()
    }

    @Test
    fun `getListOfExistingLinkIds where some ids exist, should return list of existing items`() = runTest {
        val id = dao.insertLink(InsertNewLink("https://existing-link-ids.com"), LinkType.Unknown)?.id
        val list = dao.getListOfExistingLinkIds(listOf(id!!, UUID.fromString("20000000-0000-0000-0000-000000000000")))
        assertThat(list).hasSize(1)
    }
    // </editor-fold>

    // <editor-fold desc="Get or insert link by URL">
    @Test
    fun `getOrInsertLinkByUrl where url already exists, should return existing id`() = runTest {
        val existingId = UUID.fromString("00000000-0000-0000-0000-000000000001")

        val resolvedId = dao.getOrInsertLinkByUrl("https://first-link.com", LinkType.Github)

        assertEquals(existingId, resolvedId)
        assertThat(dao.getLinks()).hasSize(4)
    }

    @Test
    fun `getOrInsertLinkByUrl where url does not exist, should insert and return new id`() = runTest(shouldSeedData = false) {
        val url = "https://brand-new-link.com"

        val resolvedId = dao.getOrInsertLinkByUrl(url, LinkType.LinkedIn)
        val storedLink = dao.getLinkById(resolvedId)

        assertThat(storedLink).matches {
            it?.url == url && it.type == LinkType.LinkedIn
        }
    }

    @Test
    fun `getOrInsertLinkByUrl called twice with same url, should return same id without duplicates`() = runTest(shouldSeedData = false) {
        val url = "https://deduplicated-link.com"

        val firstId = dao.getOrInsertLinkByUrl(url, LinkType.Github)
        val secondId = dao.getOrInsertLinkByUrl(url, LinkType.LinkedIn)

        assertEquals(firstId, secondId)
        assertThat(dao.getLinks()).hasSize(1)
    }
    // </editor-fold>
}
