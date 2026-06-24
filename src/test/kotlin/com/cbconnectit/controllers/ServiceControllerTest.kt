package com.cbconnectit.controllers

import com.cbconnectit.data.dto.requests.service.ServiceDto
import com.cbconnectit.domain.interfaces.IMediaFileDao
import com.cbconnectit.domain.interfaces.IServiceDao
import com.cbconnectit.domain.interfaces.ITagDao
import com.cbconnectit.domain.models.mediafile.MediaType
import com.cbconnectit.domain.models.mediafile.OwnerType
import com.cbconnectit.instrumentation.MediaFileInstrumentation
import com.cbconnectit.instrumentation.ServiceInstrumentation.givenAService
import com.cbconnectit.instrumentation.ServiceInstrumentation.givenAValidInsertService
import com.cbconnectit.instrumentation.ServiceInstrumentation.givenAValidInsertServiceWithParent
import com.cbconnectit.instrumentation.ServiceInstrumentation.givenAValidUpdateService
import com.cbconnectit.instrumentation.ServiceInstrumentation.givenAValidUpdateServiceWithParent
import com.cbconnectit.instrumentation.ServiceInstrumentation.givenAnInvalidInsertService
import com.cbconnectit.instrumentation.ServiceInstrumentation.givenAnInvalidUpdateService
import com.cbconnectit.modules.services.ServiceController
import com.cbconnectit.modules.services.ServiceControllerImpl
import com.cbconnectit.plugins.statuspages.ErrorFailedCreate
import com.cbconnectit.plugins.statuspages.ErrorFailedDelete
import com.cbconnectit.plugins.statuspages.ErrorFailedUpdate
import com.cbconnectit.plugins.statuspages.ErrorInvalidParameters
import com.cbconnectit.plugins.statuspages.ErrorMissingRequiredMedia
import com.cbconnectit.plugins.statuspages.ErrorNotFound
import com.cbconnectit.plugins.statuspages.ErrorUnknownServiceIdsForCreate
import com.cbconnectit.plugins.statuspages.ErrorUnknownServiceIdsForUpdate
import com.cbconnectit.plugins.statuspages.ErrorUnknownTagIdsForCreate
import com.cbconnectit.plugins.statuspages.ErrorUnknownTagIdsForUpdate
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
class ServiceControllerTest : BaseControllerTest() {

    private val serviceDao: IServiceDao = mockk()
    private val tagDao: ITagDao = mockk()
    private val mediaFileDao: IMediaFileDao = mockk()
    private val storageService: MediaStorageService = mockk()
    private val controller: ServiceController by lazy {
        ServiceControllerImpl(serviceDao, tagDao, mediaFileDao, storageService)
    }

    private fun createValidImageBytes(): ByteArray {
        val image = BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB)
        val graphics = image.createGraphics()
        graphics.fillRect(0, 0, 100, 100)
        graphics.dispose()
        val out = ByteArrayOutputStream()
        ImageIO.write(image, "jpeg", out)
        return out.toByteArray()
    }

    private val mockImageData = createValidImageBytes()
    private val mockImageFile = Parts.File(
        name = "image",
        fileName = "image.jpg",
        contentType = "image/jpeg",
        height = 600,
        width = 800,
        size = mockImageData.size.toLong(),
        data = mockImageData
    )
    private val mockBannerFile = Parts.File(
        name = "bannerImage",
        fileName = "banner.jpg",
        contentType = "image/jpeg",
        height = 400,
        width = 1200,
        size = mockImageData.size.toLong(),
        data = mockImageData
    )
    private val mockStorageResult = StorageResult(
        url = "https://example.com/test.jpg",
        fileSize = 1024L,
        mimeType = "image/jpeg",
        originalFilename = "test.jpg"
    )

    @BeforeEach
    override fun before() {
        super.before()
        clearMocks(serviceDao, tagDao, mediaFileDao, storageService)
    }

    // <editor-fold desc="Get all services">
    @Test
    fun `when requesting all services, we return valid list`() {
        val createdService = givenAService()

        coEvery { serviceDao.getServices() } returns listOf(createdService)

        runBlocking {
            val responseServices = controller.getServices()

            assertThat(responseServices).hasSize(1)
            assertThat(responseServices).allMatch { it is ServiceDto }
        }
    }
    // </editor-fold>

    // <editor-fold desc="Get specific service">
    @Test
    fun `when requesting specific service by ID, we return valid serviceDto`() {
        val createdService = givenAService()

        coEvery { serviceDao.getServiceById(any() as UUID) } returns createdService

        runBlocking {
            val responseService = controller.getServiceById(UUID.randomUUID())

            assertThat(responseService.title).isEqualTo(createdService.title)
            assertNotNull(responseService.createdAt)
            assertNotNull(responseService.updatedAt)
        }
    }

    @Test
    fun `when requesting specific service by ID where the ID does not exist, we throw exception`() {
        coEvery { serviceDao.getServiceById(any() as UUID) } throws ErrorNotFound

        assertThrows<ErrorNotFound> {
            runBlocking { controller.getServiceById(UUID.randomUUID()) }
        }
    }
    // </editor-fold>

    // <editor-fold desc="Create new service">
    @Test
    fun `when creating service with incorrect information, we throw exception`() {
        val postService = givenAnInvalidInsertService()

        assertThrows<ErrorInvalidParameters> {
            runBlocking { controller.postService(postService, mockImageFile, mockBannerFile) }
        }
    }

    @Test
    fun `when creating service with correct information, we return valid serviceDto`() {
        val postService = givenAValidInsertService()
        val createdService = givenAService()

        coEvery { tagDao.getListOfExistingTagIds(any()) } returns listOf(UUID.fromString("00000000-0000-0000-0000-000000000001"))
        coEvery { serviceDao.insertService(any()) } returns createdService
        coEvery { serviceDao.getServiceById(any()) } returns createdService
        coEvery { mediaFileDao.create(any()) } returns UUID.randomUUID()
        coEvery { storageService.storeFromBytes(any(), any(), any()) } returns mockStorageResult

        runBlocking {
            val responseService = controller.postService(postService, mockImageFile, mockBannerFile)

            assertThat(responseService.title).isEqualTo(createdService.title)
        }
    }

    @Test
    fun `when creating service with correct information but tagId does not exist, we throw exception`() {
        val postService = givenAValidInsertService()

        coEvery { tagDao.getListOfExistingTagIds(any()) } returns listOf()
        coEvery { serviceDao.insertService(any()) } returns null
        coEvery { storageService.storeFromBytes(any(), any(), any()) } returns mockStorageResult
        coEvery { storageService.delete(any()) } returns true

        assertThrows<ErrorUnknownTagIdsForCreate> {
            runBlocking { controller.postService(postService, mockImageFile, mockBannerFile) }
        }
    }

    @Test
    fun `when creating specific service but parentServiceId does not exist, we throw exception`() {
        val insertService = givenAValidInsertServiceWithParent()

        coEvery { tagDao.getListOfExistingTagIds(any()) } returns listOf(UUID.fromString("00000000-0000-0000-0000-000000000001"))
        coEvery { serviceDao.getListOfExistingServiceIds(any()) } returns listOf()
        coEvery { serviceDao.insertService(any()) } returns null
        coEvery { storageService.storeFromBytes(any(), any(), any()) } returns mockStorageResult
        coEvery { storageService.delete(any()) } returns true

        assertThrows<ErrorUnknownServiceIdsForCreate> {
            runBlocking { controller.postService(insertService, mockImageFile, mockBannerFile) }
        }
    }

    @Test
    fun `when creating specific service and parentServiceId does exist, we return service with subServices filled in`() {
        val insertService = givenAValidInsertServiceWithParent()
        val createdService = givenAService(subService = givenAService(name = "Sub Service"))

        coEvery { tagDao.getListOfExistingTagIds(any()) } returns listOf(UUID.fromString("00000000-0000-0000-0000-000000000001"))
        coEvery { serviceDao.getListOfExistingServiceIds(any()) } returns listOf(UUID.fromString("00000000-0000-0000-0000-000000000001"))
        coEvery { serviceDao.insertService(any()) } returns createdService
        coEvery { serviceDao.getServiceById(any()) } returns createdService
        coEvery { mediaFileDao.create(any()) } returns UUID.randomUUID()
        coEvery { storageService.storeFromBytes(any(), any(), any()) } returns mockStorageResult

        runBlocking {
            val responseService = controller.postService(insertService, mockImageFile, mockBannerFile)

            assertThat(responseService.title).isEqualTo(createdService.title)
            assertThat(responseService.subServices).hasSize(1)
        }
    }

    @Test
    fun `when creating service and database returns error, we throw exception`() {
        val postService = givenAValidInsertService()

        coEvery { tagDao.getListOfExistingTagIds(any()) } returns listOf(UUID.fromString("00000000-0000-0000-0000-000000000001"))
        coEvery { serviceDao.insertService(any()) } returns null
        coEvery { serviceDao.getServiceById(any()) } returns null
        coEvery { mediaFileDao.create(any()) } returns UUID.randomUUID()
        coEvery { storageService.storeFromBytes(any(), any(), any()) } returns mockStorageResult
        coEvery { storageService.delete(any()) } returns true

        assertThrows<ErrorFailedCreate> {
            runBlocking { controller.postService(postService, mockImageFile, mockBannerFile) }
        }
    }
    // </editor-fold>

    // <editor-fold desc="Update specific service">
    @Test
    fun `when updating specific service, we return valid serviceDto`() {
        val updateService = givenAValidUpdateService()
        val createdService = givenAService()
        val mediaFile = MediaFileInstrumentation.givenAMediaFile(ownerType = OwnerType.SERVICE)

        coEvery { tagDao.getListOfExistingTagIds(any()) } returns listOf(UUID.fromString("00000000-0000-0000-0000-000000000001"))
        coEvery { serviceDao.getServiceById(any()) } returns createdService
        coEvery { serviceDao.updateService(any(), any()) } returns createdService
        coEvery { mediaFileDao.readByOwnerIdAndMediaType(any(), OwnerType.SERVICE, MediaType.IMAGE) } returns mediaFile
        coEvery { mediaFileDao.readByOwnerIdAndMediaType(any(), OwnerType.SERVICE, MediaType.BANNER) } returns mediaFile

        runBlocking {
            val responseService = controller.updateServiceById(UUID.randomUUID(), updateService)

            assertThat(responseService.title).isEqualTo(createdService.title)
        }
    }

    @Test
    fun `when updating service where IMAGE is missing, we throw exception`() {
        val updateService = givenAValidUpdateService()
        val createdService = givenAService()
        val bannerFile = MediaFileInstrumentation.givenAMediaFile(ownerType = OwnerType.SERVICE)

        coEvery { serviceDao.getServiceById(any()) } returns createdService
        coEvery { mediaFileDao.readByOwnerIdAndMediaType(any(), OwnerType.SERVICE, MediaType.IMAGE) } returns null
        coEvery { mediaFileDao.readByOwnerIdAndMediaType(any(), OwnerType.SERVICE, MediaType.BANNER) } returns bannerFile

        assertThrows<ErrorMissingRequiredMedia> {
            runBlocking { controller.updateServiceById(UUID.randomUUID(), updateService) }
        }
    }

    @Test
    fun `when updating service where BANNER is missing, we throw exception`() {
        val updateService = givenAValidUpdateService()
        val createdService = givenAService()
        val imageFile = MediaFileInstrumentation.givenAMediaFile(ownerType = OwnerType.SERVICE)

        coEvery { serviceDao.getServiceById(any()) } returns createdService
        coEvery { mediaFileDao.readByOwnerIdAndMediaType(any(), OwnerType.SERVICE, MediaType.IMAGE) } returns imageFile
        coEvery { mediaFileDao.readByOwnerIdAndMediaType(any(), OwnerType.SERVICE, MediaType.BANNER) } returns null

        assertThrows<ErrorMissingRequiredMedia> {
            runBlocking { controller.updateServiceById(UUID.randomUUID(), updateService) }
        }
    }

    @Test
    fun `when updating service and replacing image, new image is stored and old image deleted`() {
        val updateService = givenAValidUpdateService()
        val createdService = givenAService()
        val existingMedia = MediaFileInstrumentation.givenAMediaFile(ownerType = OwnerType.SERVICE)

        coEvery { tagDao.getListOfExistingTagIds(any()) } returns listOf(UUID.fromString("00000000-0000-0000-0000-000000000001"))
        coEvery { serviceDao.getServiceById(any()) } returns createdService
        coEvery { serviceDao.updateService(any(), any()) } returns createdService
        coEvery { mediaFileDao.readByOwnerIdAndMediaType(any(), OwnerType.SERVICE, MediaType.IMAGE) } returns existingMedia
        coEvery { mediaFileDao.readByOwnerIdAndMediaType(any(), OwnerType.SERVICE, MediaType.BANNER) } returns existingMedia
        coEvery { mediaFileDao.delete(any()) } returns true
        coEvery { mediaFileDao.create(any()) } returns UUID.randomUUID()
        coEvery { storageService.storeFromBytes(any(), any(), any()) } returns mockStorageResult
        coEvery { storageService.delete(any()) } returns true

        runBlocking {
            controller.updateServiceById(UUID.randomUUID(), updateService, mockImageFile)
        }

        coVerify(exactly = 1) { storageService.storeFromBytes(any(), any(), any()) }
        coVerify(atLeast = 1) { storageService.delete(any()) }
    }

    @Test
    fun `when updating specific service but tagId does not exist, we throw exception`() {
        val updateService = givenAValidUpdateService()
        val createdService = givenAService()
        val mediaFile = MediaFileInstrumentation.givenAMediaFile(ownerType = OwnerType.SERVICE)

        coEvery { tagDao.getListOfExistingTagIds(any()) } returns listOf()
        coEvery { serviceDao.getServiceById(any()) } returns createdService
        coEvery { serviceDao.updateService(any(), any()) } returns createdService
        coEvery { mediaFileDao.readByOwnerIdAndMediaType(any(), OwnerType.SERVICE, MediaType.IMAGE) } returns mediaFile
        coEvery { mediaFileDao.readByOwnerIdAndMediaType(any(), OwnerType.SERVICE, MediaType.BANNER) } returns mediaFile

        assertThrows<ErrorUnknownTagIdsForUpdate> {
            runBlocking { controller.updateServiceById(UUID.randomUUID(), updateService) }
        }
    }

    @Test
    fun `when updating specific service but parentServiceId does not exist, we throw exception`() {
        val updateService = givenAValidUpdateServiceWithParent()
        val createdService = givenAService()
        val mediaFile = MediaFileInstrumentation.givenAMediaFile(ownerType = OwnerType.SERVICE)

        coEvery { tagDao.getListOfExistingTagIds(any()) } returns listOf(UUID.fromString("00000000-0000-0000-0000-000000000001"))
        coEvery { serviceDao.getListOfExistingServiceIds(any()) } returns listOf()
        coEvery { serviceDao.getServiceById(any()) } returns createdService
        coEvery { serviceDao.updateService(any(), any()) } returns createdService
        coEvery { mediaFileDao.readByOwnerIdAndMediaType(any(), OwnerType.SERVICE, MediaType.IMAGE) } returns mediaFile
        coEvery { mediaFileDao.readByOwnerIdAndMediaType(any(), OwnerType.SERVICE, MediaType.BANNER) } returns mediaFile

        assertThrows<ErrorUnknownServiceIdsForUpdate> {
            runBlocking { controller.updateServiceById(UUID.randomUUID(), updateService) }
        }
    }

    @Test
    fun `when updating specific service and parentServiceId does exist, we return service with subServices filled in`() {
        val updateService = givenAValidUpdateServiceWithParent()
        val createdService = givenAService(subService = givenAService(name = "Sub Service"))
        val mediaFile = MediaFileInstrumentation.givenAMediaFile(ownerType = OwnerType.SERVICE)

        coEvery { tagDao.getListOfExistingTagIds(any()) } returns listOf(UUID.fromString("00000000-0000-0000-0000-000000000001"))
        coEvery { serviceDao.getListOfExistingServiceIds(any()) } returns listOf(UUID.fromString("00000000-0000-0000-0000-000000000001"))
        coEvery { serviceDao.getServiceById(any()) } returns createdService
        coEvery { serviceDao.updateService(any(), any()) } returns createdService
        coEvery { mediaFileDao.readByOwnerIdAndMediaType(any(), OwnerType.SERVICE, MediaType.IMAGE) } returns mediaFile
        coEvery { mediaFileDao.readByOwnerIdAndMediaType(any(), OwnerType.SERVICE, MediaType.BANNER) } returns mediaFile

        runBlocking {
            val responseService = controller.updateServiceById(UUID.randomUUID(), updateService)

            assertThat(responseService.title).isEqualTo(createdService.title)
            assertThat(responseService.subServices).hasSize(1)
        }
    }

    @Test
    fun `when updating specific service which has invalid data, we throw exception`() {
        val updateService = givenAnInvalidUpdateService()

        assertThrows<ErrorInvalidParameters> {
            runBlocking { controller.updateServiceById(UUID.randomUUID(), updateService) }
        }
    }

    @Test
    fun `when updating specific service which does not exist, we throw exception`() {
        val updateService = givenAValidUpdateService()

        coEvery { serviceDao.getServiceById(any()) } returns null

        assertThrows<ErrorNotFound> {
            runBlocking { controller.updateServiceById(UUID.randomUUID(), updateService) }
        }
    }

    @Test
    fun `when updating specific service and database returns error, we throw exception`() {
        val updateService = givenAValidUpdateService()
        val createdService = givenAService()
        val mediaFile = MediaFileInstrumentation.givenAMediaFile(ownerType = OwnerType.SERVICE)

        coEvery { tagDao.getListOfExistingTagIds(any()) } returns listOf(UUID.fromString("00000000-0000-0000-0000-000000000001"))
        coEvery { serviceDao.getServiceById(any()) } returns createdService
        coEvery { serviceDao.updateService(any(), any()) } throws ErrorFailedUpdate
        coEvery { mediaFileDao.readByOwnerIdAndMediaType(any(), OwnerType.SERVICE, MediaType.IMAGE) } returns mediaFile
        coEvery { mediaFileDao.readByOwnerIdAndMediaType(any(), OwnerType.SERVICE, MediaType.BANNER) } returns mediaFile

        assertThrows<ErrorFailedUpdate> {
            runBlocking { controller.updateServiceById(UUID.randomUUID(), updateService) }
        }
    }
    // </editor-fold>

    // <editor-fold desc="Delete service">
    @Test
    fun `when deleting specific service with both images attached, we delete both media files and return success`() {
        val imageMedia = MediaFileInstrumentation.givenAMediaFile(
            id = "00000000-0000-0000-0000-000000000001",
            ownerType = OwnerType.SERVICE
        )
        val bannerMedia = MediaFileInstrumentation.givenAMediaFile(
            id = "00000000-0000-0000-0000-000000000002",
            ownerType = OwnerType.SERVICE
        )

        coEvery { mediaFileDao.readByOwnerIdAndMediaType(any(), OwnerType.SERVICE, MediaType.IMAGE) } returns imageMedia
        coEvery { mediaFileDao.readByOwnerIdAndMediaType(any(), OwnerType.SERVICE, MediaType.BANNER) } returns bannerMedia
        coEvery { mediaFileDao.delete(any()) } returns true
        coEvery { serviceDao.deleteService(any()) } returns true
        coEvery { storageService.delete(any()) } returns true

        assertDoesNotThrow {
            runBlocking { controller.deleteServiceById(UUID.randomUUID()) }
        }

        verify(exactly = 2) { mediaFileDao.delete(any()) }
        coVerify(exactly = 2) { storageService.delete(any()) }
    }

    @Test
    fun `when deleting specific service, we return true`() {
        coEvery { mediaFileDao.readByOwnerIdAndMediaType(any(), OwnerType.SERVICE, MediaType.IMAGE) } returns null
        coEvery { mediaFileDao.readByOwnerIdAndMediaType(any(), OwnerType.SERVICE, MediaType.BANNER) } returns null
        coEvery { serviceDao.deleteService(any()) } returns true

        assertDoesNotThrow {
            runBlocking { controller.deleteServiceById(UUID.randomUUID()) }
        }
    }

    @Test
    fun `when deleting specific service which does not exist, we throw exception`() {
        coEvery { mediaFileDao.readByOwnerIdAndMediaType(any(), OwnerType.SERVICE, MediaType.IMAGE) } returns null
        coEvery { mediaFileDao.readByOwnerIdAndMediaType(any(), OwnerType.SERVICE, MediaType.BANNER) } returns null
        coEvery { serviceDao.deleteService(any()) } returns false

        assertThrows<ErrorFailedDelete> {
            runBlocking { controller.deleteServiceById(UUID.randomUUID()) }
        }
    }
    // </editor-fold>
}
