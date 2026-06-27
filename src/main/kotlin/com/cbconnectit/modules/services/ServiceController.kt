package com.cbconnectit.modules.services

import com.cbconnectit.data.dto.requests.service.InsertNewService
import com.cbconnectit.data.dto.requests.service.ServiceAdminDto
import com.cbconnectit.data.dto.requests.service.ServiceDto
import com.cbconnectit.data.dto.requests.service.UpdateService
import com.cbconnectit.domain.interfaces.IMediaFileDao
import com.cbconnectit.domain.interfaces.IServiceDao
import com.cbconnectit.domain.interfaces.ITagDao
import com.cbconnectit.domain.models.mediafile.MediaFile
import com.cbconnectit.domain.models.mediafile.MediaType
import com.cbconnectit.domain.models.mediafile.OwnerType
import com.cbconnectit.domain.models.service.toDto
import com.cbconnectit.plugins.dbTransactionalQuery
import com.cbconnectit.plugins.statuspages.ErrorFailedCreate
import com.cbconnectit.plugins.statuspages.ErrorFailedDelete
import com.cbconnectit.plugins.statuspages.ErrorFailedUpdate
import com.cbconnectit.plugins.statuspages.ErrorInvalidFileType
import com.cbconnectit.plugins.statuspages.ErrorInvalidParameters
import com.cbconnectit.plugins.statuspages.ErrorMissingRequiredMedia
import com.cbconnectit.plugins.statuspages.ErrorNotFound
import com.cbconnectit.plugins.statuspages.ErrorUnknownServiceIdsForCreate
import com.cbconnectit.plugins.statuspages.ErrorUnknownServiceIdsForUpdate
import com.cbconnectit.plugins.statuspages.ErrorUnknownTagIdsForCreate
import com.cbconnectit.plugins.statuspages.ErrorUnknownTagIdsForUpdate
import com.cbconnectit.services.MediaStorageService
import com.cbconnectit.services.StorageResult
import com.cbconnectit.utils.FileValidationUtils
import com.cbconnectit.utils.Parts
import java.util.*

class ServiceControllerImpl(
    private val serviceDao: IServiceDao,
    private val tagDao: ITagDao,
    private val mediaFileDao: IMediaFileDao,
    private val storageService: MediaStorageService
) : ServiceController {

    override suspend fun getServices(): List<ServiceDto> = dbTransactionalQuery {
        serviceDao.getServices().map { it.toDto() }
    }

    override suspend fun getServicesOverview(): List<ServiceAdminDto> = dbTransactionalQuery {
        serviceDao.getServicesOverview().map { it.toDto() }
    }

    override suspend fun getServiceById(serviceId: UUID): ServiceDto = dbTransactionalQuery {
        serviceDao.getServiceById(serviceId)?.toDto() ?: throw ErrorNotFound
    }

    override suspend fun postService(
        insertNewService: InsertNewService,
        imageFile: Parts.File,
        bannerImageFile: Parts.File?
    ): ServiceDto {
        if (!insertNewService.isValid) throw ErrorInvalidParameters

        if (!FileValidationUtils.isValidImageFile(imageFile.contentType, imageFile.fileName, imageFile.data)) {
            throw ErrorInvalidFileType
        }
        bannerImageFile?.let {
            if (!FileValidationUtils.isValidImageFile(it.contentType, it.fileName, it.data)) throw ErrorInvalidFileType
        }

        // Phase 1: Store files to external storage (outside transaction)
        val imageStorageResult = storageService.storeFromBytes(imageFile.data, imageFile.fileName, imageFile.contentType)
        val bannerStorageResult = try {
            bannerImageFile?.let { file ->
                storageService.storeFromBytes(file.data, file.fileName, file.contentType)
            }
        } catch (e: Exception) {
            storageService.delete(imageStorageResult.url)
            throw e
        }

        // Phase 2: DB transaction — service + both media files atomically
        return try {
            dbTransactionalQuery {
                val parentServiceIds = insertNewService.parentServiceUuid?.let { serviceDao.getListOfExistingServiceIds(listOf(it)) }
                if (insertNewService.parentServiceUuid != null && parentServiceIds?.count() != 1) {
                    throw ErrorUnknownServiceIdsForCreate(listOfNotNull(insertNewService.parentServiceUuid))
                }

                val tagIds = insertNewService.tagUuid?.let { tagDao.getListOfExistingTagIds(listOf(it)) }
                if (tagIds != null && tagIds.count() != 1) {
                    throw ErrorUnknownTagIdsForCreate(listOfNotNull(insertNewService.tagUuid))
                }

                val insertedService = serviceDao.insertService(insertNewService) ?: throw ErrorFailedCreate
                val serviceId = insertedService.id

                mediaFileDao.create(
                    MediaFile(
                        storageResult = imageStorageResult,
                        imageFile = imageFile,
                        ownerId = serviceId,
                        ownerType = OwnerType.SERVICE,
                        mediaType = MediaType.IMAGE,
                        altText = insertNewService.imageAltText ?: ""
                    )
                )
                bannerStorageResult?.let { storageResult ->
                    mediaFileDao.create(
                        MediaFile(
                            storageResult = storageResult,
                            imageFile = bannerImageFile,
                            ownerId = serviceId,
                            ownerType = OwnerType.SERVICE,
                            mediaType = MediaType.BANNER,
                            altText = insertNewService.bannerImageAltText ?: ""
                        )
                    )
                }

                serviceDao.getServiceById(serviceId)?.toDto() ?: throw ErrorFailedCreate
            }
        } catch (e: Exception) {
            // Phase 3: Cleanup if DB failed
            storageService.delete(imageStorageResult.url)
            bannerStorageResult?.let { storageService.delete(it.url) }
            throw e
        }
    }

    override suspend fun updateServiceById(
        serviceId: UUID,
        updateService: UpdateService,
        imageFile: Parts.File?,
        bannerImageFile: Parts.File?
    ): ServiceDto {
        if (!updateService.isValid) throw ErrorInvalidParameters
        val shouldRemoveBanner = updateService.removeBannerImage && bannerImageFile == null

        imageFile?.let {
            if (!FileValidationUtils.isValidImageFile(it.contentType, it.fileName, it.data)) throw ErrorInvalidFileType
        }
        bannerImageFile?.let {
            if (!FileValidationUtils.isValidImageFile(it.contentType, it.fileName, it.data)) throw ErrorInvalidFileType
        }

        // Phase 1: Resolve existing media URLs (in a transaction)
        val oldUrls = dbTransactionalQuery {
            serviceDao.getServiceById(serviceId) ?: throw ErrorNotFound
            val oldImage = if (imageFile != null) mediaFileDao.readByOwnerIdAndMediaType(serviceId, OwnerType.SERVICE, MediaType.IMAGE)?.url else null
            val oldBanner = if (bannerImageFile != null || shouldRemoveBanner) {
                mediaFileDao.readByOwnerIdAndMediaType(serviceId, OwnerType.SERVICE, MediaType.BANNER)?.url
            } else {
                null
            }

            // Enforce: after update, image must still exist
            if (imageFile == null && mediaFileDao.readByOwnerIdAndMediaType(serviceId, OwnerType.SERVICE, MediaType.IMAGE) == null) {
                throw ErrorMissingRequiredMedia
            }

            Pair(oldImage, oldBanner)
        }

        // Phase 2: Store new files outside transaction
        val imageStorageResult: StorageResult? = imageFile?.let { file ->
            storageService.storeFromBytes(file.data, file.fileName, file.contentType)
        }
        val bannerStorageResult: StorageResult? = try {
            bannerImageFile?.let { file ->
                storageService.storeFromBytes(file.data, file.fileName, file.contentType)
            }
        } catch (e: Exception) {
            imageStorageResult?.let { storageService.delete(it.url) }
            throw e
        }

        // Phase 3: DB transaction
        return try {
            dbTransactionalQuery {
                val parentServiceIds = updateService.parentServiceUuid?.let { serviceDao.getListOfExistingServiceIds(listOf(it)) }
                if (updateService.parentServiceUuid != null && parentServiceIds?.count() != 1) {
                    throw ErrorUnknownServiceIdsForUpdate(listOfNotNull(updateService.parentServiceUuid))
                }

                val tagIds = updateService.tagUuid?.let { tagDao.getListOfExistingTagIds(listOf(it)) }
                if (tagIds != null && tagIds.count() != 1) {
                    throw ErrorUnknownTagIdsForUpdate(listOfNotNull(updateService.tagUuid))
                }

                // Replace image if new file provided
                imageStorageResult?.let { result ->
                    val existing = mediaFileDao.readByOwnerIdAndMediaType(serviceId, OwnerType.SERVICE, MediaType.IMAGE)
                    existing?.let { mediaFileDao.delete(it.id) }
                    mediaFileDao.create(
                        MediaFile(
                            storageResult = result,
                            imageFile = imageFile,
                            ownerId = serviceId,
                            ownerType = OwnerType.SERVICE,
                            mediaType = MediaType.IMAGE,
                            altText = updateService.imageAltText ?: ""
                        )
                    )
                } ?: updateService.imageAltText?.let { altText ->
                    mediaFileDao.readByOwnerIdAndMediaType(serviceId, OwnerType.SERVICE, MediaType.IMAGE)
                        ?.let { mediaFileDao.update(it.id, altText) }
                }

                // Replace banner if new file provided, otherwise remove when explicitly requested
                if (bannerStorageResult != null) {
                    val existing = mediaFileDao.readByOwnerIdAndMediaType(serviceId, OwnerType.SERVICE, MediaType.BANNER)
                    existing?.let { mediaFileDao.delete(it.id) }
                    mediaFileDao.create(
                        MediaFile(
                            storageResult = bannerStorageResult,
                            imageFile = bannerImageFile,
                            ownerId = serviceId,
                            ownerType = OwnerType.SERVICE,
                            mediaType = MediaType.BANNER,
                            altText = updateService.bannerImageAltText ?: ""
                        )
                    )
                } else if (shouldRemoveBanner) {
                    mediaFileDao.readByOwnerIdAndMediaType(serviceId, OwnerType.SERVICE, MediaType.BANNER)
                        ?.let { mediaFileDao.delete(it.id) }
                } else {
                    updateService.bannerImageAltText?.let { altText ->
                        mediaFileDao.readByOwnerIdAndMediaType(serviceId, OwnerType.SERVICE, MediaType.BANNER)
                            ?.let { mediaFileDao.update(it.id, altText) }
                    }
                }

                serviceDao.updateService(serviceId, updateService)?.toDto() ?: throw ErrorFailedUpdate
            }.also {
                // Phase 4: Delete old files from storage after successful commit
                oldUrls.first?.let { storageService.delete(it) }
                oldUrls.second?.let { storageService.delete(it) }
            }
        } catch (e: Exception) {
            // Cleanup newly uploaded files
            imageStorageResult?.let { storageService.delete(it.url) }
            bannerStorageResult?.let { storageService.delete(it.url) }
            throw e
        }
    }

    override suspend fun deleteServiceById(serviceId: UUID) {
        val fileUrls = dbTransactionalQuery {
            val image = mediaFileDao.readByOwnerIdAndMediaType(serviceId, OwnerType.SERVICE, MediaType.IMAGE)
            val banner = mediaFileDao.readByOwnerIdAndMediaType(serviceId, OwnerType.SERVICE, MediaType.BANNER)

            image?.let { mediaFileDao.delete(it.id) }
            banner?.let { mediaFileDao.delete(it.id) }

            val deleted = serviceDao.deleteService(serviceId)
            if (!deleted) throw ErrorFailedDelete

            Pair(image?.url, banner?.url)
        }

        fileUrls.first?.let { storageService.delete(it) }
        fileUrls.second?.let { storageService.delete(it) }
    }
}

interface ServiceController {
    suspend fun getServices(): List<ServiceDto>
    suspend fun getServicesOverview(): List<ServiceAdminDto>
    suspend fun getServiceById(serviceId: UUID): ServiceDto
    suspend fun postService(
        insertNewService: InsertNewService,
        imageFile: Parts.File,
        bannerImageFile: Parts.File?
    ): ServiceDto

    suspend fun updateServiceById(
        serviceId: UUID,
        updateService: UpdateService,
        imageFile: Parts.File? = null,
        bannerImageFile: Parts.File? = null
    ): ServiceDto

    suspend fun deleteServiceById(serviceId: UUID)
}
