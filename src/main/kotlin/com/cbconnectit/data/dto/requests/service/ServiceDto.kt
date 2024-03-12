package com.cbconnectit.data.dto.requests.service

import com.cbconnectit.data.dto.requests.tag.TagDto
import com.cbconnectit.domain.models.interfaces.DateAble
import com.google.gson.annotations.SerializedName

data class ServiceDto(
    val id: String = "",
    val title: String = "",
    val subServices: List<ServiceDto>? = null,
    val tag: TagDto? = null,
    @SerializedName("created_at")
    override val createdAt: String = "",
    @SerializedName("updated_at")
    override val updatedAt: String = ""
) : DateAble