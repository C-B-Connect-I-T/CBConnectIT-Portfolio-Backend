package com.cbconnectit.controllers

import com.cbconnectit.domain.interfaces.IMediaFileDao
import com.cbconnectit.domain.models.mediafile.OwnerType
import com.cbconnectit.instrumentation.MediaFileInstrumentation
import com.cbconnectit.modules.mediafile.MediaFileController
import com.cbconnectit.modules.mediafile.MediaFileControllerImpl
import com.cbconnectit.plugins.statuspages.ErrorInvalidFileType
import com.cbconnectit.plugins.statuspages.ErrorInvalidUUID
import com.cbconnectit.plugins.statuspages.ErrorNotFound
import com.cbconnectit.services.MediaStorageService
import com.cbconnectit.services.StorageResult
import com.cbconnectit.utils.Parts
import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.util.*
import javax.imageio.ImageIO
import kotlin.test.assertFailsWith

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MediaFileControllerTest : BaseControllerTest() {

    private val mediaFileDao: IMediaFileDao = mockk()
    private val storageService: MediaStorageService = mockk()
    private val controller: MediaFileController by lazy {
        MediaFileControllerImpl(mediaFileDao, storageService)
    }

    // Helper function to create a valid image file as bytes
    private fun createValidImageBytes(): ByteArray {
        val image = BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB)
        val graphics = image.createGraphics()
        graphics.fillRect(0, 0, 100, 100)
        graphics.dispose()

        val outputStream = ByteArrayOutputStream()
        ImageIO.write(image, "jpeg", outputStream)
        return outputStream.toByteArray()
    }

    private val mockFileData = createValidImageBytes()
    private val mockFile = Parts.File(
        name = "image",
        fileName = "test.jpg",
        contentType = "image/jpeg",
        height = 600,
        width = 800,
        size = mockFileData.size.toLong(),
        data = mockFileData
    )

    private val mockStorageResult = StorageResult(
        url = "https://example.com/test.jpg",
        fileSize = 1024L,
        mimeType = "image/jpeg",
        originalFilename = "test.jpg"
    )

    @BeforeEach
    fun setupBeforeEach() {
        clearMocks(mediaFileDao, storageService)
    }

    @Test
    fun `when dao throws unexpected exception, it propagates`() = runTest {
        val insertRequest = MediaFileInstrumentation.givenAValidInsertMediaFile()

        coEvery { mediaFileDao.readByOwnerId(any(), any()) } returns null
        coEvery { storageService.storeFromBytes(any(), any(), any()) } returns mockStorageResult
        coEvery { mediaFileDao.create(any()) } throws RuntimeException("DB exploded")

        assertFailsWith<RuntimeException> {
            controller.create(insertRequest, mockFile)
        }
    }

    // <editor-fold desc="Create">
    @Test
    fun `when creating media file with valid data, we return valid MediaFileDto`() = runTest {
        val insertRequest = MediaFileInstrumentation.givenAValidInsertMediaFile()
        val createdMediaFile = MediaFileInstrumentation.givenAMediaFile()

        coEvery { mediaFileDao.readByOwnerId(any(), any()) } returns null
        coEvery { storageService.storeFromBytes(any(), any(), any()) } returns mockStorageResult
        coEvery { mediaFileDao.create(any()) } returns createdMediaFile.id

        val result = controller.create(insertRequest, mockFile)

        assertThat(result).isNotNull
        assertThat(result.id).isNotEmpty()
        assertThat(result.url).isEqualTo(mockStorageResult.url)
        assertThat(result.ownerId).isEqualTo(insertRequest.ownerId)
        assertThat(result.ownerType).isEqualTo(insertRequest.ownerType)

        coVerify { mediaFileDao.readByOwnerId(UUID.fromString(insertRequest.ownerId), insertRequest.ownerType) }
        coVerify { storageService.storeFromBytes(any(), "test.jpg", "image/jpeg") }
        coVerify { mediaFileDao.create(any()) }
    }

    @Test
    fun `when creating media file, existing file is deleted first`() = runTest {
        val insertRequest = MediaFileInstrumentation.givenAValidInsertMediaFile()
        val existingMediaFile = MediaFileInstrumentation.givenAMediaFile()
        val createdMediaFile = MediaFileInstrumentation.givenAMediaFile()

        coEvery { mediaFileDao.readByOwnerId(any(), any()) } returns existingMediaFile
        coEvery { storageService.delete(any()) } returns true
        coEvery { mediaFileDao.delete(any()) } returns true
        coEvery { storageService.storeFromBytes(any(), any(), any()) } returns mockStorageResult
        coEvery { mediaFileDao.create(any()) } returns createdMediaFile.id

        controller.create(insertRequest, mockFile)

        coVerify { mediaFileDao.readByOwnerId(UUID.fromString(insertRequest.ownerId), insertRequest.ownerType) }
        coVerify { storageService.delete(existingMediaFile.url) }
        coVerify { mediaFileDao.delete(existingMediaFile.id) }
    }

    @Test
    fun `when creating media file with invalid content type, we throw ErrorInvalidFileType`() = runTest {
        val insertRequest = MediaFileInstrumentation.givenAValidInsertMediaFile()
        val invalidFile = mockFile.copy(contentType = "text/html")

        assertFailsWith<ErrorInvalidFileType> {
            controller.create(insertRequest, invalidFile)
        }
    }

    @Test
    fun `when creating media file with invalid extension, we throw ErrorInvalidFileType`() = runTest {
        val insertRequest = MediaFileInstrumentation.givenAValidInsertMediaFile()
        val invalidFile = mockFile.copy(fileName = "test.exe")

        assertFailsWith<ErrorInvalidFileType> {
            controller.create(insertRequest, invalidFile)
        }
    }

    @Test
    fun `when creating media file with spoofed content type, we throw ErrorInvalidFileType`() = runTest {
        val insertRequest = MediaFileInstrumentation.givenAValidInsertMediaFile()
        // HTML file with image content type - should be rejected by byte validation
        val htmlBytes = "<html><script>alert('XSS')</script></html>".toByteArray()
        val spoofedFile = Parts.File(
            name = "image",
            fileName = "malicious.jpg",
            contentType = "image/jpeg",
            height = null,
            width = null,
            size = htmlBytes.size.toLong(),
            data = htmlBytes
        )

        assertFailsWith<ErrorInvalidFileType> {
            controller.create(insertRequest, spoofedFile)
        }
    }

    @Test
    fun `when creating media file with JavaScript content, we throw ErrorInvalidFileType`() = runTest {
        val insertRequest = MediaFileInstrumentation.givenAValidInsertMediaFile()
        val jsBytes = "alert('XSS');".toByteArray()
        val jsFile = Parts.File(
            name = "image",
            fileName = "malicious.js",
            contentType = "application/javascript",
            height = null,
            width = null,
            size = jsBytes.size.toLong(),
            data = jsBytes
        )

        assertFailsWith<ErrorInvalidFileType> {
            controller.create(insertRequest, jsFile)
        }
    }

    @Test
    fun `when creating media file with invalid owner_id UUID, we throw ErrorInvalidUUID`() = runTest {
        val invalidInsertRequest = MediaFileInstrumentation.givenAValidInsertMediaFile()
            .copy(ownerId = "not-a-valid-uuid")

        assertFailsWith<ErrorInvalidUUID> {
            controller.create(invalidInsertRequest, mockFile)
        }
    }
    // </editor-fold>

    // <editor-fold desc="ReadById">
    @Test
    fun `when requesting specific media file that exists, we return MediaFileDto`() = runTest {
        val mediaFile = MediaFileInstrumentation.givenAMediaFile()

        coEvery { mediaFileDao.readById(any()) } returns mediaFile

        val result = controller.readById(mediaFile.id)

        assertThat(result).isNotNull
        assertThat(result.id).isEqualTo(mediaFile.id.toString())
        assertThat(result.url).isEqualTo(mediaFile.url)
    }

    @Test
    fun `when requesting specific media file that does not exist, we throw ErrorNotFound`() = runTest {
        coEvery { mediaFileDao.readById(any()) } returns null

        assertFailsWith<ErrorNotFound> {
            controller.readById(UUID.randomUUID())
        }
    }
    // </editor-fold>

    // <editor-fold desc="ReadByOwnerId">
    @Test
    fun `when requesting media file by owner that exists, we return MediaFileDto`() = runTest {
        val mediaFile = MediaFileInstrumentation.givenAMediaFile()

        coEvery { mediaFileDao.readByOwnerId(any(), any()) } returns mediaFile

        val result = controller.readByOwnerId(mediaFile.ownerId, OwnerType.TESTIMONIAL)

        assertThat(result).isNotNull
        assertThat(result?.ownerId).isEqualTo(mediaFile.ownerId.toString())
    }

    @Test
    fun `when requesting media file by owner that does not exist, we return null`() = runTest {
        coEvery { mediaFileDao.readByOwnerId(any(), any()) } returns null

        val result = controller.readByOwnerId(UUID.randomUUID(), OwnerType.TESTIMONIAL)

        assertThat(result).isNull()
    }
    // </editor-fold>

    // <editor-fold desc="ReadAll">
    @Test
    fun `when requesting all media files, we return valid list`() = runTest {
        val mediaFileList = MediaFileInstrumentation.givenMediaFileList()

        coEvery { mediaFileDao.readAll() } returns mediaFileList

        val result = controller.readAll()

        assertThat(result).hasSize(4)
        assertThat(result[0].id).isNotEmpty()
    }

    @Test
    fun `when requesting all media files but none exist, we return empty list`() = runTest {
        coEvery { mediaFileDao.readAll() } returns emptyList()

        val result = controller.readAll()

        assertThat(result).isEmpty()
    }
    // </editor-fold>

    // <editor-fold desc="Update">
    @Test
    fun `when updating media file with valid data, we return updated MediaFileDto`() = runTest {
        val mediaFile = MediaFileInstrumentation.givenAMediaFile()
        val updateRequest = MediaFileInstrumentation.givenAValidUpdateMediaFile()

        coEvery { mediaFileDao.update(any(), any()) } returns true
        coEvery { mediaFileDao.readById(any()) } returns mediaFile

        val result = controller.update(mediaFile.id, updateRequest)

        assertThat(result).isNotNull
        assertThat(result.id).isEqualTo(mediaFile.id.toString())
    }

    @Test
    fun `when updating media file that does not exist, we throw ErrorNotFound`() = runTest {
        val updateRequest = MediaFileInstrumentation.givenAValidUpdateMediaFile()

        coEvery { mediaFileDao.update(any(), any()) } returns false

        assertFailsWith<ErrorNotFound> {
            controller.update(UUID.randomUUID(), updateRequest)
        }
    }

    @Test
    fun `when updating media file but cannot find updated record, we throw ErrorNotFound`() = runTest {
        val updateRequest = MediaFileInstrumentation.givenAValidUpdateMediaFile()

        coEvery { mediaFileDao.update(any(), any()) } returns true
        coEvery { mediaFileDao.readById(any()) } returns null

        assertFailsWith<ErrorNotFound> {
            controller.update(UUID.randomUUID(), updateRequest)
        }
    }
    // </editor-fold>

    // <editor-fold desc="Delete">
    @Test
    fun `when deleting media file that exists, we delete from storage and database`() = runTest {
        val mediaFile = MediaFileInstrumentation.givenAMediaFile()

        coEvery { mediaFileDao.readById(any()) } returns mediaFile
        coEvery { storageService.delete(any()) } returns true
        coEvery { mediaFileDao.delete(any()) } returns true

        val result = controller.delete(mediaFile.id)

        assertThat(result).isTrue()
        coVerify { storageService.delete(mediaFile.url) }
        coVerify { mediaFileDao.delete(mediaFile.id) }
    }

    @Test
    fun `when deleting media file that does not exist, we throw ErrorNotFound`() = runTest {
        coEvery { mediaFileDao.readById(any()) } returns null

        assertFailsWith<ErrorNotFound> {
            controller.delete(UUID.randomUUID())
        }
    }
    // </editor-fold>
}
