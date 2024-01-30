package com.cbconnectit.modules.tags

import com.cbconnectit.data.dto.requests.tag.InsertNewTag
import com.cbconnectit.data.dto.requests.tag.TagDto
import com.cbconnectit.data.dto.requests.tag.UpdateTag
import com.cbconnectit.domain.interfaces.ITagDao
import com.cbconnectit.domain.models.tag.toDto
import com.cbconnectit.modules.BaseController
import com.cbconnectit.plugins.dbQuery
import com.cbconnectit.statuspages.*
import org.koin.core.component.inject
import java.util.*


class TagControllerImpl : BaseController(), TagController {

    private val tagDao by inject<ITagDao>()

    override suspend fun getTags(query: String): List<TagDto> = dbQuery {
        tagDao.getTags(query).map { it.toDto() }
    }

    override suspend fun getTagByIdentifier(tagIdentifier: String): TagDto = dbQuery {

        val tagUUID = try {
            UUID.fromString(tagIdentifier)
        } catch (e: IllegalArgumentException) {
            null
        }

        val tag = if (tagUUID != null) {
            tagDao.getTagById(tagUUID)
        } else {
            tagDao.getTagBySlug(tagIdentifier)
        }

        tag?.toDto() ?: throw ErrorNotFound
    }

    override suspend fun postTag(insertNewTag: InsertNewTag): TagDto = dbQuery {
        if (!insertNewTag.isValid) throw ErrorInvalidParameters

        val tagUnique = tagDao.tagUnique(insertNewTag.name)
        if (!tagUnique) throw ErrorDuplicateEntity

        tagDao.insertTag(insertNewTag)?.toDto() ?: throw ErrorFailedCreate
    }

    override suspend fun updateTagById(tagId: UUID, updateTag: UpdateTag): TagDto = dbQuery {
        if (!updateTag.isValid) throw ErrorInvalidParameters

        val tagUnique = tagDao.tagUnique(updateTag.name)
        if (!tagUnique) throw ErrorDuplicateEntity

        tagDao.updateTag(tagId, updateTag)?.toDto() ?: throw ErrorFailedUpdate
    }

    override suspend fun deleteTagById(tagId: UUID) = dbQuery {
        val deleted = tagDao.deleteTag(tagId)
        if (!deleted) throw ErrorFailedDelete
    }
}

interface TagController {
    suspend fun getTags(query: String): List<TagDto>
    suspend fun getTagByIdentifier(tagIdentifier: String): TagDto
//    suspend fun getTagBySlug(slug: String): TagDto
    suspend fun postTag(insertNewTag: InsertNewTag): TagDto
    suspend fun updateTagById(tagId: UUID, updateTag: UpdateTag): TagDto
    suspend fun deleteTagById(tagId: UUID)
}