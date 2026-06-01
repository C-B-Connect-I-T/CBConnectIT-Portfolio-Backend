package com.cbconnectit.instrumentation

import com.cbconnectit.data.dto.requests.link.InsertNewLink
import com.cbconnectit.data.dto.requests.link.UpdateLink
import com.cbconnectit.domain.models.link.Link
import com.cbconnectit.domain.models.link.LinkType
import java.time.LocalDateTime
import java.util.*

object LinkInstrumentation {

    fun givenAnInvalidInsertLinkBody() = InsertNewLink("    ")

    fun givenAValidInsertLinkBody() = InsertNewLink("https://first-link.com")

    fun givenAValidSecondInsertLinkBody() = InsertNewLink("https://second-link.com")

    fun givenAValidUpdateLinkBody() = UpdateLink("https://update-link.com")

    fun givenAnEmptyUpdateLinkBody() = UpdateLink("   ")

    fun givenAnUnknownLink() = InsertNewLink("https://unknown-link.com")

    fun givenLinkList() = listOf(
        givenALink("00000000-0000-0000-0000-000000000001", "https://first-link.com", LinkType.Github),
        givenALink("00000000-0000-0000-0000-000000000002", "https://second-link.com", LinkType.LinkedIn),
        givenALink("00000000-0000-0000-0000-000000000003", "https://new-extra-link.com", LinkType.PlayStore),
        givenALink("00000000-0000-0000-0000-000000000004", "https://unknown-link.com", LinkType.Unknown),
    )

    fun givenALink(
        id: String = "00000000-0000-0000-0000-000000000001",
        url: String = "https://google.com",
        type: LinkType = LinkType.Unknown
    ) = run {
        val time = LocalDateTime.now()
        Link(UUID.fromString(id), url, type, time, time)
    }
}
