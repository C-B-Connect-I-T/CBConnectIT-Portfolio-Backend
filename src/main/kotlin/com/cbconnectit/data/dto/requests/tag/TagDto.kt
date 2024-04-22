package com.cbconnectit.data.dto.requests.tag

import com.cbconnectit.domain.models.interfaces.DateAble
import com.google.gson.annotations.SerializedName

data class TagDto(
    val id: String = "",
    val name: String = "",
    val slug: String? = null,
    @SerializedName("created_at")
    override val createdAt: String = "",
    @SerializedName("updated_at")
    override val updatedAt: String = ""
) : DateAble
