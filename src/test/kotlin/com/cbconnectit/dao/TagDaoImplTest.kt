package com.cbconnectit.dao

import com.cbconnectit.data.database.dao.TagDaoImpl
import com.cbconnectit.instrumentation.TagInstrumentation.givenAValidInsertTag
import com.cbconnectit.instrumentation.TagInstrumentation.givenAValidUpdateTag
import kotlinx.coroutines.delay
import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.*
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class TagDaoImplTest : BaseDaoTest() {

    private val dao = TagDaoImpl()

    // <editor-fold desc="Get all tags">
    @Test
    fun `getTags but none exists, return empty list`() = runTest(shouldSeedData = false) {
        val list = dao.getTags("")
        assertThat(list).isEmpty()
    }

    @Test
    fun `getTags return the list`() = runTest(shouldSeedData = false) {
        dao.insertTag(givenAValidInsertTag("Android"))
        dao.insertTag(givenAValidInsertTag("Kotlin"))
        val list = dao.getTags("")
        assertThat(list).hasSize(2)
    }

    @Test
    fun `getTags with query return the list with the query`() = runTest(shouldSeedData = false) {
        dao.insertTag(givenAValidInsertTag("Android Tag"))
        dao.insertTag(givenAValidInsertTag("Kotlin Tag"))
        dao.insertTag(givenAValidInsertTag("Unknown"))
        val list = dao.getTags("tag")
        assertThat(list).hasSize(2)
    }
    // </editor-fold>

    // <editor-fold desc="Get specific tag by id">
    @Test
    fun `getTag where item exists, return correct tag`() = runTest(shouldSeedData = false) {
        val validTag = givenAValidInsertTag()
        val tagId = dao.insertTag(validTag)?.id
        val tag = dao.getTagById(tagId!!)

        assertThat(tag).matches {
            it?.name == validTag.name
        }
    }

    @Test
    fun `getTag where item does not exists, return 'null'`() = runTest(shouldSeedData = false) {
        val tag = dao.getTagById(UUID.randomUUID())

        assertNull(tag)
    }
    // </editor-fold>

    // <editor-fold desc="Get specific tag by slug">
    @Test
    fun `getTagBySlug where item exists, return correct tag`() = runTest(shouldSeedData = false) {
        val validTag = givenAValidInsertTag()
        val tagSlug = dao.insertTag(validTag)?.slug
        val tag = dao.getTagBySlug(tagSlug!!)

        assertThat(tag).matches {
            it?.name == validTag.name
        }
    }

    @Test
    fun `getTagBySlug where item does not exists, return 'null'`() = runTest(shouldSeedData = false) {
        val tag = dao.getTagBySlug("first slug")

        assertNull(tag)
    }
    // </editor-fold>

    // <editor-fold desc="Create new tag">
    @Test
    fun `insertTag where information is correct, database is storing tag and returning correct content`() = runTest(shouldSeedData = false) {
        val validTag = givenAValidInsertTag()
        val tag = dao.insertTag(validTag)

        assertThat(tag).matches {
            it?.name == validTag.name &&
                    it.createdAt == it.updatedAt
        }
    }

    @Test
    fun `insertTag where the same data exists, database will give error`() = runTest(shouldSeedData = false) {
        val validTag = givenAValidInsertTag()
        dao.insertTag(validTag)

        assertThrows<ExposedSQLException> {
            dao.insertTag(validTag)
        }
    }
    // </editor-fold>

    // <editor-fold desc="Update tag">
    @Test
    fun `updateTag where information is correct, database is storing information and returning the correct content`() = runTest(shouldSeedData = false) {
        val validTag = givenAValidInsertTag()
        val tagId = dao.insertTag(validTag)?.id

        // adding a delay so there is a clear difference between `updatedAt` and `createdAt`
        delay(1000)

        val validUpdateTag = givenAValidUpdateTag()
        val tag = dao.updateTag(tagId!!, validUpdateTag)

        assertThat(tag).matches {
            it?.name != validTag.name &&
                    it?.name == validUpdateTag.name &&
                    it.createdAt != it.updatedAt
        }
    }

    @Test
    fun `updateTag where information is correct but tag with id does not exist, database does nothing and returns 'null'`() = runTest(shouldSeedData = false) {
        val validTag = givenAValidUpdateTag()
        val tag = dao.updateTag(UUID.randomUUID(), validTag)

        assertNull(tag)
    }
    // </editor-fold>

    // <editor-fold desc="Delete tag">
    @Test
    fun `deleteTag for id that exists, return true`() = runTest(shouldSeedData = false) {
        val id = dao.insertTag(givenAValidInsertTag())?.id
        val deleted = dao.deleteTag(id!!)
        assertTrue(deleted)
    }

    @Test
    fun `deleteTag for id that does not exist, return false`() = runTest(shouldSeedData = false) {
        val deleted = dao.deleteTag(UUID.randomUUID())
        assertFalse(deleted)
    }
    // </editor-fold>

    // <editor-fold desc="Check if tag is unique">
    @Test
    fun `tagUnique for id that exists, return false`() = runTest(shouldSeedData = false) {
        dao.insertTag(givenAValidInsertTag())
        val unique = dao.tagUnique(givenAValidInsertTag().name)
        assertFalse(unique)
    }

    @Test
    fun `tagUnique for id that does not exist, return true`() = runTest(shouldSeedData = false) {
        dao.insertTag(givenAValidInsertTag("Android"))
        val unique = dao.tagUnique("Kotlin")
        assertTrue(unique)
    }
    // </editor-fold>

    // <editor-fold desc="List of Existing Tag IDs">
    @Test
    fun `getListOfExistingTagIds where ids do not exist, should return empty list`() = runTest(shouldSeedData = false) {
        val list = dao.getListOfExistingTagIds(listOf(UUID.fromString("10000000-0000-0000-0000-000000000000"), UUID.fromString("20000000-0000-0000-0000-000000000000")))
        assertThat(list).isEmpty()
    }

    @Test
    fun `getListOfExistingTagIds where some ids exist, should return list of existing items`() = runTest(shouldSeedData = false) {
        val id = dao.insertTag(givenAValidInsertTag())?.id
        val list = dao.getListOfExistingTagIds(listOf(id!!, UUID.fromString("20000000-0000-0000-0000-000000000000")))
        assertThat(list).hasSize(1)
    }
    // </editor-fold>
}
