package com.cbconnectit.data.dto.requests.jobPosition

import com.cbconnectit.domain.models.interfaces.DateAble
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class JobPositionDto(
    val id: String = "",
    val name: String = "",
    @SerialName("created_at")
    override val createdAt: String = "",
    @SerialName("updated_at")
    override val updatedAt: String = ""
) : DateAble
