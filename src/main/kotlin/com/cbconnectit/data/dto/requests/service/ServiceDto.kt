package com.cbconnectit.data.dto.requests.service

import com.cbconnectit.data.dto.requests.tag.TagDto
import com.cbconnectit.domain.models.interfaces.DateAble
import com.google.gson.annotations.SerializedName

data class ServiceDto(
    val id: String = "",
    @SerializedName("image_url")
    val imageUrl: String = "",
    val title: String = "",
    @SerializedName("short_description")
    val shortDescription: String? = null,
    val description: String = "",
    @SerializedName("banner_description")
    val bannerDescription: String? = null,
    @SerializedName("extra_info")
    val extraInfo: String? = null,
    @SerializedName("sub_services")
    val subServices: List<ServiceDto>? = null,
    val tag: TagDto? = null,
    @SerializedName("created_at")
    override val createdAt: String = "",
    @SerializedName("updated_at")
    override val updatedAt: String = ""
) : DateAble
