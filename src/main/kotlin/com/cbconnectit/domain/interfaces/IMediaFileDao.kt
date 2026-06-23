package com.cbconnectit.domain.interfaces

import com.cbconnectit.domain.models.mediafile.MediaFile
import com.cbconnectit.domain.models.mediafile.OwnerType
import java.util.*

interface IMediaFileDao {
    fun create(mediaFile: MediaFile): UUID
    fun readById(id: UUID): MediaFile?
    fun readByOwnerId(ownerId: UUID, ownerType: OwnerType): MediaFile?
    fun readAll(): List<MediaFile>
    fun update(id: UUID, altText: String?): Boolean
    fun delete(id: UUID): Boolean
}
