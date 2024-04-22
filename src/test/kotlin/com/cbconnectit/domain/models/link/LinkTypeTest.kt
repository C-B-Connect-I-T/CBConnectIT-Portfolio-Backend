package com.cbconnectit.domain.models.link

import io.ktor.http.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class LinkTypeTest {

    @Test
    fun `parse url and get the correct type for it`() {
        val appStore = LinkType.getTypeByUrl(Url("https://apps.apple.com/be/app/something/id123456"))
        val playStore = LinkType.getTypeByUrl(Url("https://play.google.com/store/apps/details?id=com.something.app&hl=nl&gl=US"))
        val github = LinkType.getTypeByUrl(Url("https://github.com/someone"))
        val linkedIn = LinkType.getTypeByUrl(Url("https://www.linkedin.com/in/someone/"))
        val unknown = LinkType.getTypeByUrl(Url("https://www.example.com"))

        assertEquals(LinkType.AppStore, appStore)
        assertEquals(LinkType.PlayStore, playStore)
        assertEquals(LinkType.Github, github)
        assertEquals(LinkType.LinkedIn, linkedIn)
        assertEquals(LinkType.Unknown, unknown)
    }
}
