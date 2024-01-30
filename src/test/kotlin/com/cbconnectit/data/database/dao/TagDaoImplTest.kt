package com.cbconnectit.data.database.dao

import com.cbconnectit.data.database.instrumentation.TagInstrumentation.givenAValidInsertTagBody
import com.cbconnectit.data.database.instrumentation.TagInstrumentation.givenAValidSecondInsertTagBody
import com.cbconnectit.data.database.instrumentation.TagInstrumentation.givenAValidUpdateTagBody
import com.cbconnectit.data.database.instrumentation.TagInstrumentation.givenAnUnknownTag
import com.cbconnectit.data.database.tables.TagsTable
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
    fun `getTags but none exists, return empty list`() {
        withTables(TagsTable) {
            val list = dao.getTags("")
            assertThat(list).isEmpty()
        }
    }

    @Test
    fun `getTags return the list`() {
        withTables(TagsTable) {
            dao.insertTag(givenAValidInsertTagBody())
            dao.insertTag(givenAValidSecondInsertTagBody())
            val list = dao.getTags("")
            assertThat(list).hasSize(2)
        }
    }

    @Test
    fun `getTags with query return the list with the query`() {
        withTables(TagsTable) {
            dao.insertTag(givenAValidInsertTagBody())
            dao.insertTag(givenAValidSecondInsertTagBody())
            dao.insertTag(givenAnUnknownTag())
            val list = dao.getTags("tag")
            assertThat(list).hasSize(2)
        }
    }
    // </editor-fold>

    // <editor-fold desc="Get specific tag by id">
    @Test
    fun `getTag where item exists, return correct tag`() {
        withTables(TagsTable) {
            val validTag = givenAValidInsertTagBody()
            val tagId = dao.insertTag(validTag)?.id
            val tag = dao.getTagById(tagId!!)

            assertThat(tag).matches {
                it?.name == validTag.name
            }
        }
    }

    @Test
    fun `getTag where item does not exists, return 'null'`() {
        withTables(TagsTable) {
            val tag = dao.getTagById(UUID.randomUUID())

            assertNull(tag)
        }
    }
    // </editor-fold>

    // <editor-fold desc="Get specific tag by slug">
    @Test
    fun `getTagBySlug where item exists, return correct tag`() {
        withTables(TagsTable) {
            val validTag = givenAValidInsertTagBody()
            val tagSlug = dao.insertTag(validTag)?.slug
            val tag = dao.getTagBySlug(tagSlug!!)

            assertThat(tag).matches {
                it?.name == validTag.name
            }
        }
    }

    @Test
    fun `getTagBySlug where item does not exists, return 'null'`() {
        withTables(TagsTable) {
            val tag = dao.getTagBySlug("first slug")

            assertNull(tag)
        }
    }
    // </editor-fold>

    // <editor-fold desc="Create new tag">
    @Test
    fun `insertTag where information is correct, database is storing tag and returning correct content`() {
        withTables(TagsTable) {
            val validTag = givenAValidInsertTagBody()
            val tag = dao.insertTag(validTag)

            assertThat(tag).matches {
                it?.name == validTag.name &&
                        it.createdAt == it.updatedAt
            }
        }
    }

    @Test
    fun `insertTag where the same data exists, database will give error`() {
        withTables(TagsTable) {
            val validTag = givenAValidInsertTagBody()
            dao.insertTag(validTag)

            assertThrows<ExposedSQLException> {
                dao.insertTag(validTag)
            }
        }
    }
    // </editor-fold>

    // <editor-fold desc="Update tag">
    @Test
    fun `updateTag where information is correct, database is storing information and returning the correct content`() {
        withTables(TagsTable) {
            val validTag = givenAValidInsertTagBody()
            val tagId = dao.insertTag(validTag)?.id

            // adding a delay so there is a clear difference between `updatedAt` and `createdAt`
            delay(1000)

            val validUpdateTag = givenAValidUpdateTagBody()
            val tag = dao.updateTag(tagId!!, validUpdateTag)

            assertThat(tag).matches {
                it?.name != validTag.name &&
                        it?.name == validUpdateTag.name &&
                        it.createdAt != it.updatedAt
            }
        }
    }

    @Test
    fun `updateTag where information is correct but tag with id does not exist, database does nothing and returns 'null'`() {
        withTables(TagsTable) {
            val validTag = givenAValidUpdateTagBody()
            val tag = dao.updateTag(UUID.randomUUID(), validTag)

            assertNull(tag)
        }
    }
    // </editor-fold>

    // <editor-fold desc="Delete tag">
    @Test
    fun `deleteTag for id that exists, return true`() {
        withTables(TagsTable) {
            val id = dao.insertTag(givenAValidInsertTagBody())?.id
            val deleted = dao.deleteTag(id!!)
            assertTrue(deleted)
        }
    }

    @Test
    fun `deleteTag for id that does not exist, return false`() {
        withTables(TagsTable) {
            val deleted = dao.deleteTag(UUID.randomUUID())
            assertFalse(deleted)
        }
    }
    // </editor-fold>

    // <editor-fold desc="Check if tag is unique">
    @Test
    fun `tagUnique for id that exists, return false`() {
        withTables(TagsTable) {
            dao.insertTag(givenAValidInsertTagBody())
            val unique = dao.tagUnique(givenAValidInsertTagBody().name)
            assertFalse(unique)
        }
    }

    @Test
    fun `tagUnique for id that does not exist, return true`() {
        withTables(TagsTable) {
            dao.insertTag(givenAValidInsertTagBody())
            val unique = dao.tagUnique(givenAValidSecondInsertTagBody().name)
            assertTrue(unique)
        }
    }
    // </editor-fold>
}