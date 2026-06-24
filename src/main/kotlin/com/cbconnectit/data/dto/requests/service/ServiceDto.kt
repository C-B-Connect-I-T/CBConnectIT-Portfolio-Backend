package com.cbconnectit.data.dto.requests.service

import com.cbconnectit.data.dto.requests.tag.TagDto
import com.cbconnectit.data.dto.responses.mediafile.CompactMediaFileDto
import com.cbconnectit.domain.models.interfaces.DateAble
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ServiceDto(
    val id: String = "",
    val image: CompactMediaFileDto? = null,
    @SerialName("banner_image")
    val bannerImage: CompactMediaFileDto? = null,
    val title: String = "",
    @SerialName("short_description")
    val shortDescription: String? = null,
    val description: String = "",
    @SerialName("banner_description")
    val bannerDescription: String? = null,
    @SerialName("extra_info")
    val extraInfo: String? = null,
    @SerialName("sub_services")
    val subServices: List<ServiceDto>? = null,
    val tag: TagDto? = null,
    @SerialName("created_at")
    override val createdAt: String = "",
    @SerialName("updated_at")
    override val updatedAt: String = ""
) : DateAble
