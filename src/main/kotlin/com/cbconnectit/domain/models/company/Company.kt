package com.cbconnectit.domain.models.company

import com.cbconnectit.data.dto.requests.company.CompanyDto
import com.cbconnectit.utils.toDatabaseString
import java.time.LocalDateTime
import java.util.UUID

data class Company(
    val id: UUID= UUID.randomUUID(),
    val name: String = "",
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)

fun Company.toDto() = CompanyDto(
    id = this.id.toString(),
    name = this.name,
    createdAt = this.createdAt.toDatabaseString(),
    updatedAt = this.updatedAt.toDatabaseString()
)
