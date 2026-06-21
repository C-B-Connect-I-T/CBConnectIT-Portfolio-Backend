package com.cbconnectit.data.database.dao

import com.cbconnectit.data.database.tables.MediaFilesTable
import com.cbconnectit.data.database.tables.toMediaFile
import com.cbconnectit.data.database.tables.toMediaFiles
import com.cbconnectit.domain.interfaces.IMediaFileDao
import com.cbconnectit.domain.models.mediafile.MediaFile
import com.cbconnectit.domain.models.mediafile.OwnerType
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.javatime.CurrentDateTime
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import java.util.*

class MediaFileDaoImpl : IMediaFileDao {
    override fun create(mediaFile: MediaFile): UUID {
        MediaFilesTable.insert {
            it[id] = mediaFile.id
            it[url] = mediaFile.url
            it[ownerId] = mediaFile.ownerId
            it[ownerType] = mediaFile.ownerType
            it[mediaType] = mediaFile.mediaType
            it[fileSize] = mediaFile.fileSize
            it[originalFilename] = mediaFile.originalFilename
            it[altText] = mediaFile.altText
            it[mimeType] = mediaFile.mimeType
            it[width] = mediaFile.width
            it[height] = mediaFile.height
        }
        return mediaFile.id
    }

    override fun readById(id: UUID): MediaFile? =
        MediaFilesTable
            .selectAll()
            .where { MediaFilesTable.id eq id }
            .toMediaFile()

    override fun readByOwnerId(ownerId: UUID, ownerType: OwnerType): MediaFile? =
        MediaFilesTable
            .selectAll()
            .where {
                (MediaFilesTable.ownerId eq ownerId) and
                        (MediaFilesTable.ownerType eq ownerType)
            }
            .toMediaFile()

    override fun readAll(): List<MediaFile> =
        MediaFilesTable
            .selectAll()
            .orderBy(MediaFilesTable.createdAt to SortOrder.DESC)
            .toMediaFiles()

    override fun update(id: UUID, altText: String?): Boolean {
        val updateCount = MediaFilesTable.update({ MediaFilesTable.id eq id }) {
            altText?.let { text -> it[MediaFilesTable.altText] = text }
            it[updatedAt] = CurrentDateTime
        }
        return updateCount > 0
    }

    override fun delete(id: UUID): Boolean {
        val deleteCount = MediaFilesTable.deleteWhere {
            with(it) {
                MediaFilesTable.id eq id
            }
        }
        return deleteCount > 0
    }
}
