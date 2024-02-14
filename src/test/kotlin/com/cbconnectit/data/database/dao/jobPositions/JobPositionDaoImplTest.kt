package com.cbconnectit.data.database.dao.jobPositions

import com.cbconnectit.data.database.dao.BaseDaoTest
import com.cbconnectit.data.database.dao.JobPositionDaoImpl
import com.cbconnectit.data.database.dao.jobPositions.JobPositionInstrumentation.givenAValidInsertJobPositionBody
import com.cbconnectit.data.database.dao.jobPositions.JobPositionInstrumentation.givenAValidSecondInsertJobPositionBody
import com.cbconnectit.data.database.dao.jobPositions.JobPositionInstrumentation.givenAValidUpdateJobPositionBody
import com.cbconnectit.data.database.tables.JobPositionsTable
import kotlinx.coroutines.delay
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.*
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class JobPositionDaoImplTest : BaseDaoTest() {

    private val dao = JobPositionDaoImpl()

    // <editor-fold desc="Get all jobPositions">
    @Test
    fun `getJobPositions but none exists, return empty list`() {
        withTables(JobPositionsTable) {
            val list = dao.getJobPositions()
            assertThat(list).isEmpty()
        }
    }

    @Test
    fun `getJobPositions return the list`() {
        withTables(JobPositionsTable) {
            dao.insertJobPosition(givenAValidInsertJobPositionBody())
            dao.insertJobPosition(givenAValidSecondInsertJobPositionBody())
            val list = dao.getJobPositions()
            assertThat(list).hasSize(2)
        }
    }
    // </editor-fold>

    // <editor-fold desc="Get specific jobPosition by id">
    @Test
    fun `getJobPosition where item exists, return correct jobPosition`() {
        withTables(JobPositionsTable) {
            val validJobPosition = givenAValidInsertJobPositionBody()
            val jobPositionId = dao.insertJobPosition(validJobPosition)?.id
            val jobPosition = dao.getJobPositionById(jobPositionId!!)

            assertThat(jobPosition).matches {
                it?.name == validJobPosition.name
            }
        }
    }

    @Test
    fun `getJobPosition where item does not exists, return 'null'`() {
        withTables(JobPositionsTable) {
            val jobPosition = dao.getJobPositionById(UUID.randomUUID())

            assertNull(jobPosition)
        }
    }
    // </editor-fold>

    // <editor-fold desc="Create new jobPosition">
    @Test
    fun `insertJobPosition where information is correct, database is storing jobPosition and returning correct content`() {
        withTables(JobPositionsTable) {
            val validJobPosition = givenAValidInsertJobPositionBody()
            val jobPosition = dao.insertJobPosition(validJobPosition)

            assertThat(jobPosition).matches {
                it?.name == validJobPosition.name &&
                        it.createdAt == it.updatedAt
            }
        }
    }
    // </editor-fold>

    // <editor-fold desc="Update jobPosition">
    @Test
    fun `updateJobPosition where information is correct, database is storing information and returning the correct content`() {
        withTables(JobPositionsTable) {
            val validJobPosition = givenAValidInsertJobPositionBody()
            val jobPositionId = dao.insertJobPosition(validJobPosition)?.id

            // adding a delay so there is a clear difference between `updatedAt` and `createdAt`
            delay(1000)

            val validUpdateJobPosition = givenAValidUpdateJobPositionBody()
            val jobPosition = dao.updateJobPosition(jobPositionId!!, validUpdateJobPosition)

            assertThat(jobPosition).matches {
                it?.name != validJobPosition.name &&
                        it?.name == validUpdateJobPosition.name &&
                        it.createdAt != it.updatedAt
            }
        }
    }

    @Test
    fun `updateJobPosition where information is correct but jobPosition with id does not exist, database does nothing and returns 'null'`() {
        withTables(JobPositionsTable) {
            val validJobPosition = givenAValidUpdateJobPositionBody()
            val jobPosition = dao.updateJobPosition(UUID.randomUUID(), validJobPosition)

            assertNull(jobPosition)
        }
    }
    // </editor-fold>

    // <editor-fold desc="Delete jobPosition">
    @Test
    fun `deleteJobPosition for id that exists, return true`() {
        withTables(JobPositionsTable) {
            val id = dao.insertJobPosition(givenAValidInsertJobPositionBody())?.id
            val deleted = dao.deleteJobPosition(id!!)
            assertTrue(deleted)
        }
    }

    @Test
    fun `deleteJobPosition for id that does not exist, return false`() {
        withTables(JobPositionsTable) {
            val deleted = dao.deleteJobPosition(UUID.randomUUID())
            assertFalse(deleted)
        }
    }
    // </editor-fold>

    // <editor-fold desc="Check if position is unique">
    @Test
    fun `obPositionUnique() for id that exists, return false`() {
        withTables(JobPositionsTable) {
            dao.insertJobPosition(givenAValidInsertJobPositionBody())
            val unique = dao.jobPositionUnique(givenAValidInsertJobPositionBody().name)
            assertFalse(unique)
        }
    }

    @Test
    fun `obPositionUnique() for id that does not exist, return true`() {
        withTables(JobPositionsTable) {
            dao.insertJobPosition(givenAValidInsertJobPositionBody())
            val unique = dao.jobPositionUnique(givenAValidSecondInsertJobPositionBody().name)
            assertTrue(unique)
        }
    }

    // </editor-fold>

    // <editor-fold desc="List of Existing JobPosition IDs">
    @Test
    fun `getListOfExistingJobPositionIds where ids do not exist, should return empty list`() {
        withTables(JobPositionsTable) {
            val list = dao.getListOfExistingJobPositionIds(listOf(UUID.fromString("10000000-0000-0000-0000-000000000000"), UUID.fromString("20000000-0000-0000-0000-000000000000")))
            assertThat(list).isEmpty()
        }
    }

    @Test
    fun `getListOfExistingJobPositionIds where some ids exist, should return list of existing items`() {
        withTables(JobPositionsTable) {
            val id = dao.insertJobPosition(givenAValidInsertJobPositionBody())?.id
            val list = dao.getListOfExistingJobPositionIds(listOf(id!!, UUID.fromString("20000000-0000-0000-0000-000000000000")))
            assertThat(list).hasSize(1)
        }
    }
    // </editor-fold>
}