package com.cbconnectit.data.dto.requests.testimonial

import com.cbconnectit.data.dto.requests.company.CompanyDto
import com.cbconnectit.data.dto.requests.jobPosition.JobPositionDto
import com.cbconnectit.domain.models.interfaces.DateAble
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TestimonialDto(
    val id: String = "",
    @SerialName("image_url")
    val imageUrl: String = "",
    @SerialName("full_name")
    val fullName: String = "",
    val company: CompanyDto? = null,
    @SerialName("job_position")
    val jobPosition: JobPositionDto = JobPositionDto(),
    val review: String = "",
    @SerialName("created_at")
    override val createdAt: String = "",
    @SerialName("updated_at")
    override val updatedAt: String = ""
) : DateAble
