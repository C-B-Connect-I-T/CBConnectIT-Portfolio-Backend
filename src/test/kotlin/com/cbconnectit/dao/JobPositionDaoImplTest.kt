package com.cbconnectit.dao

import com.cbconnectit.data.database.dao.JobPositionDaoImpl
import com.cbconnectit.data.database.tables.JobPositionsTable
import com.cbconnectit.instrumentation.JobPositionInstrumentation
import com.cbconnectit.instrumentation.JobPositionInstrumentation.givenAValidInsertJobPosition
import com.cbconnectit.instrumentation.JobPositionInstrumentation.givenAValidUpdateJobPosition
import kotlinx.coroutines.delay
import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.exposed.sql.insert
import org.junit.jupiter.api.Test
import java.util.*
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class JobPositionDaoImplTest : BaseDaoTest() {

    private val dao = JobPositionDaoImpl()

    override suspend fun seedData() {
        JobPositionInstrumentation.givenJobPositionList().forEach { data ->
            JobPositionsTable.insert {
                it[id] = data.id
                it[name] = data.name
                it[createdAt] = data.createdAt
                it[updatedAt] = data.updatedAt
            }
        }
    }

    // <editor-fold desc="Get all jobPositions">
    @Test
    fun `getJobPositions but none exists, return empty list`() = runTest(shouldSeedData = false) {
        val list = dao.getJobPositions()
        assertThat(list).isEmpty()
    }

    @Test
    fun `getJobPositions return the list`() = runTest {
        val list = dao.getJobPositions()
        assertThat(list).hasSize(4)
    }
    // </editor-fold>

    // <editor-fold desc="Get specific jobPosition by id">
    @Test
    fun `getJobPosition where item exists, return correct jobPosition`() = runTest {
        val jobPosition = dao.getJobPositionById(JobPositionInstrumentation.givenJobPositionList()[0].id)

        assertThat(jobPosition).matches {
            it?.name == JobPositionInstrumentation.givenJobPositionList()[0].name
        }
    }

    @Test
    fun `getJobPosition where item does not exists, return 'null'`() = runTest {
        val jobPosition = dao.getJobPositionById(UUID.randomUUID())

        assertNull(jobPosition)
    }
    // </editor-fold>

    // <editor-fold desc="Create new jobPosition">
    @Test
    fun `insertJobPosition where information is correct, database is storing jobPosition and returning correct content`() = runTest(shouldSeedData = false) {
        val validJobPosition = givenAValidInsertJobPosition()
        val jobPosition = dao.insertJobPosition(validJobPosition)

        assertThat(jobPosition).matches {
            it?.name == validJobPosition.name &&
                    it.createdAt == it.updatedAt
        }
    }
    // </editor-fold>

    // <editor-fold desc="Update jobPosition">
    @Test
    fun `updateJobPosition where information is correct, database is storing information and returning the correct content`() = runTest {
        // adding a delay so there is a clear difference between `updatedAt` and `createdAt`
        delay(1000)

        val validUpdateJobPosition = givenAValidUpdateJobPosition()
        val jobPosition = dao.updateJobPosition(JobPositionInstrumentation.givenJobPositionList()[0].id, validUpdateJobPosition)

        assertThat(jobPosition).matches {
            it?.name == validUpdateJobPosition.name &&
                    it.createdAt != it.updatedAt
        }
    }

    @Test
    fun `updateJobPosition where information is correct but jobPosition with id does not exist, database does nothing and returns 'null'`() = runTest {
        val validJobPosition = givenAValidUpdateJobPosition()
        val jobPosition = dao.updateJobPosition(UUID.randomUUID(), validJobPosition)

        assertNull(jobPosition)
    }
    // </editor-fold>

    // <editor-fold desc="Delete jobPosition">
    @Test
    fun `deleteJobPosition for id that exists, return true`() = runTest {
        val deleted = dao.deleteJobPosition(JobPositionInstrumentation.givenJobPositionList()[0].id)
        assertTrue(deleted)
    }

    @Test
    fun `deleteJobPosition for id that does not exist, return false`() = runTest {
        val deleted = dao.deleteJobPosition(UUID.randomUUID())
        assertFalse(deleted)
    }
    // </editor-fold>

    // <editor-fold desc="Check if position is unique">
    @Test
    fun `jobPositionUnique() for id that exists, return false`() = runTest {
        val unique = dao.jobPositionUnique(JobPositionInstrumentation.givenJobPositionList()[0].name)
        assertFalse(unique)
    }

    @Test
    fun `jobPositionUnique() for id that does not exist, return true`() = runTest {
        val unique = dao.jobPositionUnique("iOS Developer")
        assertTrue(unique)
    }

    // </editor-fold>

    // <editor-fold desc="List of Existing JobPosition IDs">
    @Test
    fun `getListOfExistingJobPositionIds where ids do not exist, should return empty list`() = runTest(shouldSeedData = false) {
        val list = dao.getListOfExistingJobPositionIds(listOf(UUID.fromString("10000000-0000-0000-0000-000000000000"), UUID.fromString("20000000-0000-0000-0000-000000000000")))
        assertThat(list).isEmpty()
    }

    @Test
    fun `getListOfExistingJobPositionIds where some ids exist, should return list of existing items`() = runTest {
        val id = dao.insertJobPosition(givenAValidInsertJobPosition())?.id
        val list = dao.getListOfExistingJobPositionIds(listOf(id!!, UUID.fromString("20000000-0000-0000-0000-000000000000")))
        assertThat(list).hasSize(1)
    }
    // </editor-fold>
}
