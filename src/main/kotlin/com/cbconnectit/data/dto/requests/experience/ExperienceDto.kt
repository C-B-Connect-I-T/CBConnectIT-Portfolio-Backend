package com.cbconnectit.data.dto.requests.experience

import com.cbconnectit.data.dto.requests.company.CompanyDto
import com.cbconnectit.data.dto.requests.jobPosition.JobPositionDto
import com.cbconnectit.data.dto.requests.tag.TagDto
import com.cbconnectit.domain.models.interfaces.DateAble
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ExperienceDto(
    val id: String = "",
    @SerialName("short_description")
    val shortDescription: String = "",
    val description: String = "",
    val from: String = "",
    val to: String = "",
    val tags: List<TagDto> = emptyList(),
    val company: CompanyDto = CompanyDto(),
    @SerialName("as_freelance")
    val asFreelance: Boolean = false,
    @SerialName("job_position")
    val jobPosition: JobPositionDto = JobPositionDto(),
    @SerialName("created_at")
    override val createdAt: String = "",
    @SerialName("updated_at")
    override val updatedAt: String = ""
) : DateAble
