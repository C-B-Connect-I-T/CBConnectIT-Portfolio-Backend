package com.cbconnectit.controllers.links

import com.cbconnectit.data.dto.requests.link.InsertNewLink
import com.cbconnectit.data.dto.requests.link.UpdateLink
import com.cbconnectit.domain.models.link.Link
import com.cbconnectit.domain.models.link.LinkType
import com.github.slugify.Slugify
import io.ktor.http.*
import java.util.*

object LinkInstrumentation {

    fun givenAnInvalidInsertLink() = InsertNewLink("  ")
    fun givenAnInvalidUpdateLink() = UpdateLink("  ")
    fun givenAValidInsertLink() = InsertNewLink("https://www.play.google.com/new")
    fun givenAValidUpdateLink() = UpdateLink("https://www.play.google.com/updated")

    fun givenLinkList() = listOf(
        givenALink("https://www.play.google.com"),
        givenALink("https://www.linkedin.com"),
        givenALink("https://www.github.com"),
        givenALink("https://www.someone.com"),
    )

    fun givenALink(url: String = "https://www.someone.com") = Link(UUID.randomUUID(), url, LinkType.getTypeByUrl(Url(url)))
}