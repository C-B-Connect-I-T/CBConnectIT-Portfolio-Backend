package com.cbconnectit.domain.models.jobPosition

import com.cbconnectit.data.dto.requests.jobPosition.JobPositionDto
import com.cbconnectit.utils.toDatabaseString
import java.time.LocalDateTime
import java.util.*

data class JobPosition(
    val id: UUID = UUID.randomUUID(),
    val name: String = "",
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)

fun JobPosition.toDto() = JobPositionDto(
    id = this.id.toString(),
    name = this.name,
    createdAt = this.createdAt.toDatabaseString(),
    updatedAt = this.updatedAt.toDatabaseString()
)
