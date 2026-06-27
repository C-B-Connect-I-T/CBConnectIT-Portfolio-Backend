package com.cbconnectit.domain.interfaces

import com.cbconnectit.domain.models.mediafile.MediaFile
import com.cbconnectit.domain.models.mediafile.MediaType
import com.cbconnectit.domain.models.mediafile.OwnerType
import java.util.*

interface IMediaFileDao {
    fun create(mediaFile: MediaFile): UUID
    fun readById(id: UUID): MediaFile?
    /**
     * Returns the first media file for an owner. Use only for owner types that have a single media file.
     * For SERVICE/PROJECT replacement flows, use [readByOwnerIdAndMediaType] to avoid ambiguity.
     */
    fun readByOwnerId(ownerId: UUID, ownerType: OwnerType): MediaFile?
    fun readByOwnerIdAndMediaType(ownerId: UUID, ownerType: OwnerType, mediaType: MediaType): MediaFile?
    fun readAll(): List<MediaFile>
    fun update(id: UUID, altText: String?): Boolean
    fun delete(id: UUID): Boolean
}
