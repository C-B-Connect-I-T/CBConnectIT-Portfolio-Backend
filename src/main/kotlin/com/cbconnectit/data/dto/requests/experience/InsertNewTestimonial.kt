package com.cbconnectit.data.dto.requests.experience

import com.google.gson.annotations.SerializedName
import java.util.*

data class InsertNewExperience(
    @SerializedName("short_description")
    val shortDescription: String,
    val description: String,
    val from: String,
    val to: String,
    val tags: List<String>? = emptyList(), // TODO: determine if this really should be a required field!!
    @SerializedName("company_id")
    val companyId: String,
    @SerializedName("job_position_id")
    val jobPositionId: String
) {
    val companyUuid: UUID get() = UUID.fromString(companyId)
    val jobPositionUuid: UUID get() = UUID.fromString(jobPositionId)
    val isValid get() = shortDescription.isNotBlank() && description.isNotBlank() && from.isNotBlank() && to.isNotBlank() && companyId.isNotBlank() && jobPositionId.isNotBlank()
}

data class UpdateExperience(
    @SerializedName("short_description")
    val shortDescription: String,
    val description: String,
    val from: String,
    val to: String,
    val tags: List<String>? = emptyList(), // TODO: determine if this really should be a required field!!
    @SerializedName("company_id")
    val companyId: String,
    @SerializedName("job_position_id")
    val jobPositionId: String
) {
    val companyUuid: UUID get() = UUID.fromString(companyId)
    val jobPositionUuid: UUID get() = UUID.fromString(jobPositionId)
    val isValid get() = shortDescription.isNotBlank() && description.isNotBlank() && from.isNotBlank() && to.isNotBlank() && companyId.isNotBlank() && jobPositionId.isNotBlank()
}
