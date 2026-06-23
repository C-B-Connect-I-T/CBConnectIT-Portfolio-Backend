package com.cbconnectit.controllers

import com.cbconnectit.data.dto.requests.testimonial.TestimonialDto
import com.cbconnectit.domain.interfaces.ICompanyDao
import com.cbconnectit.domain.interfaces.IJobPositionDao
import com.cbconnectit.domain.interfaces.IMediaFileDao
import com.cbconnectit.domain.interfaces.ITestimonialDao
import com.cbconnectit.domain.models.mediafile.OwnerType
import com.cbconnectit.instrumentation.MediaFileInstrumentation
import com.cbconnectit.instrumentation.TestimonialInstrumentation.givenATestimonial
import com.cbconnectit.instrumentation.TestimonialInstrumentation.givenAValidInsertTestimonial
import com.cbconnectit.instrumentation.TestimonialInstrumentation.givenAValidUpdateTestimonial
import com.cbconnectit.instrumentation.TestimonialInstrumentation.givenAnInvalidInsertTestimonial
import com.cbconnectit.instrumentation.TestimonialInstrumentation.givenAnInvalidUpdateTestimonial
import com.cbconnectit.modules.testimonials.TestimonialController
import com.cbconnectit.modules.testimonials.TestimonialControllerImpl
import com.cbconnectit.plugins.statuspages.ErrorFailedCreate
import com.cbconnectit.plugins.statuspages.ErrorFailedDelete
import com.cbconnectit.plugins.statuspages.ErrorInvalidParameters
import com.cbconnectit.plugins.statuspages.ErrorNotFound
import com.cbconnectit.plugins.statuspages.ErrorUnknownCompanyIdsForCreateTestimonial
import com.cbconnectit.plugins.statuspages.ErrorUnknownCompanyIdsForUpdateTestimonial
import com.cbconnectit.plugins.statuspages.ErrorUnknownJobPositionIdsForCreateTestimonial
import com.cbconnectit.plugins.statuspages.ErrorUnknownJobPositionIdsForUpdateTestimonial
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
class TestimonialControllerTest : BaseControllerTest() {

    private val testimonialDao: ITestimonialDao = mockk()
    private val companyDao: ICompanyDao = mockk()
    private val jobPositionDao: IJobPositionDao = mockk()
    private val mediaFileDao: IMediaFileDao = mockk()
    private val storageService: MediaStorageService = mockk()
    private val controller: TestimonialController by lazy { TestimonialControllerImpl(testimonialDao, companyDao, jobPositionDao, mediaFileDao, storageService) }

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
    override fun before() {
        super.before()
        clearMocks(testimonialDao, companyDao, jobPositionDao, mediaFileDao, storageService)
    }

    // <editor-fold desc="Get all testimonials">
    @Test
    fun `when requesting all testimonials, we return valid list`() {
        val createdTestimonial = givenATestimonial()

        coEvery { testimonialDao.readAll() } returns listOf(createdTestimonial)

        runBlocking {
            val responseTestimonials = controller.readAll()

            assertThat(responseTestimonials).hasSize(1)
            assertThat(responseTestimonials).allMatch { it is TestimonialDto }
        }
    }
    // </editor-fold>

    // <editor-fold desc="Get specific testimonial">
    @Test
    fun `when requesting specific testimonial by ID, we return valid testimonialDto`() {
        val createdTestimonial = givenATestimonial()

        coEvery { testimonialDao.readById(any() as UUID) } returns createdTestimonial

        runBlocking {
            val responseTestimonial = controller.readById(UUID.randomUUID())

            assertThat(responseTestimonial.review).isEqualTo(createdTestimonial.review)
            assertNotNull(responseTestimonial.createdAt)
            assertNotNull(responseTestimonial.updatedAt)
        }
    }

    @Test
    fun `when requesting specific testimonial by ID where the ID does not exist, we throw exception`() {
        coEvery { testimonialDao.readById(any() as UUID) } throws ErrorNotFound

        assertThrows<ErrorNotFound> {
            runBlocking { controller.readById(UUID.randomUUID()) }
        }
    }
    // </editor-fold>

    // <editor-fold desc="Create new testimonial">
    @Test
    fun `when creating testimonial with incorrect information, we throw exception`() {
        val postTestimonial = givenAnInvalidInsertTestimonial()

        assertThrows<ErrorInvalidParameters> {
            runBlocking { controller.create(postTestimonial) }
        }
    }

    @Test
    fun `when creating testimonial with correct information, we return valid testimonialDto`() {
        val postTestimonial = givenAValidInsertTestimonial()
        val createdTestimonial = givenATestimonial()

        coEvery { companyDao.getListOfExistingCompanyIds(any()) } returns listOf(UUID.fromString("00000000-0000-0000-0000-000000000001"))
        coEvery { jobPositionDao.getListOfExistingJobPositionIds(any()) } returns listOf(UUID.fromString("00000000-0000-0000-0000-000000000001"))
        coEvery { testimonialDao.create(any(), any()) } returns UUID.randomUUID()
        coEvery { testimonialDao.readById(any()) } returns createdTestimonial

        runBlocking {
            val responseTestimonial = controller.create(postTestimonial)

            assertThat(responseTestimonial.review).isEqualTo(createdTestimonial.review)
        }
    }

    @Test
    fun `when creating testimonial with correct information but companyId does not exist, we throw exception`() {
        val postTestimonial = givenAValidInsertTestimonial()
        val createdTestimonial = givenATestimonial()

        coEvery { companyDao.getListOfExistingCompanyIds(any()) } returns listOf()
        coEvery { jobPositionDao.getListOfExistingJobPositionIds(any()) } returns listOf(UUID.fromString("00000000-0000-0000-0000-000000000001"))
        coEvery { testimonialDao.create(any(), any()) } returns UUID.randomUUID()
        coEvery { testimonialDao.readById(any()) } returns createdTestimonial

        assertThrows<ErrorUnknownCompanyIdsForCreateTestimonial> {
            runBlocking { controller.create(postTestimonial) }
        }
    }

    @Test
    fun `when creating testimonial with correct information but jobPositionId does not exist, we throw exception`() {
        val postTestimonial = givenAValidInsertTestimonial()
        val createdTestimonial = givenATestimonial()

        coEvery { companyDao.getListOfExistingCompanyIds(any()) } returns listOf(UUID.fromString("00000000-0000-0000-0000-000000000001"))
        coEvery { jobPositionDao.getListOfExistingJobPositionIds(any()) } returns listOf()
        coEvery { testimonialDao.create(any(), any()) } returns UUID.randomUUID()
        coEvery { testimonialDao.readById(any()) } returns createdTestimonial

        assertThrows<ErrorUnknownJobPositionIdsForCreateTestimonial> {
            runBlocking { controller.create(postTestimonial) }
        }
    }

    @Test
    fun `when creating testimonial and database returns error, we throw exception`() {
        val postTestimonial = givenAValidInsertTestimonial()

        coEvery { companyDao.getListOfExistingCompanyIds(any()) } returns listOf(UUID.fromString("00000000-0000-0000-0000-000000000001"))
        coEvery { jobPositionDao.getListOfExistingJobPositionIds(any()) } returns listOf(UUID.fromString("00000000-0000-0000-0000-000000000001"))
        coEvery { testimonialDao.create(any(), any()) } returns UUID.randomUUID()
        coEvery { testimonialDao.readById(any()) } returns null

        assertThrows<ErrorFailedCreate> {
            runBlocking { controller.create(postTestimonial) }
        }
    }
    // </editor-fold>

    // <editor-fold desc="Update specific testimonial">
    @Test
    fun `when updating specific testimonial, we return valid testimonialDto`() {
        val updateTestimonial = givenAValidUpdateTestimonial()
        val createdTestimonial = givenATestimonial()

        coEvery { companyDao.getListOfExistingCompanyIds(any()) } returns listOf(UUID.fromString("00000000-0000-0000-0000-000000000001"))
        coEvery { jobPositionDao.getListOfExistingJobPositionIds(any()) } returns listOf(UUID.fromString("00000000-0000-0000-0000-000000000001"))
        coEvery { testimonialDao.updateById(any(), any()) } returns true
        coEvery { testimonialDao.readById(any()) } returns createdTestimonial

        runBlocking {
            val responseTestimonial = controller.updateById(UUID.randomUUID(), updateTestimonial)

            // Assertion
            assertThat(responseTestimonial.review).isEqualTo(createdTestimonial.review)
        }
    }

    @Test
    fun `when updating specific testimonial but companyId does not exist, we throw exception`() {
        val updateTestimonial = givenAValidUpdateTestimonial()
        val createdTestimonial = givenATestimonial()

        coEvery { companyDao.getListOfExistingCompanyIds(any()) } returns listOf()
        coEvery { jobPositionDao.getListOfExistingJobPositionIds(any()) } returns listOf(UUID.fromString("00000000-0000-0000-0000-000000000001"))
        coEvery { testimonialDao.updateById(any(), any()) } returns true
        coEvery { testimonialDao.readById(any()) } returns createdTestimonial

        assertThrows<ErrorUnknownCompanyIdsForUpdateTestimonial> {
            runBlocking { controller.updateById(UUID.randomUUID(), updateTestimonial) }
        }
    }

    @Test
    fun `when updating specific testimonial but jobPositionId does not exist, we throw exception`() {
        val updateTestimonial = givenAValidUpdateTestimonial()
        val createdTestimonial = givenATestimonial()

        coEvery { companyDao.getListOfExistingCompanyIds(any()) } returns listOf(UUID.fromString("00000000-0000-0000-0000-000000000001"))
        coEvery { jobPositionDao.getListOfExistingJobPositionIds(any()) } returns listOf()
        coEvery { testimonialDao.updateById(any(), any()) } returns true
        coEvery { testimonialDao.readById(any()) } returns createdTestimonial

        assertThrows<ErrorUnknownJobPositionIdsForUpdateTestimonial> {
            runBlocking { controller.updateById(UUID.randomUUID(), updateTestimonial) }
        }
    }

    @Test
    fun `when updating specific testimonial which has invalid data, we throw exception`() {
        val updateTestimonial = givenAnInvalidUpdateTestimonial()

        assertThrows<ErrorInvalidParameters> {
            runBlocking { controller.updateById(UUID.randomUUID(), updateTestimonial) }
        }
    }

    @Test
    fun `when updating specific testimonial which does not exist, we throw exception`() {
        val updateTestimonial = givenAValidUpdateTestimonial()

        coEvery { companyDao.getListOfExistingCompanyIds(any()) } returns listOf(UUID.fromString("00000000-0000-0000-0000-000000000001"))
        coEvery { jobPositionDao.getListOfExistingJobPositionIds(any()) } returns listOf(UUID.fromString("00000000-0000-0000-0000-000000000001"))
        coEvery { testimonialDao.updateById(any(), any()) } returns true
        coEvery { testimonialDao.readById(any()) } returns null

        assertThrows<ErrorNotFound> {
            runBlocking { controller.updateById(UUID.randomUUID(), updateTestimonial) }
        }
    }
    // </editor-fold>

    // <editor-fold desc="Delete testimonial">
    @Test
    fun `when deleting specific testimonial, with an avatar image attached, we delete avatar image and return true`() {
        val mediaFile = MediaFileInstrumentation.givenAMediaFile()

        coEvery { mediaFileDao.readByOwnerId(any(), OwnerType.TESTIMONIAL) } returns mediaFile
        coEvery { testimonialDao.deleteById(any()) } returns true
        coEvery { mediaFileDao.delete(any()) } returns true
        coEvery { storageService.delete(any()) } returns true

        assertDoesNotThrow {
            runBlocking {
                controller.deleteById(UUID.randomUUID())
            }
        }
        verify(exactly = 1) { mediaFileDao.readByOwnerId(any(), any()) }
        verify(exactly = 1) { mediaFileDao.delete(any()) }
        coVerify(exactly = 1) { storageService.delete(any()) }
    }

    @Test
    fun `when deleting specific testimonial, without an avatar image attached, we delete testimonial and return true`() {
        coEvery { mediaFileDao.readByOwnerId(any(), OwnerType.TESTIMONIAL) } returns null
        coEvery { testimonialDao.deleteById(any()) } returns true

        assertDoesNotThrow {
            runBlocking {
                controller.deleteById(UUID.randomUUID())
            }
        }

        verify(exactly = 1) { mediaFileDao.readByOwnerId(any(), any()) }
        verify(inverse = true) { mediaFileDao.delete(any()) }
        coVerify(inverse = true) { storageService.delete(any()) }
    }

    @Test
    fun `when deleting specific testimonial, we return true`() {
        coEvery { mediaFileDao.readByOwnerId(any(), OwnerType.TESTIMONIAL) } returns null
        coEvery { testimonialDao.deleteById(any()) } returns true

        assertDoesNotThrow {
            runBlocking {
                controller.deleteById(UUID.randomUUID())
            }
        }
    }

    @Test
    fun `when deleting specific testimonial which does not exist, we throw exception`() {
        coEvery { mediaFileDao.readByOwnerId(any(), OwnerType.TESTIMONIAL) } returns null
        coEvery { testimonialDao.deleteById(any()) } returns false

        assertThrows<ErrorFailedDelete> {
            runBlocking { controller.deleteById(UUID.randomUUID()) }
        }
    }
    // </editor-fold>
}
