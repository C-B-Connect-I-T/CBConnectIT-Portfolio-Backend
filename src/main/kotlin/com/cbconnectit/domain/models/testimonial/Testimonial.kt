package com.cbconnectit.domain.models.testimonial

import com.cbconnectit.data.dto.requests.testimonial.TestimonialDto
import com.cbconnectit.domain.models.company.Company
import com.cbconnectit.domain.models.company.toDto
import com.cbconnectit.domain.models.jobPosition.JobPosition
import com.cbconnectit.domain.models.jobPosition.toDto
import com.cbconnectit.domain.models.mediafile.MediaFile
import com.cbconnectit.domain.models.mediafile.toCompactDto
import com.cbconnectit.utils.toDatabaseString
import java.time.LocalDateTime
import java.util.*

data class Testimonial(
    val id: UUID = UUID.randomUUID(),
    val fullName: String = "",
    val review: String = "",
    val company: Company? = null,
    val jobPosition: JobPosition = JobPosition(),
    val avatarImage: MediaFile? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)

fun Testimonial.toDto(): TestimonialDto = TestimonialDto(
    id = this.id.toString(),
    fullName = this.fullName,
    review = this.review,
    company = this.company?.toDto(),
    jobPosition = this.jobPosition.toDto(),
    avatarImage = this.avatarImage?.toCompactDto(),
    createdAt = this.createdAt.toDatabaseString(),
    updatedAt = this.updatedAt.toDatabaseString()
)
