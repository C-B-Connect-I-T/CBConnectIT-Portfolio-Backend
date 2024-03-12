package com.cbconnectit.data.database.dao.services

import com.cbconnectit.data.database.dao.BaseDaoTest
import com.cbconnectit.data.database.dao.ServiceDaoImpl
import com.cbconnectit.data.database.dao.services.ServiceInstrumentation.givenAValidInsertServiceBody
import com.cbconnectit.data.database.dao.services.ServiceInstrumentation.givenAValidUpdateServiceBody
import com.cbconnectit.data.database.tables.ServicesTable
import com.cbconnectit.data.database.tables.TagsTable
import com.cbconnectit.domain.models.service.Service
import com.cbconnectit.domain.models.tag.Tag
import kotlinx.coroutines.delay
import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.insert
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.*
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class ServiceDaoImplTest : BaseDaoTest() {

    private val dao = ServiceDaoImpl()

    // <editor-fold desc="Get all Services">
    @Test
    fun `getServices but none exists, return empty list`() {
        withTables(
            TagsTable,
            ServicesTable
        ) {
            val list = dao.getServices()
            assertThat(list).isEmpty()
        }
    }

    @Test
    fun `getServices return the list`() {
        baseTest {
            val list = dao.getServices()
            assertThat(list).hasSize(2)
        }
    }
    // </editor-fold>

    // <editor-fold desc="Get specific Service by id">
    @Test
    fun `getService where item exists, return correct Service`() {
        baseTest {
            val service = dao.getServiceById(UUID.fromString("00000000-0000-0000-0000-000000000001"))

            assertThat(service).matches {
                it?.title == "First parent service"
            }
        }
    }

    @Test
    fun `getService where item does not exists, return 'null'`() {
        baseTest {
            val service = dao.getServiceById(UUID.randomUUID())

            assertNull(service)
        }
    }
    // </editor-fold>

    // <editor-fold desc="Create new Service">
    @Test
    fun `insertService where information is correct, database is storing Service and returning correct content`() {
        baseTest {
            val validService = givenAValidInsertServiceBody()
            val service = dao.insertService(validService)

            assertThat(service).matches {
                it?.title == validService.title &&
                        it.createdAt == it.updatedAt
            }
        }
    }

    @Test
    fun `insertService where the same data exists, database will give error`() {
        baseTest {
            val validService = givenAValidInsertServiceBody()
            dao.insertService(validService)

            assertThrows<ExposedSQLException> {
                dao.insertService(validService)
            }
        }
    }
    // </editor-fold>

    // <editor-fold desc="Update Service">
    @Test
    fun `updateService where information is correct, database is storing information and returning the correct content`() {
        baseTest {
            // adding a delay so there is a clear difference between `updatedAt` and `createdAt`
            delay(1000)

            val validUpdateService = givenAValidUpdateServiceBody()
            val service = dao.updateService(UUID.fromString("00000000-0000-0000-0000-000000000001"), validUpdateService)

            assertThat(service).matches {
                it?.title == validUpdateService.title &&
                        it.createdAt != it.updatedAt
            }
        }
    }

    @Test
    fun `updateService where information is correct but Service with id does not exist, database does nothing and returns 'null'`() {
        baseTest {
            val validService = givenAValidUpdateServiceBody()
            val service = dao.updateService(UUID.randomUUID(), validService)

            assertNull(service)
        }
    }
    // </editor-fold>

    // <editor-fold desc="Delete Service">
    @Test
    fun `deleteService for id that exists, return true`() {
        baseTest {
            val deleted = dao.deleteService(UUID.fromString("00000000-0000-0000-0000-000000000001"))
            assertTrue(deleted)
        }
    }

    @Test
    fun `deleteService for id that does not exist, return false`() {
        baseTest {
            val deleted = dao.deleteService(UUID.randomUUID())
            assertFalse(deleted)
        }
    }
    // </editor-fold>

    // <editor-fold desc="List of Existing Service IDs">
    @Test
    fun `getListOfExistingServiceIds where ids do not exist, should return empty list`() {
        baseTest {
            val list = dao.getListOfExistingServiceIds(listOf(UUID.fromString("10000000-0000-0000-0000-000000000000"), UUID.fromString("20000000-0000-0000-0000-000000000000")))
            assertThat(list).isEmpty()
        }
    }

    @Test
    fun `getListOfExistingServiceIds where some ids exist, should return empty list`() {
        baseTest {
            val list = dao.getListOfExistingServiceIds(listOf(UUID.fromString("00000000-0000-0000-0000-000000000001"), UUID.fromString("20000000-0000-0000-0000-000000000000")))
            assertThat(list).hasSize(1)
        }
    }
    // </editor-fold>

    private fun baseTest(
        test: suspend Transaction.() -> Unit
    ) {
        withTables(
            TagsTable,
            ServicesTable
        ) {
            listOf(
                Tag(UUID.fromString("00000000-0000-0000-0000-000000000001"), name = "First tag", slug ="first-tag")
            ).forEach {  data ->
                TagsTable.insert {
                    it[id] = data.id
                    it[name] = data.name
                    it[slug] = data.slug
                }
            }

            listOf(
                Service(id = UUID.fromString("00000000-0000-0000-0000-000000000001"), title = "First parent service", imageUrl = "https://www.google.be/image", description = "Description", tag = Tag(UUID.fromString("00000000-0000-0000-0000-000000000001"))) to null,
                Service(id = UUID.fromString("00000000-0000-0000-0000-000000000002"), title = "Sub service of First parent service", imageUrl = "https://www.google.be/image", description = "Description", tag = Tag(UUID.fromString("00000000-0000-0000-0000-000000000001"))) to UUID.fromString("00000000-0000-0000-0000-000000000001"),
                Service(id = UUID.fromString("00000000-0000-0000-0000-000000000003"), title = "Second parent service", imageUrl = "https://www.google.be/image", description = "Description", tag = Tag(UUID.fromString("00000000-0000-0000-0000-000000000001"))) to null,
                Service(id = UUID.fromString("00000000-0000-0000-0000-000000000004"), title = "Sub service of Sub service of First parent service", imageUrl = "https://www.google.be/image", description = "Description", tag = Tag(UUID.fromString("00000000-0000-0000-0000-000000000001"))) to UUID.fromString("00000000-0000-0000-0000-000000000002")
            ).forEach { data ->
                ServicesTable.insert {
                    it[id] = data.first.id
                    it[title] = data.first.title
                    it[imageUrl] = data.first.imageUrl
                    it[description] = data.first.description
                    it[tagId] = data.first.tag?.id
                    it[parentServiceId] = data.second
                }
            }

            test()
        }
    }
}