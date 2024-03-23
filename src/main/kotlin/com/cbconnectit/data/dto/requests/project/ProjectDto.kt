package com.cbconnectit.data.dto.requests.project

import com.cbconnectit.data.dto.requests.link.LinkDto
import com.cbconnectit.data.dto.requests.tag.TagDto
import com.cbconnectit.domain.models.interfaces.DateAble
import com.google.gson.annotations.SerializedName

data class ProjectDto(
    val id: String = "",
    @SerializedName("banner_image_url")
    val bannerImageUrl: String? = null,
    @SerializedName("image_url")
    val imageUrl: String? = null,
    val title: String = "",
    @SerializedName("short_description")
    val shortDescription: String = "",
    val description: String = "",
    val tags: List<TagDto> = emptyList(),
    val links: List<LinkDto> = emptyList(),
    @SerializedName("created_at")
    override val createdAt: String = "",
    @SerializedName("updated_at")
    override val updatedAt: String = ""
) : DateAble
