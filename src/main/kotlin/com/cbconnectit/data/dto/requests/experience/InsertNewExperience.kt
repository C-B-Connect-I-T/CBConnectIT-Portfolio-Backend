package com.cbconnectit.data.dto.requests.experience

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class InsertNewExperience(
    @SerialName("short_description")
    val shortDescription: String,
    val description: String,
    val from: String,
    val to: String,
    val tags: List<String>? = emptyList(), // TODO: determine if this really should be a required field!!
    @SerialName("as_freelance")
    val asFreelance: Boolean = false,
    @SerialName("company_id")
    val companyId: String,
    @SerialName("job_position_id")
    val jobPositionId: String
) {
    val companyUuid: UUID get() = UUID.fromString(companyId)
    val jobPositionUuid: UUID get() = UUID.fromString(jobPositionId)
    val isValid get() = shortDescription.isNotBlank() && description.isNotBlank() && from.isNotBlank() && to.isNotBlank() && companyId.isNotBlank() && jobPositionId.isNotBlank()
}

@Serializable
data class UpdateExperience(
    @SerialName("short_description")
    val shortDescription: String,
    val description: String,
    val from: String,
    val to: String,
    @SerialName("as_freelance")
    val asFreelance: Boolean = false,
    val tags: List<String>? = emptyList(), // TODO: determine if this really should be a required field!!
    @SerialName("company_id")
    val companyId: String,
    @SerialName("job_position_id")
    val jobPositionId: String
) {
    val companyUuid: UUID get() = UUID.fromString(companyId)
    val jobPositionUuid: UUID get() = UUID.fromString(jobPositionId)
    val isValid get() = shortDescription.isNotBlank() && description.isNotBlank() && from.isNotBlank() && to.isNotBlank() && companyId.isNotBlank() && jobPositionId.isNotBlank()
}
