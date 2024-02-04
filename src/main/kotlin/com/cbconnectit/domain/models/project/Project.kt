package com.cbconnectit.domain.models.project

import com.cbconnectit.data.dto.requests.project.ProjectDto
import com.cbconnectit.utils.toDatabaseString
import java.time.LocalDateTime
import java.util.*

data class Project(
    val id: UUID = UUID.randomUUID(),
    val bannerImage: String? = null,
    val image: String? = null,
    val title: String = "",
    val shortDescription: String = "",
    val description: String = "",
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)

fun Project.toDto() = ProjectDto(
    id = this.id.toString(),
    bannerImage = bannerImage,
    image = image,
    title = title,
    shortDescription = shortDescription,
    description = description,
    createdAt = createdAt.toDatabaseString(),
    updatedAt = updatedAt.toDatabaseString(),
)