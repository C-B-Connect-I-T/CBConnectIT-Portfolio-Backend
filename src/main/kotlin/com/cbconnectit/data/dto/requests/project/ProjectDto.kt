package com.cbconnectit.data.dto.requests.project

import com.cbconnectit.domain.models.interfaces.DateAble
import com.google.gson.annotations.SerializedName

data class ProjectDto(
    val id: String = "",
    val bannerImage: String? = null,
    val image: String? = null,
    val title: String = "",
    val shortDescription: String = "",
    val description: String = "",
    @SerializedName("created_at")
    override val createdAt: String = "",
    @SerializedName("updated_at")
    override val updatedAt: String = ""
) : DateAble
