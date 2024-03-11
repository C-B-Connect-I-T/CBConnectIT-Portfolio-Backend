package com.cbconnectit.domain.models.experience

import com.cbconnectit.data.dto.requests.experience.ExperienceDto
import com.cbconnectit.domain.models.company.Company
import com.cbconnectit.domain.models.company.toDto
import com.cbconnectit.domain.models.jobPosition.JobPosition
import com.cbconnectit.domain.models.jobPosition.toDto
import com.cbconnectit.domain.models.tag.Tag
import com.cbconnectit.domain.models.tag.toDto
import com.cbconnectit.utils.toDatabaseString
import java.time.LocalDateTime
import java.util.*

data class Experience(
    val id: UUID = UUID.randomUUID(),
    val shortDescription: String = "",
    val description: String = "",
    val from: LocalDateTime = LocalDateTime.now(),
    val to: LocalDateTime = LocalDateTime.now(),
    val company: Company = Company(),
    val jobPosition: JobPosition = JobPosition(),
    val tags: List<Tag> = emptyList(),
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)

fun Experience.toDto(): ExperienceDto = ExperienceDto(
    id = this.id.toString(),
    shortDescription = this.shortDescription,
    description = this.description,
    from = this.from.toDatabaseString(),
    to = this.to.toDatabaseString(),
    company = this.company.toDto(),
    jobPosition = this.jobPosition.toDto(),
    tags = this.tags.map { it.toDto() },
    createdAt = this.createdAt.toDatabaseString(),
    updatedAt = this.updatedAt.toDatabaseString()
)