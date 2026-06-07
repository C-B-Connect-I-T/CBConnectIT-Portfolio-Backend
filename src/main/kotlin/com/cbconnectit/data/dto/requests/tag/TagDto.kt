package com.cbconnectit.data.dto.requests.tag

import com.cbconnectit.domain.models.interfaces.DateAble
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TagDto(
    val id: String = "",
    val name: String = "",
    val slug: String? = null,
    @SerialName("created_at")
    override val createdAt: String = "",
    @SerialName("updated_at")
    override val updatedAt: String = ""
) : DateAble
