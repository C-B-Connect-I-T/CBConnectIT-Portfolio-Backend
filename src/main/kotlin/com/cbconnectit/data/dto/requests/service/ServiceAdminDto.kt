package com.cbconnectit.data.dto.requests.service

import com.cbconnectit.data.dto.requests.tag.TagDto
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CompactServiceDto(
    val id: String = "",
    val title: String = ""
)

@Serializable
data class ServiceAdminDto(
    val id: String = "",
    val title: String = "",
    @SerialName("parent_service")
    val parentService: CompactServiceDto? = null,
    val tag: TagDto? = null,
    @SerialName("updated_at")
    val updatedAt: String = ""
)
