package com.cbconnectit.data.dto.requests.link

import com.cbconnectit.domain.models.interfaces.DateAble
import com.cbconnectit.domain.models.link.LinkType
import com.google.gson.annotations.SerializedName

data class LinkDto(
    val id: String = "",
    val url: String = "",
    val type: LinkType = LinkType.Unknown,
    @SerializedName("created_at")
    override val createdAt: String = "",
    @SerializedName("updated_at")
    override val updatedAt: String = ""
) : DateAble
