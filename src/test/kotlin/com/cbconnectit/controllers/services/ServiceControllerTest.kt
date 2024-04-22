package com.cbconnectit.controllers.services

import com.cbconnectit.controllers.BaseControllerTest
import com.cbconnectit.controllers.services.ServiceInstrumentation.givenAService
import com.cbconnectit.controllers.services.ServiceInstrumentation.givenAValidInsertNewServiceWithParent
import com.cbconnectit.controllers.services.ServiceInstrumentation.givenAValidInsertService
import com.cbconnectit.controllers.services.ServiceInstrumentation.givenAValidUpdateService
import com.cbconnectit.controllers.services.ServiceInstrumentation.givenAValidUpdateServiceWithParent
import com.cbconnectit.controllers.services.ServiceInstrumentation.givenAnInvalidInsertService
import com.cbconnectit.controllers.services.ServiceInstrumentation.givenAnInvalidUpdateService
import com.cbconnectit.data.dto.requests.service.ServiceDto
import com.cbconnectit.domain.interfaces.IServiceDao
import com.cbconnectit.domain.interfaces.ITagDao
import com.cbconnectit.modules.services.ServiceController
import com.cbconnectit.modules.services.ServiceControllerImpl
import com.cbconnectit.statuspages.ErrorFailedCreate
import com.cbconnectit.statuspages.ErrorFailedDelete
import com.cbconnectit.statuspages.ErrorFailedUpdate
import com.cbconnectit.statuspages.ErrorInvalidParameters
import com.cbconnectit.statuspages.ErrorNotFound
import com.cbconnectit.statuspages.ErrorUnknownServiceIdsForCreate
import com.cbconnectit.statuspages.ErrorUnknownServiceIdsForUpdate
import com.cbconnectit.statuspages.ErrorUnknownTagIdsForCreate
import com.cbconnectit.statuspages.ErrorUnknownTagIdsForUpdate
import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.koin.dsl.module
import java.util.*
import kotlin.test.assertNotNull

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ServiceControllerTest : BaseControllerTest() {

    private val serviceDao: IServiceDao = mockk()
    private val tagDao: ITagDao = mockk()
    private val controller: ServiceController by lazy { ServiceControllerImpl() }

    init {
        startInjection(
            module {
                single { serviceDao }
                single { tagDao }
            }
        )
    }

    @BeforeEach
    override fun before() {
        super.before()
        clearMocks(serviceDao, tagDao)
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
            runBlocking { controller.postService(postService) }
        }
    }

    @Test
    fun `when creating service with correct information, we return valid serviceDto`() {
        val postService = givenAValidInsertService()
        val createdService = givenAService()

        coEvery { tagDao.getListOfExistingTagIds(any()) } returns listOf(UUID.fromString("00000000-0000-0000-0000-000000000001"))
        coEvery { serviceDao.insertService(any()) } returns createdService

        runBlocking {
            val responseService = controller.postService(postService)

            assertThat(responseService.title).isEqualTo(createdService.title)
        }
    }

    @Test
    fun `when creating service with correct information but tagId does not exist, we throw exception`() {
        val postService = givenAValidInsertService()
        val createdService = givenAService()

        coEvery { tagDao.getListOfExistingTagIds(any()) } returns listOf()
        coEvery { serviceDao.insertService(any()) } returns createdService

        assertThrows<ErrorUnknownTagIdsForCreate> {
            runBlocking { controller.postService(postService) }
        }
    }

    @Test
    fun `when creating specific service but parentServiceId does not exist, we throw exception`() {
        val insertService = givenAValidInsertNewServiceWithParent()
        val createdService = givenAService()

        coEvery { tagDao.getListOfExistingTagIds(any()) } returns listOf(UUID.fromString("00000000-0000-0000-0000-000000000001"))
        coEvery { serviceDao.getListOfExistingServiceIds(any()) } returns listOf()
        coEvery { serviceDao.insertService(any()) } returns createdService

        assertThrows<ErrorUnknownServiceIdsForCreate> {
            runBlocking { controller.postService(insertService) }
        }
    }

    @Test
    fun `when creating specific service and parentServiceId does exist, we return service with subServices filled in`() {
        val insertService = givenAValidInsertNewServiceWithParent()
        val createdService = givenAService(subService = givenAService(name = "Sub Service"))

        coEvery { tagDao.getListOfExistingTagIds(any()) } returns listOf(UUID.fromString("00000000-0000-0000-0000-000000000001"))
        coEvery { serviceDao.getListOfExistingServiceIds(any()) } returns listOf(UUID.fromString("00000000-0000-0000-0000-000000000001"))
        coEvery { serviceDao.insertService(any()) } returns createdService

        runBlocking {
            val responseService = controller.postService(insertService)

            assertThat(responseService.title).isEqualTo(createdService.title)
            assertThat(responseService.subServices).hasSize(1)
        }
    }

    @Test
    fun `when creating service and database returns error, we throw exception`() {
        val postService = givenAValidInsertService()

        coEvery { tagDao.getListOfExistingTagIds(any()) } returns listOf(UUID.fromString("00000000-0000-0000-0000-000000000001"))
        coEvery { serviceDao.insertService(any()) } returns null

        assertThrows<ErrorFailedCreate> {
            runBlocking { controller.postService(postService) }
        }
    }
    // </editor-fold>

    // <editor-fold desc="Update specific service">
    @Test
    fun `when updating specific service, we return valid serviceDto`() {
        val updateService = givenAValidUpdateService()
        val createdService = givenAService()

        coEvery { tagDao.getListOfExistingTagIds(any()) } returns listOf(UUID.fromString("00000000-0000-0000-0000-000000000001"))
        coEvery { serviceDao.updateService(any(), any()) } returns createdService

        runBlocking {
            val responseService = controller.updateServiceById(UUID.randomUUID(), updateService)

            // Assertion
            assertThat(responseService.title).isEqualTo(createdService.title)
        }
    }

    @Test
    fun `when updating specific service but tagId does not exist, we throw exception`() {
        val updateService = givenAValidUpdateService()
        val createdService = givenAService()

        coEvery { tagDao.getListOfExistingTagIds(any()) } returns listOf()
        coEvery { serviceDao.updateService(any(), any()) } returns createdService

        assertThrows<ErrorUnknownTagIdsForUpdate> {
            runBlocking { controller.updateServiceById(UUID.randomUUID(), updateService) }
        }
    }

    @Test
    fun `when updating specific service but parentServiceId does not exist, we throw exception`() {
        val updateService = givenAValidUpdateServiceWithParent()
        val createdService = givenAService()

        coEvery { tagDao.getListOfExistingTagIds(any()) } returns listOf(UUID.fromString("00000000-0000-0000-0000-000000000001"))
        coEvery { serviceDao.getListOfExistingServiceIds(any()) } returns listOf()
        coEvery { serviceDao.updateService(any(), any()) } returns createdService

        assertThrows<ErrorUnknownServiceIdsForUpdate> {
            runBlocking { controller.updateServiceById(UUID.randomUUID(), updateService) }
        }
    }

    @Test
    fun `when updating specific service and parentServiceId does exist, we return service with subServices filled in`() {
        val updateService = givenAValidUpdateServiceWithParent()
        val createdService = givenAService(subService = givenAService(name = "Sub Service"))

        coEvery { tagDao.getListOfExistingTagIds(any()) } returns listOf(UUID.fromString("00000000-0000-0000-0000-000000000001"))
        coEvery { serviceDao.getListOfExistingServiceIds(any()) } returns listOf(UUID.fromString("00000000-0000-0000-0000-000000000001"))
        coEvery { serviceDao.updateService(any(), any()) } returns createdService

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

        coEvery { tagDao.getListOfExistingTagIds(any()) } returns listOf(UUID.fromString("00000000-0000-0000-0000-000000000001"))
        coEvery { serviceDao.updateService(any(), any()) } throws ErrorFailedUpdate

        assertThrows<ErrorFailedUpdate> {
            runBlocking { controller.updateServiceById(UUID.randomUUID(), updateService) }
        }
    }
    // </editor-fold>

    // <editor-fold desc="Delete service">
    @Test
    fun `when deleting specific service, we return valid serviceDto`() {
        coEvery { serviceDao.deleteService(any()) } returns true

        assertDoesNotThrow {
            runBlocking {
                controller.deleteServiceById(UUID.randomUUID())
            }
        }
    }

    @Test
    fun `when deleting specific service which does not exist, we throw exception`() {
        coEvery { serviceDao.deleteService(any()) } returns false

        assertThrows<ErrorFailedDelete> {
            runBlocking { controller.deleteServiceById(UUID.randomUUID()) }
        }
    }
    // </editor-fold>
}
