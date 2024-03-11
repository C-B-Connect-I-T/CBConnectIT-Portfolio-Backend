package com.cbconnectit.data.dto.requests.testimonial

import com.google.gson.annotations.SerializedName
import java.util.*

data class InsertNewTestimonial(
    @SerializedName("image_url")
    val imageUrl: String,
    val review: String,
    @SerializedName("full_name")
    val fullName: String,
    @SerializedName("company_id")
    val companyId: String,
    @SerializedName("job_position_id")
    val jobPositionId: String
) {
    val companyUuid: UUID get() = UUID.fromString(companyId)
    val jobPositionUuid: UUID get() = UUID.fromString(jobPositionId)
    val isValid get() = fullName.isNotBlank() && companyId.isNotBlank() && review.isNotBlank() && jobPositionId.isNotBlank()
}

data class UpdateTestimonial(
    @SerializedName("image_url")
    val imageUrl: String,
    val review: String,
    @SerializedName("full_name")
    val fullName: String,
    @SerializedName("company_id")
    val companyId: String,
    @SerializedName("job_position_id")
    val jobPositionId: String
) {
    val companyUuid: UUID get() = UUID.fromString(companyId)
    val jobPositionUuid: UUID get() = UUID.fromString(jobPositionId)
    val isValid get() = fullName.isNotBlank() && companyId.isNotBlank() && review.isNotBlank() && jobPositionId.isNotBlank()
}
