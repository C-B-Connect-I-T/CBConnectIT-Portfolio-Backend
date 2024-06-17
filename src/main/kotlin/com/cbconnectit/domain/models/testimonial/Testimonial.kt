package com.cbconnectit.domain.models.testimonial

import com.cbconnectit.data.dto.requests.testimonial.TestimonialDto
import com.cbconnectit.domain.models.company.Company
import com.cbconnectit.domain.models.company.toDto
import com.cbconnectit.domain.models.jobPosition.JobPosition
import com.cbconnectit.domain.models.jobPosition.toDto
import com.cbconnectit.utils.toDatabaseString
import java.time.LocalDateTime
import java.util.*

data class Testimonial(
    val id: UUID = UUID.randomUUID(),
    val imageUrl: String = "",
    val fullName: String = "",
    val review: String = "",
    val company: Company? = null,
    val jobPosition: JobPosition = JobPosition(),
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)

fun Testimonial.toDto(): TestimonialDto = TestimonialDto(
    id = this.id.toString(),
    imageUrl = this.imageUrl,
    fullName = this.fullName,
    review = this.review,
    company = this.company?.toDto(),
    jobPosition = this.jobPosition.toDto(),
    createdAt = this.createdAt.toDatabaseString(),
    updatedAt = this.updatedAt.toDatabaseString()
)
