package com.cbconnectit.controllers.tags

import com.cbconnectit.data.dto.requests.tag.InsertNewTag
import com.cbconnectit.data.dto.requests.tag.UpdateTag
import com.cbconnectit.domain.models.tag.Tag
import com.github.slugify.Slugify
import java.util.*

object TagInstrumentation {

    fun givenAnInvalidInsertTag() = InsertNewTag("  ")
    fun givenAnInvalidUpdateTag() = UpdateTag("  ")
    fun givenAValidInsertTag() = InsertNewTag("Android")
    fun givenAValidUpdateTag() = UpdateTag("Update tag")

    fun givenTagList() = listOf(
        givenATag("Tag no. 1"),
        givenATag("Tag no. 2"),
        givenATag("Tag no. 3"),
        givenATag("Unknown"),
    )

    fun givenATag(name: String = "First Tag") = Tag(UUID.randomUUID(), name, Slugify.builder().build().slugify(name))
}