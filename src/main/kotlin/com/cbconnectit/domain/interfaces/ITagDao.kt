package com.cbconnectit.domain.interfaces

import com.cbconnectit.data.dto.requests.tag.InsertNewTag
import com.cbconnectit.data.dto.requests.tag.UpdateTag
import com.cbconnectit.domain.models.tag.Tag
import java.util.*

interface ITagDao {

    fun getTagById(id: UUID): Tag?
    fun getTagBySlug(slug: String): Tag?
    fun getTags(query: String): List<Tag>
    fun insertTag(insertNewTag: InsertNewTag): Tag?
    fun updateTag(id: UUID, updateTag: UpdateTag): Tag?
    fun deleteTag(id: UUID): Boolean
    fun tagUnique(name: String): Boolean
}