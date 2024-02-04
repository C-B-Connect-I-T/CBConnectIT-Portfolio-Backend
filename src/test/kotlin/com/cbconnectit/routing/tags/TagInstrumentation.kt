package com.cbconnectit.routing.tags

import com.cbconnectit.data.dto.requests.tag.InsertNewTag
import com.cbconnectit.data.dto.requests.tag.TagDto
import com.cbconnectit.data.dto.requests.tag.UpdateTag
import com.cbconnectit.utils.toDatabaseString
import java.time.LocalDateTime
import java.util.*

object TagInstrumentation {
    fun givenAValidInsertTag() = InsertNewTag("First tag")
    fun givenAValidUpdateTagBody() = UpdateTag("Updated Tag")

    fun givenAnEmptyInsertTagBody() = InsertNewTag("    ")


    fun givenTagList() = listOf(
        givenATag("Tag no. 1"),
        givenATag("Tag no. 2"),
        givenATag("Tag no. 3"),
        givenATag("Unknown"),
    )

    fun givenATag(name: String = "First tag") = run {
        val time = LocalDateTime.now().toDatabaseString()
        TagDto(
            id = UUID.randomUUID().toString(),
            name = name,
            createdAt = time,
            updatedAt = time
        )
    }
}