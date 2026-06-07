package com.cbconnectit.data.dto.requests.link

import com.cbconnectit.domain.models.interfaces.DateAble
import com.cbconnectit.domain.models.link.LinkType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LinkDto(
    val id: String = "",
    val url: String = "",
    val type: LinkType = LinkType.Unknown,
    @SerialName("created_at")
    override val createdAt: String = "",
    @SerialName("updated_at")
    override val updatedAt: String = ""
) : DateAble
