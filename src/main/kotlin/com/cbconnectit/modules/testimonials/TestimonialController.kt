package com.cbconnectit.modules.testimonials

import com.cbconnectit.data.dto.requests.testimonial.InsertTestimonial
import com.cbconnectit.data.dto.requests.testimonial.TestimonialDto
import com.cbconnectit.data.dto.requests.testimonial.UpdateTestimonial
import com.cbconnectit.domain.interfaces.ICompanyDao
import com.cbconnectit.domain.interfaces.IJobPositionDao
import com.cbconnectit.domain.interfaces.IMediaFileDao
import com.cbconnectit.domain.interfaces.ITestimonialDao
import com.cbconnectit.domain.models.mediafile.MediaFile
import com.cbconnectit.domain.models.mediafile.OwnerType
import com.cbconnectit.domain.models.testimonial.toDto
import com.cbconnectit.plugins.dbTransactionalQuery
import com.cbconnectit.plugins.statuspages.ErrorFailedCreate
import com.cbconnectit.plugins.statuspages.ErrorFailedDelete
import com.cbconnectit.plugins.statuspages.ErrorFailedUpdate
import com.cbconnectit.plugins.statuspages.ErrorInvalidFileType
import com.cbconnectit.plugins.statuspages.ErrorInvalidParameters
import com.cbconnectit.plugins.statuspages.ErrorNotFound
import com.cbconnectit.plugins.statuspages.ErrorUnknownCompanyIdsForCreateTestimonial
import com.cbconnectit.plugins.statuspages.ErrorUnknownCompanyIdsForUpdateTestimonial
import com.cbconnectit.plugins.statuspages.ErrorUnknownJobPositionIdsForCreateTestimonial
import com.cbconnectit.plugins.statuspages.ErrorUnknownJobPositionIdsForUpdateTestimonial
import com.cbconnectit.services.MediaStorageService
import com.cbconnectit.utils.FileValidationUtils
import com.cbconnectit.utils.Parts
import java.util.*

class TestimonialControllerImpl(
    private val testimonialDao: ITestimonialDao,
    private val companyDao: ICompanyDao,
    private val jobPositionDao: IJobPositionDao,
    private val mediaFileDao: IMediaFileDao,
    private val storageService: MediaStorageService
) : TestimonialController {

    override suspend fun create(insertTestimonial: InsertTestimonial, imageFile: Parts.File?): TestimonialDto {
        if (!insertTestimonial.isValid) throw ErrorInvalidParameters

        // Validate image file if provided
        imageFile?.let {
            if (!FileValidationUtils.isValidImageFile(it.contentType, it.fileName, it.data)) {
                throw ErrorInvalidFileType
            }
        }

        // Phase 1: Generate UUID upfront and store file to external storage (outside transaction)
        val testimonialId = UUID.randomUUID()
        val storageResult = imageFile?.let { file ->
            storageService.storeFromBytes(file.data, file.fileName, file.contentType)
        }

        // Phase 2: Single atomic database transaction (testimonial + media file together)
        // Let database constraint handle uniqueness check - simpler and more reliable
        return try {
            dbTransactionalQuery {
                // TODO: rework the uniqueness check to be more efficient, see klantenstop's `CompanyController` for a reference
                val companyIds = insertTestimonial.companyUuid.let { companyDao.getListOfExistingCompanyIds(listOf(it)) }

                if (companyIds.count() != 1) {
                    throw ErrorUnknownCompanyIdsForCreateTestimonial(listOf(insertTestimonial.companyUuid))
                }

                val jobPositionIds = insertTestimonial.jobPositionUuid.let { jobPositionDao.getListOfExistingJobPositionIds(listOf(it)) }

                if (jobPositionIds.count() != 1) {
                    throw ErrorUnknownJobPositionIdsForCreateTestimonial(listOf(insertTestimonial.jobPositionUuid))
                }

                testimonialDao.create(testimonialId, insertTestimonial)

                // Handle image upload if provided
                storageResult?.let { result ->
                    val mediaFile = MediaFile(
                        storageResult = result,
                        imageFile = imageFile,
                        ownerId = testimonialId,
                        ownerType = OwnerType.TESTIMONIAL,
                        altText = "${insertTestimonial.fullName} avatar",
                    )
                    mediaFileDao.create(mediaFile)
                }

                testimonialDao.readById(testimonialId)?.toDto() ?: throw ErrorFailedCreate
            }
        } catch (e: Exception) {
            // Phase 3: Cleanup uploaded file if DB operation failed (no orphaned files in storage)
            storageResult?.let { storageService.delete(it.url) }
            throw e
        }
    }

    override suspend fun readAll(): List<TestimonialDto> = dbTransactionalQuery {
        testimonialDao.readAll().map { it.toDto() }
    }

    override suspend fun readById(id: UUID): TestimonialDto = dbTransactionalQuery {
        testimonialDao.readById(id)?.toDto() ?: throw ErrorNotFound
    }

    override suspend fun updateById(id: UUID, updateTestimonial: UpdateTestimonial, imageFile: Parts.File?): TestimonialDto {
        if (!updateTestimonial.isValid) throw ErrorInvalidParameters

        // Validate image file if provided
        imageFile?.let {
            if (!FileValidationUtils.isValidImageFile(it.contentType, it.fileName, it.data)) {
                throw ErrorInvalidFileType
            }
        }

        // Phase 1: Validate and get old file URL (inside transaction)
        val oldFileUrl = dbTransactionalQuery {
            // Verify category exists
            testimonialDao.readById(id) ?: throw ErrorNotFound

            // Get old file URL if image is being replaced or deleted
            if (imageFile != null || updateTestimonial.deleteImage) {
                mediaFileDao.readByOwnerId(id, OwnerType.TESTIMONIAL)?.url
            } else {
                null
            }
        }

        // Phase 2: Store new file to external storage (outside transaction)
        val storageResult = imageFile?.let { file ->
            storageService.storeFromBytes(file.data, file.fileName, file.contentType)
        }

        // Phase 3: Database operations in transaction
        // Let database constraint handle uniqueness check - simpler and more reliable
        return try {
            dbTransactionalQuery {
                val companyIds = updateTestimonial.companyUuid.let { companyDao.getListOfExistingCompanyIds(listOf(it)) }

                if (companyIds.count() != 1) {
                    throw ErrorUnknownCompanyIdsForUpdateTestimonial(listOf(updateTestimonial.companyUuid))
                }

                val jobPositionIds = updateTestimonial.jobPositionUuid.let { jobPositionDao.getListOfExistingJobPositionIds(listOf(it)) }

                if (jobPositionIds.count() != 1) {
                    throw ErrorUnknownJobPositionIdsForUpdateTestimonial(listOf(updateTestimonial.jobPositionUuid))
                }

                // Handle image deletion if requested
                if (updateTestimonial.deleteImage) {
                    val existingFile = mediaFileDao.readByOwnerId(id, OwnerType.TESTIMONIAL)
                    existingFile?.let { mediaFileDao.delete(it.id) }
                }

                // Handle image upload if provided
                storageResult?.let { result ->
                    val existingFile = mediaFileDao.readByOwnerId(id, OwnerType.TESTIMONIAL)
                    existingFile?.let { mediaFileDao.delete(it.id) }

                    val mediaFile = MediaFile(
                        storageResult = result,
                        imageFile = imageFile,
                        ownerId = id,
                        ownerType = OwnerType.TESTIMONIAL,
                        altText = updateTestimonial.avatarAltText ?: ""
                    )
                    mediaFileDao.create(mediaFile)
                } ?: run {
                    // Update alt text only if no file but avatarAltText is provided
                    updateTestimonial.avatarAltText?.let { altText ->
                        val existingFile = mediaFileDao.readByOwnerId(id, OwnerType.TESTIMONIAL)
                        existingFile?.let { mediaFileDao.update(it.id, altText) }
                    }
                }

                val updated = testimonialDao.updateById(id, updateTestimonial)
                if (!updated) throw ErrorFailedUpdate

                testimonialDao.readById(id)?.toDto() ?: throw ErrorNotFound
            }.also {
                // Phase 4: Delete old file from storage (after successful DB commit)
                oldFileUrl?.let { storageService.delete(it) }
            }
        } catch (e: Exception) {
            // Cleanup newly uploaded file if DB operation failed
            storageResult?.let { storageService.delete(it.url) }
            throw e
        }
    }

    override suspend fun updateTestimonialAvatar(id: UUID, imageFile: Parts.File, altText: String): TestimonialDto {
        if (!FileValidationUtils.isValidImageFile(imageFile.contentType, imageFile.fileName, imageFile.data)) {
            throw ErrorInvalidFileType
        }

        // Phase 1: Store new file to external storage (outside transaction)
        val storageResult = storageService.storeFromBytes(imageFile.data, imageFile.fileName, imageFile.contentType)

        // Phase 2: Database operations in transaction
        @Suppress("TooGenericExceptionCaught")
        return try {
            val oldFileUrl = dbTransactionalQuery {
                // Verify category exists
                testimonialDao.readById(id) ?: throw ErrorNotFound

                // Check for existing media file
                val existingFile = mediaFileDao.readByOwnerId(id, OwnerType.TESTIMONIAL)
                val oldUrl = existingFile?.url

                if (existingFile != null) {
                    mediaFileDao.delete(existingFile.id)
                }

                // Create new media file record
                val mediaFile = MediaFile(
                    storageResult = storageResult,
                    imageFile = imageFile,
                    ownerId = id,
                    ownerType = OwnerType.TESTIMONIAL,
                    altText = altText
                )
                mediaFileDao.create(mediaFile)

                // Return old file URL and category DTO
                Pair(oldUrl, testimonialDao.readById(id)?.toDto() ?: throw ErrorNotFound)
            }

            // Phase 3: Delete old file from storage (after successful DB commit, outside transaction)
            oldFileUrl.first?.let { storageService.delete(it) }

            oldFileUrl.second
        } catch (e: Exception) {
            // Cleanup newly uploaded file if DB operation failed
            storageService.delete(storageResult.url)
            throw e
        }
    }

    override suspend fun deleteTestimonialAvatar(id: UUID): TestimonialDto {
        // Phase 1: Get file URL from database
        val fileUrl = dbTransactionalQuery {
            // Verify category exists
            testimonialDao.readById(id) ?: throw ErrorNotFound

            val existingFile = mediaFileDao.readByOwnerId(id, OwnerType.TESTIMONIAL)
            val url = existingFile?.url

            // Delete from database
            existingFile?.let { mediaFileDao.delete(it.id) }

            url
        }

        // Phase 2: Delete from storage (outside transaction)
        fileUrl?.let { storageService.delete(it) }

        // Phase 3: Return updated category
        return dbTransactionalQuery {
            testimonialDao.readById(id)?.toDto() ?: throw ErrorNotFound
        }
    }

    // TODO: Also delete the used image from the storage!
    override suspend fun deleteById(id: UUID) = dbTransactionalQuery {
        val deleted = testimonialDao.deleteById(id)
        if (!deleted) throw ErrorFailedDelete
    }
}

interface TestimonialController {
    suspend fun readAll(): List<TestimonialDto>
    suspend fun readById(id: UUID): TestimonialDto
    suspend fun create(insertTestimonial: InsertTestimonial, imageFile: Parts.File? = null): TestimonialDto
    suspend fun updateById(
        id: UUID,
        updateTestimonial: UpdateTestimonial,
        imageFile: Parts.File? = null
    ): TestimonialDto
    suspend fun updateTestimonialAvatar(id: UUID, imageFile: Parts.File, altText: String): TestimonialDto
    suspend fun deleteTestimonialAvatar(id: UUID): TestimonialDto

    suspend fun deleteById(id: UUID)
}
