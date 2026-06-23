package com.cbconnectit.data.dto.requests.testimonial

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class InsertTestimonial(
    val review: String,
    @SerialName("full_name")
    val fullName: String,
    @SerialName("company_id")
    val companyId: String,
    @SerialName("job_position_id")
    val jobPositionId: String,
    @SerialName("avatar_alt_text")
    val avatarAltText: String? = null
) {
    val companyUuid: UUID get() = UUID.fromString(companyId)
    val jobPositionUuid: UUID get() = UUID.fromString(jobPositionId)
    val isValid get() = fullName.isNotBlank() && companyId.isNotBlank() && review.isNotBlank() && jobPositionId.isNotBlank()
}

@Serializable
data class UpdateTestimonial(
    val review: String,
    @SerialName("full_name")
    val fullName: String,
    @SerialName("company_id")
    val companyId: String,
    @SerialName("job_position_id")
    val jobPositionId: String,
    @SerialName("avatar_alt_text")
    val avatarAltText: String? = null,
    val deleteImage: Boolean = false
) {
    val companyUuid: UUID get() = UUID.fromString(companyId)
    val jobPositionUuid: UUID get() = UUID.fromString(jobPositionId)
    val isValid get() = fullName.isNotBlank() && companyId.isNotBlank() && review.isNotBlank() && jobPositionId.isNotBlank()
}
