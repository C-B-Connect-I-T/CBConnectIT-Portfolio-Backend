package com.cbconnectit.controllers.tags

import com.cbconnectit.controllers.BaseControllerTest
import com.cbconnectit.controllers.tags.TagInstrumentation.givenATag
import com.cbconnectit.controllers.tags.TagInstrumentation.givenAValidInsertTag
import com.cbconnectit.controllers.tags.TagInstrumentation.givenAValidUpdateTag
import com.cbconnectit.controllers.tags.TagInstrumentation.givenAnInvalidInsertTag
import com.cbconnectit.controllers.tags.TagInstrumentation.givenAnInvalidUpdateTag
import com.cbconnectit.controllers.tags.TagInstrumentation.givenTagList
import com.cbconnectit.data.dto.requests.tag.TagDto
import com.cbconnectit.domain.interfaces.ITagDao
import com.cbconnectit.modules.tags.TagController
import com.cbconnectit.modules.tags.TagControllerImpl
import com.cbconnectit.statuspages.ErrorDuplicateEntity
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
class TagControllerTest : BaseControllerTest() {

    private val tagDao: ITagDao = mockk()
    private val controller: TagController by lazy { TagControllerImpl(tagDao) }

    @BeforeEach
    override fun before() {
        super.before()
        clearMocks(tagDao)
    }

    // <editor-fold desc="Get all tags">
    @Test
    fun `when requesting all tags, we return valid list`() {
        val createdTag = givenATag()

        coEvery { tagDao.getTags(any()) } returns listOf(createdTag)

        runBlocking {
            val responseTags = controller.getTags("")

            assertThat(responseTags).hasSize(1)
            assertThat(responseTags).allMatch { it is TagDto }
        }
    }

    @Test
    fun `when requesting all tags with specific query, we return valid list`() {
        val query = "tag"
        val tagList = givenTagList()

        coEvery { tagDao.getTags(any()) } returns tagList.filter { it.name.contains(query, true) }

        runBlocking {
            val responseTags = controller.getTags(query)

            assertThat(responseTags).hasSize(3)
            assertThat(responseTags).allMatch { it is TagDto }
        }
    }
    // </editor-fold>

    // <editor-fold desc="Get specific tag">
    @Test
    fun `when requesting specific tag by ID, we return valid tagDto`() {
        val createdTag = givenATag()

        coEvery { tagDao.getTagById(any() as UUID) } returns createdTag

        runBlocking {
            val responseTag = controller.getTagByIdentifier(UUID.randomUUID().toString())

            assertThat(responseTag.name).isEqualTo(createdTag.name)
            assertThat(responseTag.slug).isEqualTo(createdTag.slug)
            assertNotNull(responseTag.createdAt)
            assertNotNull(responseTag.updatedAt)
        }
    }

    @Test
    fun `when requesting specific tag by ID where the ID does not exist, we throw exception`() {
        coEvery { tagDao.getTagById(any() as UUID) } throws ErrorNotFound

        assertThrows<ErrorNotFound> {
            runBlocking { controller.getTagByIdentifier(UUID.randomUUID().toString()) }
        }
    }

    @Test
    fun `when requesting specific tag by slug, we return valid tagDto`() {
        val createdTag = givenATag()

        coEvery { tagDao.getTagBySlug(any()) } returns createdTag

        runBlocking {
            val responseTag = controller.getTagByIdentifier("This is a random slug")

            assertThat(responseTag.name).isEqualTo(createdTag.name)
            assertThat(responseTag.slug).isEqualTo(createdTag.slug)
            assertNotNull(responseTag.createdAt)
            assertNotNull(responseTag.updatedAt)
        }
    }

    @Test
    fun `when requesting specific tag by slug where the slug does not exist, we throw exception`() {
        coEvery { tagDao.getTagBySlug(any()) } throws ErrorNotFound

        assertThrows<ErrorNotFound> {
            runBlocking { controller.getTagByIdentifier("This is a random slug") }
        }
    }
    // </editor-fold>

    // <editor-fold desc="Create new tag">
    @Test
    fun `when creating tag with incorrect information, we throw exception`() {
        val postTag = givenAnInvalidInsertTag()

        coEvery { tagDao.tagUnique(any()) } returns true

        assertThrows<ErrorInvalidParameters> {
            runBlocking { controller.postTag(postTag) }
        }
    }

    @Test
    fun `when creating tag with correct information and tag not taken, we return valid tagDto`() {
        val postTag = givenAValidInsertTag()
        val createdTag = givenATag()

        coEvery { tagDao.tagUnique(any()) } returns true
        coEvery { tagDao.insertTag(any()) } returns createdTag

        runBlocking {
            val responseTag = controller.postTag(postTag)

            assertThat(responseTag.name).isEqualTo(createdTag.name)
            assertThat(responseTag.slug).isEqualTo(createdTag.slug)
        }
    }

    @Test
    fun `when creating tag with tag already taken, we throw an error`() {
        val postTag = givenAValidInsertTag()

        coEvery { tagDao.tagUnique(any()) } returns false

        assertThrows<ErrorDuplicateEntity> {
            runBlocking { controller.postTag(postTag) }
        }
    }

    @Test
    fun `when creating tag and database returns error, we throw exception`() {
        val postTag = givenAValidInsertTag()

        coEvery { tagDao.tagUnique(any()) } returns true
        coEvery { tagDao.insertTag(any()) } returns null

        assertThrows<ErrorFailedCreate> {
            runBlocking { controller.postTag(postTag) }
        }
    }
    // </editor-fold>

    // <editor-fold desc="Update specific tag">
    @Test
    fun `when updating specific tag, we return valid tagDto`() {
        val updateTag = givenAValidUpdateTag()
        val createdTag = givenATag()

        coEvery { tagDao.updateTag(any(), any()) } returns createdTag
        coEvery { tagDao.tagUnique(any()) } returns true

        runBlocking {
            val responseTag = controller.updateTagById(UUID.randomUUID(), updateTag)

            // Assertion
            assertThat(responseTag.name).isEqualTo(createdTag.name)
        }
    }

    @Test
    fun `when updating specific tag where new data is not unique, we throw exception`() {
        val updateTag = givenAValidUpdateTag()

        coEvery { tagDao.tagUnique(any()) } returns false

        assertThrows<ErrorDuplicateEntity> {
            runBlocking { controller.updateTagById(UUID.randomUUID(), updateTag) }
        }
    }

    @Test
    fun `when updating specific tag which has invalid data, we throw exception`() {
        val updateTag = givenAnInvalidUpdateTag()

        assertThrows<ErrorInvalidParameters> {
            runBlocking { controller.updateTagById(UUID.randomUUID(), updateTag) }
        }
    }

    @Test
    fun `when updating specific tag which does not exist, we throw exception`() {
        val updateTag = givenAValidUpdateTag()

        coEvery { tagDao.updateTag(any(), any()) } throws ErrorFailedUpdate
        coEvery { tagDao.tagUnique(any()) } returns true

        assertThrows<ErrorFailedUpdate> {
            runBlocking { controller.updateTagById(UUID.randomUUID(), updateTag) }
        }
    }
    // </editor-fold>

    // <editor-fold desc="Delete tag">
    @Test
    fun `when deleting specific tag, we return valid tagDto`() {
        coEvery { tagDao.deleteTag(any()) } returns true

        assertDoesNotThrow {
            runBlocking {
                controller.deleteTagById(UUID.randomUUID())
            }
        }
    }

    @Test
    fun `when deleting specific tag which does not exist, we throw exception`() {
        coEvery { tagDao.deleteTag(any()) } returns false

        assertThrows<ErrorFailedDelete> {
            runBlocking { controller.deleteTagById(UUID.randomUUID()) }
        }
    }
    // </editor-fold>
}
