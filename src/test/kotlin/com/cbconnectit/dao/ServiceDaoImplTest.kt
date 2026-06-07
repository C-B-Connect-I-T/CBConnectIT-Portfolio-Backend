package com.cbconnectit.dao

import com.cbconnectit.data.database.dao.ServiceDaoImpl
import com.cbconnectit.data.database.tables.ServicesTable
import com.cbconnectit.data.database.tables.TagsTable
import com.cbconnectit.instrumentation.ServiceInstrumentation
import com.cbconnectit.instrumentation.ServiceInstrumentation.givenAValidInsertService
import com.cbconnectit.instrumentation.ServiceInstrumentation.givenAValidUpdateService
import com.cbconnectit.instrumentation.TagInstrumentation
import kotlinx.coroutines.delay
import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.insert
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.*
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class ServiceDaoImplTest : BaseDaoTest() {

    private val dao = ServiceDaoImpl()

    override suspend fun seedData() {
        // Seed tag first (foreign key dependency)
        TagInstrumentation.givenTagList().take(1).forEach { data ->
            TagsTable.insert {
                it[id] = UUID.fromString("00000000-0000-0000-0000-000000000001")
                it[name] = data.name
                it[slug] = data.slug
                it[createdAt] = data.createdAt
                it[updatedAt] = data.updatedAt
            }
        }

        // Seed services with parent-child relationships
        listOf(
            Pair(UUID.fromString("00000000-0000-0000-0000-000000000001"), null),
            Pair(UUID.fromString("00000000-0000-0000-0000-000000000002"), UUID.fromString("00000000-0000-0000-0000-000000000001")),
            Pair(UUID.fromString("00000000-0000-0000-0000-000000000003"), null),
            Pair(UUID.fromString("00000000-0000-0000-0000-000000000004"), UUID.fromString("00000000-0000-0000-0000-000000000002"))
        ).forEachIndexed { index, (serviceId, parentId) ->
            val service = ServiceInstrumentation.givenServiceList()[index]
            ServicesTable.insert {
                it[id] = serviceId
                it[title] = service.title
                it[imageUrl] = service.imageUrl ?: "https://www.google.be/image"
                it[description] = service.description
                it[tagId] = UUID.fromString("00000000-0000-0000-0000-000000000001")
                it[parentServiceId] = parentId
                it[createdAt] = service.createdAt
                it[updatedAt] = service.updatedAt
            }
        }
    }

    // <editor-fold desc="Get all Services">
    @Test
    fun `getServices but none exists, return empty list`() = runTest(shouldSeedData = false) {
        val list = dao.getServices()
        assertThat(list).isEmpty()
    }

    @Test
    fun `getServices return the list`() = runTest {
        val list = dao.getServices()
        assertThat(list).hasSize(2)
    }
    // </editor-fold>

    // <editor-fold desc="Get specific Service by id">
    @Test
    fun `getService where item exists, return correct Service`() = runTest {
        val service = dao.getServiceById(UUID.fromString("00000000-0000-0000-0000-000000000001"))

        assertThat(service).matches {
            it?.title == ServiceInstrumentation.givenServiceList()[0].title
        }
    }

    @Test
    fun `getService where item does not exists, return 'null'`() = runTest {
        val service = dao.getServiceById(UUID.randomUUID())

        assertNull(service)
    }
    // </editor-fold>

    // <editor-fold desc="Create new Service">
    @Test
    fun `insertService where information is correct, database is storing Service and returning correct content`() = runTest {
        val validService = givenAValidInsertService()
        val service = dao.insertService(validService)

        assertThat(service).matches {
            it?.title == validService.title &&
                    it.createdAt == it.updatedAt
        }
    }

    @Test
    fun `insertService where the same data exists, database will give error`() = runTest {
        val validService = givenAValidInsertService()
        dao.insertService(validService)

        assertThrows<ExposedSQLException> {
            dao.insertService(validService)
        }
    }
    // </editor-fold>

    // <editor-fold desc="Update Service">
    @Test
    fun `updateService where information is correct, database is storing information and returning the correct content`() = runTest {
        // adding a delay so there is a clear difference between `updatedAt` and `createdAt`
        delay(1000)

        val validUpdateService = givenAValidUpdateService()
        val service = dao.updateService(UUID.fromString("00000000-0000-0000-0000-000000000001"), validUpdateService)

        assertThat(service).matches {
            it?.title == validUpdateService.title &&
                    it.createdAt != it.updatedAt
        }
    }

    @Test
    fun `updateService where information is correct but Service with id does not exist, database does nothing and returns 'null'`() = runTest {
        val validService = givenAValidUpdateService()
        val service = dao.updateService(UUID.randomUUID(), validService)

        assertNull(service)
    }
    // </editor-fold>

    // <editor-fold desc="Delete Service">
    @Test
    fun `deleteService for id that exists, return true`() = runTest {
        val deleted = dao.deleteService(UUID.fromString("00000000-0000-0000-0000-000000000001"))
        assertTrue(deleted)
    }

    @Test
    fun `deleteService for id that does not exist, return false`() = runTest {
        val deleted = dao.deleteService(UUID.randomUUID())
        assertFalse(deleted)
    }
    // </editor-fold>

    // <editor-fold desc="List of Existing Service IDs">
    @Test
    fun `getListOfExistingServiceIds where ids do not exist, should return empty list`() = runTest {
        val list = dao.getListOfExistingServiceIds(listOf(UUID.fromString("10000000-0000-0000-0000-000000000000"), UUID.fromString("20000000-0000-0000-0000-000000000000")))
        assertThat(list).isEmpty()
    }

    @Test
    fun `getListOfExistingServiceIds where some ids exist, should return empty list`() = runTest {
        val list = dao.getListOfExistingServiceIds(listOf(UUID.fromString("00000000-0000-0000-0000-000000000001"), UUID.fromString("20000000-0000-0000-0000-000000000000")))
        assertThat(list).hasSize(1)
    }
    // </editor-fold>
}
