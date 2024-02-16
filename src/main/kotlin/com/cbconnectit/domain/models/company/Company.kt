package com.cbconnectit.domain.models.company

import com.cbconnectit.data.dto.requests.company.CompanyDto
import com.cbconnectit.domain.models.link.Link
import com.cbconnectit.utils.toDatabaseString
import java.time.LocalDateTime
import java.util.*

data class Company(
    val id: UUID = UUID.randomUUID(),
    val name: String = "",
    val links: List<Link> = emptyList(),
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)

fun Company.toDto() = CompanyDto(
    id = this.id.toString(),
    name = this.name,
    createdAt = this.createdAt.toDatabaseString(),
    updatedAt = this.updatedAt.toDatabaseString()
)
