package com.cbconnectit.controllers

import com.cbconnectit.data.dto.requests.project.ProjectDto
import com.cbconnectit.domain.interfaces.ILinkDao
import com.cbconnectit.domain.interfaces.IMediaFileDao
import com.cbconnectit.domain.interfaces.IProjectDao
import com.cbconnectit.domain.interfaces.ITagDao
import com.cbconnectit.domain.models.mediafile.MediaType
import com.cbconnectit.domain.models.mediafile.OwnerType
import com.cbconnectit.instrumentation.MediaFileInstrumentation
import com.cbconnectit.instrumentation.ProjectInstrumentation.givenAProject
import com.cbconnectit.instrumentation.ProjectInstrumentation.givenAValidInsertProject
import com.cbconnectit.instrumentation.ProjectInstrumentation.givenAValidUpdateProject
import com.cbconnectit.instrumentation.ProjectInstrumentation.givenAnInvalidInsertProject
import com.cbconnectit.instrumentation.ProjectInstrumentation.givenAnInvalidUpdateProject
import com.cbconnectit.modules.projects.ProjectController
import com.cbconnectit.modules.projects.ProjectControllerImpl
import com.cbconnectit.plugins.statuspages.ErrorFailedCreate
import com.cbconnectit.plugins.statuspages.ErrorFailedDelete
import com.cbconnectit.plugins.statuspages.ErrorFailedUpdate
import com.cbconnectit.plugins.statuspages.ErrorInvalidParameters
import com.cbconnectit.plugins.statuspages.ErrorMissingRequiredMedia
import com.cbconnectit.plugins.statuspages.ErrorNotFound
import com.cbconnectit.plugins.statuspages.ErrorUnknownTagIdsForCreateProject
import com.cbconnectit.plugins.statuspages.ErrorUnknownTagIdsForUpdateProject
import com.cbconnectit.services.MediaStorageService
import com.cbconnectit.services.StorageResult
import com.cbconnectit.utils.Parts
import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.util.*
import javax.imageio.ImageIO
import kotlin.test.assertNotNull

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ProjectControllerTest : BaseControllerTest() {

    private val projectDao: IProjectDao = mockk()
    private val tagDao: ITagDao = mockk()
    private val linkDao: ILinkDao = mockk()
    private val mediaFileDao: IMediaFileDao = mockk()
    private val storageService: MediaStorageService = mockk()
    private val controller: ProjectController by lazy {
        ProjectControllerImpl(projectDao, tagDao, linkDao, mediaFileDao, storageService)
    }

    private fun createValidImageBytes(): ByteArray {
        val image = BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB)
        val g = image.createGraphics(); g.fillRect(0, 0, 100, 100); g.dispose()
        val out = ByteArrayOutputStream()
        ImageIO.write(image, "jpeg", out)
        return out.toByteArray()
    }

    private val mockImageData = createValidImageBytes()
    private val mockImageFile = Parts.File("image", "image.jpg", "image/jpeg", 600, 800, mockImageData.size.toLong(), mockImageData)
    private val mockBannerFile = Parts.File("bannerImage", "banner.jpg", "image/jpeg", 400, 1200, mockImageData.size.toLong(), mockImageData)
    private val mockStorageResult = StorageResult("https://example.com/test.jpg", 1024L, "image/jpeg", "test.jpg")

    @BeforeEach
    override fun before() {
        super.before()
        clearMocks(projectDao, tagDao, linkDao, mediaFileDao, storageService)
    }

    // <editor-fold desc="Get all projects">
    @Test
    fun `when requesting all projects, we return valid list`() {
        coEvery { projectDao.getProjects() } returns listOf(givenAProject())

        runBlocking {
            val responseProjects = controller.getProjects()
            assertThat(responseProjects).hasSize(1)
            assertThat(responseProjects).allMatch { it is ProjectDto }
        }
    }
    // </editor-fold>

    // <editor-fold desc="Get specific project">
    @Test
    fun `when requesting specific project by ID, we return valid projectDto`() {
        val createdProject = givenAProject()
        coEvery { projectDao.getProjectById(any() as UUID) } returns createdProject

        runBlocking {
            val responseProject = controller.getProjectById(UUID.randomUUID())
            assertThat(responseProject.title).isEqualTo(createdProject.title)
            assertNotNull(responseProject.createdAt)
            assertNotNull(responseProject.updatedAt)
        }
    }

    @Test
    fun `when requesting specific project by ID where the ID does not exist, we throw exception`() {
        coEvery { projectDao.getProjectById(any() as UUID) } throws ErrorNotFound

        assertThrows<ErrorNotFound> {
            runBlocking { controller.getProjectById(UUID.randomUUID()) }
        }
    }
    // </editor-fold>

    // <editor-fold desc="Create new project">
    @Test
    fun `when creating project with incorrect information, we throw exception`() {
        assertThrows<ErrorInvalidParameters> {
            runBlocking { controller.postProject(givenAnInvalidInsertProject(), mockImageFile, mockBannerFile) }
        }
    }

    @Test
    fun `when creating project with correct information, we return valid projectDto`() {
        val createdProject = givenAProject()

        coEvery { tagDao.getListOfExistingTagIds(any()) } returns listOf(UUID.fromString("00000000-0000-0000-0000-000000000001"))
        coEvery { linkDao.getOrInsertLinkByUrl(any(), any()) } returns UUID.randomUUID()
        coEvery { projectDao.insertProject(any()) } returns createdProject
        coEvery { projectDao.getProjectById(any()) } returns createdProject
        coEvery { mediaFileDao.create(any()) } returns UUID.randomUUID()
        coEvery { storageService.storeFromBytes(any(), any(), any()) } returns mockStorageResult

        runBlocking {
            val responseProject = controller.postProject(givenAValidInsertProject(), mockImageFile, mockBannerFile)
            assertThat(responseProject.title).isEqualTo(createdProject.title)
        }
    }

    @Test
    fun `when creating project with correct information but tagId does not exist, we throw exception`() {
        coEvery { tagDao.getListOfExistingTagIds(any()) } returns listOf()
        coEvery { linkDao.getOrInsertLinkByUrl(any(), any()) } returns UUID.randomUUID()
        coEvery { projectDao.insertProject(any()) } returns null
        coEvery { storageService.storeFromBytes(any(), any(), any()) } returns mockStorageResult
        coEvery { storageService.delete(any()) } returns true

        assertThrows<ErrorUnknownTagIdsForCreateProject> {
            runBlocking { controller.postProject(givenAValidInsertProject(), mockImageFile, mockBannerFile) }
        }
    }

    @Test
    fun `when creating project with an invalid link url, we throw exception`() {
        assertThrows<ErrorInvalidParameters> {
            runBlocking {
                controller.postProject(
                    givenAValidInsertProject().copy(links = listOf("not-a-valid-url")),
                    mockImageFile, mockBannerFile
                )
            }
        }
    }

    @Test
    fun `when creating project and database returns error, we throw exception`() {
        coEvery { tagDao.getListOfExistingTagIds(any()) } returns listOf(UUID.fromString("00000000-0000-0000-0000-000000000001"))
        coEvery { linkDao.getOrInsertLinkByUrl(any(), any()) } returns UUID.randomUUID()
        coEvery { projectDao.insertProject(any()) } returns null
        coEvery { projectDao.getProjectById(any()) } returns null
        coEvery { mediaFileDao.create(any()) } returns UUID.randomUUID()
        coEvery { storageService.storeFromBytes(any(), any(), any()) } returns mockStorageResult
        coEvery { storageService.delete(any()) } returns true

        assertThrows<ErrorFailedCreate> {
            runBlocking { controller.postProject(givenAValidInsertProject(), mockImageFile, mockBannerFile) }
        }
    }
    // </editor-fold>

    // <editor-fold desc="Update specific project">
    @Test
    fun `when updating specific project, we return valid projectDto`() {
        val createdProject = givenAProject()
        val mediaFile = MediaFileInstrumentation.givenAMediaFile(ownerType = OwnerType.PROJECT)

        coEvery { tagDao.getListOfExistingTagIds(any()) } returns listOf(UUID.fromString("00000000-0000-0000-0000-000000000001"))
        coEvery { linkDao.getOrInsertLinkByUrl(any(), any()) } returns UUID.randomUUID()
        coEvery { projectDao.getProjectById(any()) } returns createdProject
        coEvery { projectDao.updateProject(any(), any()) } returns createdProject
        coEvery { mediaFileDao.readByOwnerIdAndMediaType(any(), OwnerType.PROJECT, MediaType.IMAGE) } returns mediaFile
        coEvery { mediaFileDao.readByOwnerIdAndMediaType(any(), OwnerType.PROJECT, MediaType.BANNER) } returns mediaFile

        runBlocking {
            val responseProject = controller.updateProjectById(UUID.randomUUID(), givenAValidUpdateProject())
            assertThat(responseProject.title).isEqualTo(createdProject.title)
        }
    }

    @Test
    fun `when updating project where IMAGE is missing, we throw exception`() {
        val createdProject = givenAProject()
        val bannerFile = MediaFileInstrumentation.givenAMediaFile(ownerType = OwnerType.PROJECT)

        coEvery { projectDao.getProjectById(any()) } returns createdProject
        coEvery { mediaFileDao.readByOwnerIdAndMediaType(any(), OwnerType.PROJECT, MediaType.IMAGE) } returns null
        coEvery { mediaFileDao.readByOwnerIdAndMediaType(any(), OwnerType.PROJECT, MediaType.BANNER) } returns bannerFile

        assertThrows<ErrorMissingRequiredMedia> {
            runBlocking { controller.updateProjectById(UUID.randomUUID(), givenAValidUpdateProject()) }
        }
    }

    @Test
    fun `when updating project where BANNER is missing, we throw exception`() {
        val createdProject = givenAProject()
        val imageFile = MediaFileInstrumentation.givenAMediaFile(ownerType = OwnerType.PROJECT)

        coEvery { projectDao.getProjectById(any()) } returns createdProject
        coEvery { mediaFileDao.readByOwnerIdAndMediaType(any(), OwnerType.PROJECT, MediaType.IMAGE) } returns imageFile
        coEvery { mediaFileDao.readByOwnerIdAndMediaType(any(), OwnerType.PROJECT, MediaType.BANNER) } returns null

        assertThrows<ErrorMissingRequiredMedia> {
            runBlocking { controller.updateProjectById(UUID.randomUUID(), givenAValidUpdateProject()) }
        }
    }

    @Test
    fun `when replacing project banner image successfully, we delete old banner storage url`() {
        val projectId = UUID.randomUUID()
        val createdProject = givenAProject(id = projectId)
        val existingImage = MediaFileInstrumentation.givenAMediaFile(
            id = "00000000-0000-0000-0000-000000000101",
            ownerType = OwnerType.PROJECT
        ).copy(url = "https://example.com/images/existing-image.jpg", mediaType = MediaType.IMAGE)
        val existingBanner = MediaFileInstrumentation.givenAMediaFile(
            id = "00000000-0000-0000-0000-000000000102",
            ownerType = OwnerType.PROJECT
        ).copy(url = "https://example.com/images/old-banner.jpg", mediaType = MediaType.BANNER)
        val newBannerStorage = StorageResult("https://example.com/images/new-banner.jpg", 1024L, "image/jpeg", "new-banner.jpg")

        coEvery { tagDao.getListOfExistingTagIds(any()) } returns listOf(UUID.fromString("00000000-0000-0000-0000-000000000001"))
        coEvery { linkDao.getOrInsertLinkByUrl(any(), any()) } returns UUID.randomUUID()
        coEvery { projectDao.getProjectById(projectId) } returns createdProject
        coEvery { projectDao.updateProject(projectId, any()) } returns createdProject
        coEvery { storageService.storeFromBytes(any(), any(), any()) } returns newBannerStorage
        coEvery { storageService.delete(existingBanner.url) } returns true
        coEvery { mediaFileDao.readByOwnerIdAndMediaType(projectId, OwnerType.PROJECT, MediaType.IMAGE) } returns existingImage
        coEvery {
            mediaFileDao.readByOwnerIdAndMediaType(projectId, OwnerType.PROJECT, MediaType.BANNER)
        } returnsMany listOf(existingBanner, existingBanner)
        coEvery { mediaFileDao.delete(existingBanner.id) } returns true
        coEvery { mediaFileDao.create(any()) } returns UUID.randomUUID()

        runBlocking {
            controller.updateProjectById(
                projectId = projectId,
                updateProject = givenAValidUpdateProject(),
                imageFile = null,
                bannerImageFile = mockBannerFile
            )
        }

        coVerify(exactly = 1) { storageService.delete(existingBanner.url) }
    }

    @Test
    fun `when updating specific project but tagId does not exist, we throw exception`() {
        val createdProject = givenAProject()
        val mediaFile = MediaFileInstrumentation.givenAMediaFile(ownerType = OwnerType.PROJECT)

        coEvery { tagDao.getListOfExistingTagIds(any()) } returns listOf()
        coEvery { linkDao.getOrInsertLinkByUrl(any(), any()) } returns UUID.randomUUID()
        coEvery { projectDao.getProjectById(any()) } returns createdProject
        coEvery { projectDao.updateProject(any(), any()) } returns createdProject
        coEvery { mediaFileDao.readByOwnerIdAndMediaType(any(), OwnerType.PROJECT, MediaType.IMAGE) } returns mediaFile
        coEvery { mediaFileDao.readByOwnerIdAndMediaType(any(), OwnerType.PROJECT, MediaType.BANNER) } returns mediaFile

        assertThrows<ErrorUnknownTagIdsForUpdateProject> {
            runBlocking { controller.updateProjectById(UUID.randomUUID(), givenAValidUpdateProject()) }
        }
    }

    @Test
    fun `when updating specific project with an invalid link url, we throw exception`() {
        assertThrows<ErrorInvalidParameters> {
            runBlocking {
                controller.updateProjectById(
                    UUID.randomUUID(),
                    givenAValidUpdateProject().copy(links = listOf("not-a-valid-url"))
                )
            }
        }
    }

    @Test
    fun `when updating specific project which has invalid data, we throw exception`() {
        assertThrows<ErrorInvalidParameters> {
            runBlocking { controller.updateProjectById(UUID.randomUUID(), givenAnInvalidUpdateProject()) }
        }
    }

    @Test
    fun `when updating specific project which does not exist, we throw exception`() {
        coEvery { projectDao.getProjectById(any()) } returns null

        assertThrows<ErrorNotFound> {
            runBlocking { controller.updateProjectById(UUID.randomUUID(), givenAValidUpdateProject()) }
        }
    }

    @Test
    fun `when updating specific project and database returns error, we throw exception`() {
        val createdProject = givenAProject()
        val mediaFile = MediaFileInstrumentation.givenAMediaFile(ownerType = OwnerType.PROJECT)

        coEvery { tagDao.getListOfExistingTagIds(any()) } returns listOf(UUID.fromString("00000000-0000-0000-0000-000000000001"))
        coEvery { linkDao.getOrInsertLinkByUrl(any(), any()) } returns UUID.randomUUID()
        coEvery { projectDao.getProjectById(any()) } returns createdProject
        coEvery { projectDao.updateProject(any(), any()) } throws ErrorFailedUpdate
        coEvery { mediaFileDao.readByOwnerIdAndMediaType(any(), OwnerType.PROJECT, MediaType.IMAGE) } returns mediaFile
        coEvery { mediaFileDao.readByOwnerIdAndMediaType(any(), OwnerType.PROJECT, MediaType.BANNER) } returns mediaFile

        assertThrows<ErrorFailedUpdate> {
            runBlocking { controller.updateProjectById(UUID.randomUUID(), givenAValidUpdateProject()) }
        }
    }
    // </editor-fold>

    // <editor-fold desc="Delete project">
    @Test
    fun `when deleting project with both images attached, we delete both media files`() {
        val imageMedia = MediaFileInstrumentation.givenAMediaFile(id = "00000000-0000-0000-0000-000000000001", ownerType = OwnerType.PROJECT)
        val bannerMedia = MediaFileInstrumentation.givenAMediaFile(id = "00000000-0000-0000-0000-000000000002", ownerType = OwnerType.PROJECT)

        coEvery { mediaFileDao.readByOwnerIdAndMediaType(any(), OwnerType.PROJECT, MediaType.IMAGE) } returns imageMedia
        coEvery { mediaFileDao.readByOwnerIdAndMediaType(any(), OwnerType.PROJECT, MediaType.BANNER) } returns bannerMedia
        coEvery { mediaFileDao.delete(any()) } returns true
        coEvery { projectDao.deleteProject(any()) } returns true
        coEvery { storageService.delete(any()) } returns true

        assertDoesNotThrow {
            runBlocking { controller.deleteProjectById(UUID.randomUUID()) }
        }

        verify(exactly = 2) { mediaFileDao.delete(any()) }
        coVerify(exactly = 2) { storageService.delete(any()) }
    }

    @Test
    fun `when deleting specific project, we return true`() {
        coEvery { mediaFileDao.readByOwnerIdAndMediaType(any(), OwnerType.PROJECT, MediaType.IMAGE) } returns null
        coEvery { mediaFileDao.readByOwnerIdAndMediaType(any(), OwnerType.PROJECT, MediaType.BANNER) } returns null
        coEvery { projectDao.deleteProject(any()) } returns true

        assertDoesNotThrow {
            runBlocking { controller.deleteProjectById(UUID.randomUUID()) }
        }
    }

    @Test
    fun `when deleting specific project which does not exist, we throw exception`() {
        coEvery { mediaFileDao.readByOwnerIdAndMediaType(any(), OwnerType.PROJECT, MediaType.IMAGE) } returns null
        coEvery { mediaFileDao.readByOwnerIdAndMediaType(any(), OwnerType.PROJECT, MediaType.BANNER) } returns null
        coEvery { projectDao.deleteProject(any()) } returns false

        assertThrows<ErrorFailedDelete> {
            runBlocking { controller.deleteProjectById(UUID.randomUUID()) }
        }
    }
    // </editor-fold>
}
