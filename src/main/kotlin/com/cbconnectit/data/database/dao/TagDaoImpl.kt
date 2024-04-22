package com.cbconnectit.data.database.dao

import com.cbconnectit.data.database.tables.TagsTable
import com.cbconnectit.data.database.tables.toTag
import com.cbconnectit.data.database.tables.toTags
import com.cbconnectit.data.dto.requests.tag.InsertNewTag
import com.cbconnectit.data.dto.requests.tag.UpdateTag
import com.cbconnectit.domain.interfaces.ITagDao
import com.cbconnectit.domain.models.tag.Tag
import com.github.slugify.Slugify
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.javatime.CurrentDateTime
import org.jetbrains.exposed.sql.lowerCase
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.update
import java.util.*

class TagDaoImpl : ITagDao {
    override fun getTagById(id: UUID): Tag? =
        TagsTable.select { TagsTable.id eq id }.toTag()

    override fun getTagBySlug(slug: String): Tag? =
        TagsTable.select { TagsTable.slug eq slug }.toTag()

    override fun getTags(query: String): List<Tag> =
        TagsTable.select {
            TagsTable.name.lowerCase() like "%${query.lowercase()}%"
        }.toTags()

    override fun insertTag(insertNewTag: InsertNewTag): Tag? {
        val tagId = TagsTable.insertAndGetId {
            it[name] = insertNewTag.name
            it[slug] = Slugify.builder().build().slugify(insertNewTag.name)
        }.value

        return getTagById(tagId)
    }

    override fun updateTag(id: UUID, updateTag: UpdateTag): Tag? {
        TagsTable.update({ TagsTable.id eq id }) {
            it[name] = updateTag.name
            it[slug] = Slugify.builder().build().slugify(updateTag.name)
            it[updatedAt] = CurrentDateTime
        }

        return getTagById(id)
    }

    override fun deleteTag(id: UUID): Boolean =
        TagsTable.deleteWhere { TagsTable.id eq id } > 0

    override fun tagUnique(name: String): Boolean =
        TagsTable.select { TagsTable.name eq name }.empty()

    override fun getListOfExistingTagIds(tagIds: List<UUID>): List<UUID> =
        TagsTable.select { TagsTable.id inList tagIds }.map { it[TagsTable.id].value }
}
