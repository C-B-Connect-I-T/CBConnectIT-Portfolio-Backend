package com.cbconnectit.domain.models.project

import com.cbconnectit.data.dto.requests.project.ProjectDto
import com.cbconnectit.domain.models.link.Link
import com.cbconnectit.domain.models.link.toDto
import com.cbconnectit.domain.models.mediafile.MediaFile
import com.cbconnectit.domain.models.mediafile.toCompactDto
import com.cbconnectit.domain.models.tag.Tag
import com.cbconnectit.domain.models.tag.toDto
import com.cbconnectit.utils.toDatabaseString
import java.time.LocalDateTime
import java.util.*

data class Project(
    val id: UUID = UUID.randomUUID(),
    val image: MediaFile? = null,
    val bannerImage: MediaFile? = null,
    val title: String = "",
    val shortDescription: String = "",
    val description: String = "",
    val tags: List<Tag> = emptyList(),
    val links: List<Link> = emptyList(),
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)

fun Project.toDto() = ProjectDto(
    id = this.id.toString(),
    image = this.image?.toCompactDto(),
    bannerImage = this.bannerImage?.toCompactDto(),
    title = title,
    shortDescription = shortDescription,
    description = description,
    tags = tags.map { it.toDto() },
    links = links.map { it.toDto() },
    createdAt = createdAt.toDatabaseString(),
    updatedAt = updatedAt.toDatabaseString(),
)
