package com.cbconnectit.domain.models.service

import com.cbconnectit.data.dto.requests.service.ServiceDto
import com.cbconnectit.domain.models.tag.Tag
import com.cbconnectit.domain.models.tag.toDto
import com.cbconnectit.utils.toDatabaseString
import java.time.LocalDateTime
import java.util.*

data class Service(
    val id: UUID = UUID.randomUUID(),
    val imageUrl: String = "",
    val bannerImageUrl: String? = null,
    val title: String = "",
    val shortDescription: String? = null,
    val description: String = "",
    val bannerDescription: String? = null,
    val extraInfo: String? = null,
    val subServices: List<Service>? = null,
    val tag: Tag? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)

fun Service.toDto(): ServiceDto = ServiceDto(
    id = this.id.toString(),
    imageUrl = this.imageUrl,
    bannerImageUrl = this.bannerImageUrl,
    title = this.title,
    shortDescription = this.shortDescription,
    description = this.description,
    bannerDescription = this.bannerDescription,
    extraInfo = this.extraInfo,
    subServices = subServices?.map { it.toDto() },
    tag = this.tag?.toDto(),
    createdAt = this.createdAt.toDatabaseString(),
    updatedAt = this.updatedAt.toDatabaseString()
)
