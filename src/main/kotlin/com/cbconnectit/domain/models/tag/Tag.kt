package com.cbconnectit.domain.models.tag

import com.cbconnectit.data.dto.requests.tag.TagDto
import com.cbconnectit.utils.toDatabaseString
import java.time.LocalDateTime
import java.util.*

data class Tag(
    val id: UUID = UUID.randomUUID(),
    val name: String = "",
    val slug: String = "",
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)

fun Tag.toDto() = TagDto(
    id = this.id.toString(),
    name = name,
    slug = slug,
    createdAt = createdAt.toDatabaseString(),
    updatedAt = updatedAt.toDatabaseString()
)
