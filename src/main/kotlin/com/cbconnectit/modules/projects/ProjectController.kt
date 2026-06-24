package com.cbconnectit.modules.projects

import com.cbconnectit.data.dto.requests.project.InsertNewProject
import com.cbconnectit.data.dto.requests.project.ProjectDto
import com.cbconnectit.data.dto.requests.project.UpdateProject
import com.cbconnectit.domain.interfaces.ILinkDao
import com.cbconnectit.domain.interfaces.IMediaFileDao
import com.cbconnectit.domain.interfaces.IProjectDao
import com.cbconnectit.domain.interfaces.ITagDao
import com.cbconnectit.domain.models.link.LinkType
import com.cbconnectit.domain.models.mediafile.MediaFile
import com.cbconnectit.domain.models.mediafile.MediaType
import com.cbconnectit.domain.models.mediafile.OwnerType
import com.cbconnectit.domain.models.project.toDto
import com.cbconnectit.plugins.dbTransactionalQuery
import com.cbconnectit.plugins.statuspages.ErrorFailedCreate
import com.cbconnectit.plugins.statuspages.ErrorFailedDelete
import com.cbconnectit.plugins.statuspages.ErrorFailedUpdate
import com.cbconnectit.plugins.statuspages.ErrorInvalidFileType
import com.cbconnectit.plugins.statuspages.ErrorInvalidParameters
import com.cbconnectit.plugins.statuspages.ErrorMissingRequiredMedia
import com.cbconnectit.plugins.statuspages.ErrorNotFound
import com.cbconnectit.plugins.statuspages.ErrorUnknownTagIdsForCreateProject
import com.cbconnectit.plugins.statuspages.ErrorUnknownTagIdsForUpdateProject
import com.cbconnectit.services.MediaStorageService
import com.cbconnectit.services.StorageResult
import com.cbconnectit.utils.FileValidationUtils
import com.cbconnectit.utils.Parts
import io.ktor.http.*
import java.util.*

class ProjectControllerImpl(
    private val projectDao: IProjectDao,
    private val tagDao: ITagDao,
    private val linkDao: ILinkDao,
    private val mediaFileDao: IMediaFileDao,
    private val storageService: MediaStorageService
) : ProjectController {

    override suspend fun getProjects(): List<ProjectDto> = dbTransactionalQuery {
        projectDao.getProjects().map { it.toDto() }
    }

    override suspend fun getProjectById(projectId: UUID): ProjectDto = dbTransactionalQuery {
        projectDao.getProjectById(projectId)?.toDto() ?: throw ErrorNotFound
    }

    override suspend fun postProject(
        insertNewProject: InsertNewProject,
        imageFile: Parts.File,
        bannerImageFile: Parts.File
    ): ProjectDto {
        if (!insertNewProject.isValid) throw ErrorInvalidParameters

        if (!FileValidationUtils.isValidImageFile(imageFile.contentType, imageFile.fileName, imageFile.data)) {
            throw ErrorInvalidFileType
        }
        if (!FileValidationUtils.isValidImageFile(bannerImageFile.contentType, bannerImageFile.fileName, bannerImageFile.data)) {
            throw ErrorInvalidFileType
        }

        // Phase 1: Store files (outside transaction)
        val imageStorageResult = storageService.storeFromBytes(imageFile.data, imageFile.fileName, imageFile.contentType)
        val bannerStorageResult = try {
            storageService.storeFromBytes(bannerImageFile.data, bannerImageFile.fileName, bannerImageFile.contentType)
        } catch (e: Exception) {
            storageService.delete(imageStorageResult.url)
            throw e
        }

        // Phase 2: DB transaction
        return try {
            dbTransactionalQuery {
                val tagUUIDs = insertNewProject.tags?.map { UUID.fromString(it) } ?: emptyList()
                val existingTagUUIDs = tagDao.getListOfExistingTagIds(tagUUIDs)

                if (tagUUIDs.isNotEmpty() && existingTagUUIDs.count() != insertNewProject.tags?.count()) {
                    val nonExistingIds = tagUUIDs.filterNot { existingTagUUIDs.contains(it) }
                    throw ErrorUnknownTagIdsForCreateProject(nonExistingIds)
                }

                val resolvedLinkIds = (insertNewProject.links ?: emptyList()).map { url ->
                    val linkType = LinkType.getTypeByUrl(Url(url))
                    linkDao.getOrInsertLinkByUrl(url, linkType).toString()
                }

                val insertedProject = projectDao.insertProject(insertNewProject.copy(links = resolvedLinkIds)) ?: throw ErrorFailedCreate
                val projectId = insertedProject.id

                mediaFileDao.create(
                    MediaFile(
                        storageResult = imageStorageResult,
                        imageFile = imageFile,
                        ownerId = projectId,
                        ownerType = OwnerType.PROJECT,
                        mediaType = MediaType.IMAGE,
                        altText = insertNewProject.imageAltText ?: ""
                    )
                )
                mediaFileDao.create(
                    MediaFile(
                        storageResult = bannerStorageResult,
                        imageFile = bannerImageFile,
                        ownerId = projectId,
                        ownerType = OwnerType.PROJECT,
                        mediaType = MediaType.BANNER,
                        altText = insertNewProject.bannerImageAltText ?: ""
                    )
                )

                projectDao.getProjectById(projectId)?.toDto() ?: throw ErrorFailedCreate
            }
        } catch (e: Exception) {
            storageService.delete(imageStorageResult.url)
            storageService.delete(bannerStorageResult.url)
            throw e
        }
    }

    override suspend fun updateProjectById(
        projectId: UUID,
        updateProject: UpdateProject,
        imageFile: Parts.File?,
        bannerImageFile: Parts.File?
    ): ProjectDto {
        if (!updateProject.isValid) throw ErrorInvalidParameters

        imageFile?.let {
            if (!FileValidationUtils.isValidImageFile(it.contentType, it.fileName, it.data)) throw ErrorInvalidFileType
        }
        bannerImageFile?.let {
            if (!FileValidationUtils.isValidImageFile(it.contentType, it.fileName, it.data)) throw ErrorInvalidFileType
        }

        // Phase 1: Get existing entity + validate media invariants
        val oldUrls = dbTransactionalQuery {
            projectDao.getProjectById(projectId) ?: throw ErrorNotFound
            val oldImage = if (imageFile != null) mediaFileDao.readByOwnerIdAndMediaType(projectId, OwnerType.PROJECT, MediaType.IMAGE)?.url else null
            val oldBanner = if (bannerImageFile != null) mediaFileDao.readByOwnerIdAndMediaType(projectId, OwnerType.PROJECT, MediaType.BANNER)?.url else null

            if (imageFile == null && mediaFileDao.readByOwnerIdAndMediaType(projectId, OwnerType.PROJECT, MediaType.IMAGE) == null) {
                throw ErrorMissingRequiredMedia
            }
            if (bannerImageFile == null && mediaFileDao.readByOwnerIdAndMediaType(projectId, OwnerType.PROJECT, MediaType.BANNER) == null) {
                throw ErrorMissingRequiredMedia
            }

            Pair(oldImage, oldBanner)
        }

        // Phase 2: Store new files
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
                val tagUUIDs = updateProject.tags?.map { UUID.fromString(it) } ?: emptyList()
                val existingTagUUIDs = tagDao.getListOfExistingTagIds(tagUUIDs)

                if (tagUUIDs.isNotEmpty() && existingTagUUIDs.count() != updateProject.tags?.count()) {
                    val nonExistingIds = tagUUIDs.filterNot { existingTagUUIDs.contains(it) }
                    throw ErrorUnknownTagIdsForUpdateProject(nonExistingIds)
                }

                val resolvedLinkIds = (updateProject.links ?: emptyList()).map { url ->
                    val linkType = LinkType.getTypeByUrl(Url(url))
                    linkDao.getOrInsertLinkByUrl(url, linkType).toString()
                }

                imageStorageResult?.let { result ->
                    val existing = mediaFileDao.readByOwnerIdAndMediaType(projectId, OwnerType.PROJECT, MediaType.IMAGE)
                    existing?.let { mediaFileDao.delete(it.id) }
                    mediaFileDao.create(
                        MediaFile(
                            storageResult = result,
                            imageFile = imageFile,
                            ownerId = projectId,
                            ownerType = OwnerType.PROJECT,
                            mediaType = MediaType.IMAGE,
                            altText = updateProject.imageAltText ?: ""
                        )
                    )
                } ?: updateProject.imageAltText?.let { altText ->
                    mediaFileDao.readByOwnerIdAndMediaType(projectId, OwnerType.PROJECT, MediaType.IMAGE)
                        ?.let { mediaFileDao.update(it.id, altText) }
                }

                bannerStorageResult?.let { result ->
                    val existing = mediaFileDao.readByOwnerIdAndMediaType(projectId, OwnerType.PROJECT, MediaType.BANNER)
                    existing?.let { mediaFileDao.delete(it.id) }
                    mediaFileDao.create(
                        MediaFile(
                            storageResult = result,
                            imageFile = bannerImageFile,
                            ownerId = projectId,
                            ownerType = OwnerType.PROJECT,
                            mediaType = MediaType.BANNER,
                            altText = updateProject.bannerImageAltText ?: ""
                        )
                    )
                } ?: updateProject.bannerImageAltText?.let { altText ->
                    mediaFileDao.readByOwnerIdAndMediaType(projectId, OwnerType.PROJECT, MediaType.BANNER)
                        ?.let { mediaFileDao.update(it.id, altText) }
                }

                projectDao.updateProject(projectId, updateProject.copy(links = resolvedLinkIds))?.toDto() ?: throw ErrorFailedUpdate
            }.also {
                oldUrls.first?.let { storageService.delete(it) }
                oldUrls.second?.let { storageService.delete(it) }
            }
        } catch (e: Exception) {
            imageStorageResult?.let { storageService.delete(it.url) }
            bannerStorageResult?.let { storageService.delete(it.url) }
            throw e
        }
    }

    override suspend fun deleteProjectById(projectId: UUID) {
        val fileUrls = dbTransactionalQuery {
            val image = mediaFileDao.readByOwnerIdAndMediaType(projectId, OwnerType.PROJECT, MediaType.IMAGE)
            val banner = mediaFileDao.readByOwnerIdAndMediaType(projectId, OwnerType.PROJECT, MediaType.BANNER)

            image?.let { mediaFileDao.delete(it.id) }
            banner?.let { mediaFileDao.delete(it.id) }

            val deleted = projectDao.deleteProject(projectId)
            if (!deleted) throw ErrorFailedDelete

            Pair(image?.url, banner?.url)
        }

        fileUrls.first?.let { storageService.delete(it) }
        fileUrls.second?.let { storageService.delete(it) }
    }
}

interface ProjectController {
    suspend fun getProjects(): List<ProjectDto>
    suspend fun getProjectById(projectId: UUID): ProjectDto
    suspend fun postProject(
        insertNewProject: InsertNewProject,
        imageFile: Parts.File,
        bannerImageFile: Parts.File
    ): ProjectDto

    suspend fun updateProjectById(
        projectId: UUID,
        updateProject: UpdateProject,
        imageFile: Parts.File? = null,
        bannerImageFile: Parts.File? = null
    ): ProjectDto

    suspend fun deleteProjectById(projectId: UUID)
}
