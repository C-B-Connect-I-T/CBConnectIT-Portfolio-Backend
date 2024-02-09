package com.cbconnectit.routing.links

import com.cbconnectit.data.dto.requests.link.InsertNewLink
import com.cbconnectit.data.dto.requests.link.LinkDto
import com.cbconnectit.data.dto.requests.link.UpdateLink
import com.cbconnectit.utils.toDatabaseString
import java.time.LocalDateTime
import java.util.*

object LinkInstrumentation {
    fun givenAValidInsertLink() = InsertNewLink("https://www.google.be")
    fun givenAValidUpdateLinkBody() = UpdateLink("https://www.google.be/updated")

    fun givenAnEmptyInsertLinkBody() = InsertNewLink("    ")


    fun givenLinkList() = listOf(
        givenALink("https://www.google.be"),
        givenALink("https://www.google.be/second"),
        givenALink("https://www.google.be/third"),
        givenALink("https://www.google.be/fourth"),
    )

    fun givenALink(url: String = "https://www.google.be") = run {
        val time = LocalDateTime.now().toDatabaseString()
        LinkDto(
            id = UUID.randomUUID().toString(),
            url = url,
            createdAt = time,
            updatedAt = time
        )
    }
}