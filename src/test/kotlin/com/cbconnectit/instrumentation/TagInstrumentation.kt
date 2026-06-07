package com.cbconnectit.instrumentation

import com.cbconnectit.data.dto.requests.tag.InsertNewTag
import com.cbconnectit.data.dto.requests.tag.UpdateTag
import com.cbconnectit.domain.models.tag.Tag
import com.github.slugify.Slugify
import java.time.LocalDateTime
import java.util.*

object TagInstrumentation {

    fun givenAnInvalidInsertTag() = InsertNewTag("  ")
    fun givenAnInvalidUpdateTag() = UpdateTag("  ")
    fun givenAValidInsertTag(name: String = "Android") = InsertNewTag(name)
    fun givenAValidUpdateTag(name: String = "Update tag") = UpdateTag(name)

    fun givenTagList() = listOf(
        givenATag(name = "Tag no. 1"),
        givenATag(name = "Tag no. 2"),
        givenATag(name = "Tag no. 3"),
        givenATag(name = "Unknown"),
    )

    fun givenATag(
        id: UUID = UUID.randomUUID(),
        name: String = "First Tag"
    ) = Tag(
        id,
        name,
        Slugify.builder().build().slugify(name),
        LocalDateTime.now(),
        LocalDateTime.now()
    )
}
