package com.cbconnectit.data.dto.requests.company

import com.cbconnectit.data.dto.requests.link.LinkDto
import com.cbconnectit.domain.models.interfaces.DateAble
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CompanyDto(
    val id: String = "",
    val name: String = "",
    val links: List<LinkDto> = emptyList(),
    @SerialName("created_at")
    override val createdAt: String = "",
    @SerialName("updated_at")
    override val updatedAt: String = ""
) : DateAble
