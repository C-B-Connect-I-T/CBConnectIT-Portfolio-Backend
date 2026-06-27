package com.cbconnectit.data.database.tables

import com.cbconnectit.data.database.tables.Constants.normalTextSize
import com.cbconnectit.data.database.tables.Constants.smallerTextSize
import com.cbconnectit.domain.models.mediafile.MediaFile
import com.cbconnectit.domain.models.mediafile.MediaType
import com.cbconnectit.domain.models.mediafile.OwnerType
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.javatime.CurrentDateTime
import org.jetbrains.exposed.sql.javatime.datetime

object MediaFilesTable : UUIDTable() {
    val url = text("url")
    val ownerId = uuid("owner_id").index()
    val ownerType = enumerationByName<OwnerType>("owner_type", smallerTextSize).index()
    val mediaType = enumerationByName<MediaType>("media_type", smallerTextSize)
    val fileSize = long("file_size")
    val originalFilename = varchar("original_filename", normalTextSize)
    val altText = text("alt_text")
    val mimeType = varchar("mime_type", smallerTextSize)
    val width = integer("width").nullable()
    val height = integer("height").nullable()
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)
    val updatedAt = datetime("updated_at").defaultExpression(CurrentDateTime)

    init {
        // Unique constraint: one media file per owner per media type (allows IMAGE + BANNER for same owner)
        uniqueIndex(ownerId, ownerType, mediaType)
    }
}

fun ResultRow.toMediaFile(): MediaFile = MediaFile(
    id = this[MediaFilesTable.id].value,
    url = this[MediaFilesTable.url],
    ownerId = this[MediaFilesTable.ownerId],
    ownerType = this[MediaFilesTable.ownerType],
    mediaType = this[MediaFilesTable.mediaType],
    fileSize = this[MediaFilesTable.fileSize],
    originalFilename = this[MediaFilesTable.originalFilename],
    altText = this[MediaFilesTable.altText],
    mimeType = this[MediaFilesTable.mimeType],
    width = this[MediaFilesTable.width],
    height = this[MediaFilesTable.height],
    createdAt = this[MediaFilesTable.createdAt],
    updatedAt = this[MediaFilesTable.updatedAt]
)

fun Iterable<ResultRow>.toMediaFiles() = this.map { it.toMediaFile() }
fun Iterable<ResultRow>.toMediaFile() = this.firstOrNull()?.toMediaFile()
