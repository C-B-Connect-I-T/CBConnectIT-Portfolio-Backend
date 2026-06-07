package com.cbconnectit.data.dto.requests.project

import com.cbconnectit.data.dto.requests.link.LinkDto
import com.cbconnectit.data.dto.requests.tag.TagDto
import com.cbconnectit.domain.models.interfaces.DateAble
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProjectDto(
    val id: String = "",
    @SerialName("banner_image_url")
    val bannerImageUrl: String? = null,
    @SerialName("image_url")
    val imageUrl: String? = null,
    val title: String = "",
    @SerialName("short_description")
    val shortDescription: String = "",
    val description: String = "",
    val tags: List<TagDto> = emptyList(),
    val links: List<LinkDto> = emptyList(),
    @SerialName("created_at")
    override val createdAt: String = "",
    @SerialName("updated_at")
    override val updatedAt: String = ""
) : DateAble
