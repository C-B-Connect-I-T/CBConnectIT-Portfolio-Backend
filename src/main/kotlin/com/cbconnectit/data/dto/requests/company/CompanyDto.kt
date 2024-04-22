package com.cbconnectit.data.dto.requests.company

import com.cbconnectit.data.dto.requests.link.LinkDto
import com.cbconnectit.domain.models.interfaces.DateAble
import com.google.gson.annotations.SerializedName

data class CompanyDto(
    val id: String = "",
    val name: String = "",
    val links: List<LinkDto> = emptyList(),
    @SerializedName("created_at")
    override val createdAt: String = "",
    @SerializedName("updated_at")
    override val updatedAt: String = ""
) : DateAble
