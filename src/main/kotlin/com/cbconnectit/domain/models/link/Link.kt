package com.cbconnectit.domain.models.link

import com.cbconnectit.data.dto.requests.link.LinkDto
import com.cbconnectit.utils.toDatabaseString
import io.ktor.http.*
import java.time.LocalDateTime
import java.util.*

data class Link(
    val id: UUID = UUID.randomUUID(),
    val url: String = "",
    val type: LinkType = LinkType.Unknown,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)

fun Link.toDto() = LinkDto(
    id = this.id.toString(),
    url = this.url,
    type = this.type,
    createdAt = this.createdAt.toDatabaseString(),
    updatedAt = this.updatedAt.toDatabaseString()
)

enum class LinkType(val host: String? = null) {
    Github("github.com"),
    LinkedIn("linkedin"),
    PlayStore("play.google"),
    AppStore("apps.apple"),
    Unknown; // This should be the last entry!!

    companion object {
        fun getTypeByUrl(url: Url): LinkType {
            return LinkType.entries.firstOrNull {
                url.host.contains(it.host ?: "", ignoreCase = true)
            } ?: Unknown
        }
    }
}
