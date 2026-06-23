package com.cbconnectit.modules.mediafile

import com.cbconnectit.data.dto.requests.mediafile.InsertMediaFile
import com.cbconnectit.data.dto.requests.mediafile.UpdateMediaFile
import com.cbconnectit.data.dto.responses.mediafile.MediaFileDto
import com.cbconnectit.domain.interfaces.IMediaFileDao
import com.cbconnectit.domain.models.mediafile.MediaFile
import com.cbconnectit.domain.models.mediafile.OwnerType
import com.cbconnectit.domain.models.mediafile.toDto
import com.cbconnectit.plugins.dbTransactionalQuery
import com.cbconnectit.plugins.statuspages.ErrorInvalidFileType
import com.cbconnectit.plugins.statuspages.ErrorInvalidUUID
import com.cbconnectit.plugins.statuspages.ErrorNotFound
import com.cbconnectit.services.MediaStorageService
import com.cbconnectit.utils.FileValidationUtils
import com.cbconnectit.utils.Parts
import java.util.*

interface MediaFileController {
    suspend fun create(
        request: InsertMediaFile,
        file: Parts.File
    ): MediaFileDto

    suspend fun readById(id: UUID): MediaFileDto
    suspend fun readByOwnerId(ownerId: UUID, ownerType: OwnerType): MediaFileDto?
    suspend fun readAll(): List<MediaFileDto>
    suspend fun update(id: UUID, request: UpdateMediaFile): MediaFileDto
    suspend fun delete(id: UUID): Boolean
}

class MediaFileControllerImpl(
    private val mediaFileDao: IMediaFileDao,
    private val storageService: MediaStorageService
) : MediaFileController {

    override suspend fun create(
        request: InsertMediaFile,
        file: Parts.File
    ): MediaFileDto {
        // Validate and parse owner ID (explicit validation to return 400 instead of 500)
        val ownerId = runCatching { UUID.fromString(request.ownerId) }
            .getOrElse { throw ErrorInvalidUUID }

        // Validate file type before processing (security: prevent XSS/content-hosting attacks)
        if (!FileValidationUtils.isValidImageFile(file.contentType, file.fileName, file.data)) {
            throw ErrorInvalidFileType
        }

        // Phase 1: Store new file to external storage (outside transaction to avoid coupling I/O with DB)
        val storageResult = storageService.storeFromBytes(file.data, file.fileName, file.contentType)

        // Phase 2: Perform database operations in transaction
        // Need to catch any DB exception to trigger compensating cleanup
        @Suppress("TooGenericExceptionCaught")
        return try {
            val oldFileUrl = dbTransactionalQuery {
                // Check for existing media file
                val existingFile = mediaFileDao.readByOwnerId(ownerId, request.ownerType)
                val oldUrl = existingFile?.url

                if (existingFile != null) {
                    // Delete from database
                    mediaFileDao.delete(existingFile.id)
                }

                // Create new media file record
                val mediaFile = MediaFile(
                    url = storageResult.url,
                    ownerId = ownerId,
                    ownerType = request.ownerType,
                    mediaType = request.mediaType,
                    fileSize = storageResult.fileSize,
                    originalFilename = storageResult.originalFilename,
                    altText = request.altText,
                    mimeType = storageResult.mimeType,
                    width = file.width,
                    height = file.height
                )

                mediaFileDao.create(mediaFile)

                // Return old file URL and media file DTO
                Pair(oldUrl, mediaFile.toDto())
            }

            // Phase 3: Delete old file from storage (after successful DB commit, outside transaction)
            oldFileUrl.first?.let { storageService.delete(it) }

            oldFileUrl.second
        } catch (e: Exception) {
            // Compensating action: cleanup newly uploaded file if DB operation failed
            storageService.delete(storageResult.url)
            throw e
        }
    }

    override suspend fun readById(id: UUID): MediaFileDto = dbTransactionalQuery {
        val mediaFile = mediaFileDao.readById(id) ?: throw ErrorNotFound
        mediaFile.toDto()
    }

    override suspend fun readByOwnerId(ownerId: UUID, ownerType: OwnerType): MediaFileDto? = dbTransactionalQuery {
        mediaFileDao.readByOwnerId(ownerId, ownerType)?.toDto()
    }

    override suspend fun readAll(): List<MediaFileDto> = dbTransactionalQuery {
        mediaFileDao.readAll().map { it.toDto() }
    }

    override suspend fun update(id: UUID, request: UpdateMediaFile): MediaFileDto = dbTransactionalQuery {
        val updated = mediaFileDao.update(id, request.altText)
        if (!updated) throw ErrorNotFound

        val mediaFile = mediaFileDao.readById(id) ?: throw ErrorNotFound
        mediaFile.toDto()
    }

    override suspend fun delete(id: UUID): Boolean {
        // Phase 1: Get media file info and delete from database
        val fileUrl = dbTransactionalQuery {
            val mediaFile = mediaFileDao.readById(id) ?: throw ErrorNotFound
            val url = mediaFile.url

            // Delete from database first (ensures DB cleanup happens even if storage fails)
            mediaFileDao.delete(id)

            url
        }

        // Phase 2: Delete from storage (outside transaction)
        // If this fails, DB is already cleaned up, which is acceptable
        // (orphaned files are better than DB entries pointing to non-existent files)
        storageService.delete(fileUrl)

        return true
    }
}
