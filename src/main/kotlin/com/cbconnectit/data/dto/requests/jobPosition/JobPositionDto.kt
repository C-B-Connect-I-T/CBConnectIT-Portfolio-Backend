package com.cbconnectit.data.dto.requests.jobPosition

import com.cbconnectit.domain.models.interfaces.DateAble
import com.google.gson.annotations.SerializedName

data class JobPositionDto(
    val id: String = "",
    val name: String = "",
    @SerializedName("created_at")
    override val createdAt: String = "",
    @SerializedName("updated_at")
    override val updatedAt: String = ""
) : DateAble