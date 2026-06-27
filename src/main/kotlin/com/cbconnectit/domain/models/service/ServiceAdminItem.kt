package com.cbconnectit.domain.models.service

import com.cbconnectit.data.dto.requests.service.CompactServiceDto
import com.cbconnectit.data.dto.requests.service.ServiceAdminDto
import com.cbconnectit.domain.models.tag.Tag
import com.cbconnectit.domain.models.tag.toDto
import com.cbconnectit.utils.toDatabaseString
import java.time.LocalDateTime
import java.util.*

data class CompactService(
    val id: UUID,
    val title: String
)

data class ServiceAdminItem(
    val id: UUID,
    val title: String,
    val parentService: CompactService? = null,
    val tag: Tag? = null,
    val updatedAt: LocalDateTime = LocalDateTime.now()
)

fun CompactService.toDto() = CompactServiceDto(
    id = this.id.toString(),
    title = this.title
)

fun ServiceAdminItem.toDto() = ServiceAdminDto(
    id = this.id.toString(),
    title = this.title,
    parentService = this.parentService?.toDto(),
    tag = this.tag?.toDto(),
    updatedAt = this.updatedAt.toDatabaseString()
)

fun List<ServiceAdminItem>.groupedAndSorted(): List<ServiceAdminItem> {
    val childrenByParentId = this.filter { it.parentService != null }
        .groupBy { it.parentService!!.id }

    fun collectWithChildren(item: ServiceAdminItem): List<ServiceAdminItem> {
        val children = (childrenByParentId[item.id] ?: emptyList()).sortedBy { it.title }
        return listOf(item) + children.flatMap { collectWithChildren(it) }
    }

    return this.filter { it.parentService == null }
        .sortedBy { it.title }
        .flatMap { collectWithChildren(it) }
}
