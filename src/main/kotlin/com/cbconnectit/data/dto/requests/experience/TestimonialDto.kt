package com.cbconnectit.data.dto.requests.experience

import com.cbconnectit.data.dto.requests.company.CompanyDto
import com.cbconnectit.data.dto.requests.jobPosition.JobPositionDto
import com.cbconnectit.data.dto.requests.tag.TagDto
import com.cbconnectit.domain.models.interfaces.DateAble
import com.google.gson.annotations.SerializedName

data class ExperienceDto(
    val id: String = "",
    @SerializedName("short_description")
    val shortDescription: String = "",
    val description: String = "",
    val from: String = "",
    val to: String = "",
    val tags: List<TagDto> = emptyList(),
    val company: CompanyDto = CompanyDto(),
    @SerializedName("job_position")
    val jobPosition: JobPositionDto = JobPositionDto(),
    @SerializedName("created_at")
    override val createdAt: String = "",
    @SerializedName("updated_at")
    override val updatedAt: String = ""
) : DateAble