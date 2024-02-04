package com.cbconnectit.domain.models.link

import com.cbconnectit.data.dto.requests.link.LinkDto
import com.cbconnectit.utils.toDatabaseString
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
    url = url,
    type = type,
    createdAt = createdAt.toDatabaseString(),
    updatedAt = updatedAt.toDatabaseString()
)

enum class LinkType {
    Unknown,
    Github,
    LinkedIn,
    PlayStore
}