package com.cbconnectit.controllers.jobPositions

import com.cbconnectit.controllers.BaseControllerTest
import com.cbconnectit.controllers.jobPositions.JobPositionInstrumentation.givenAJobPosition
import com.cbconnectit.controllers.jobPositions.JobPositionInstrumentation.givenAValidInsertJobPosition
import com.cbconnectit.controllers.jobPositions.JobPositionInstrumentation.givenAValidUpdateJobPosition
import com.cbconnectit.controllers.jobPositions.JobPositionInstrumentation.givenAnInvalidInsertJobPosition
import com.cbconnectit.controllers.jobPositions.JobPositionInstrumentation.givenAnInvalidUpdateJobPosition
import com.cbconnectit.data.dto.requests.jobPosition.JobPositionDto
import com.cbconnectit.domain.interfaces.IJobPositionDao
import com.cbconnectit.modules.jobPositions.JobPositionController
import com.cbconnectit.modules.jobPositions.JobPositionControllerImpl
import com.cbconnectit.statuspages.ErrorDuplicateEntity
import com.cbconnectit.statuspages.ErrorFailedCreate
import com.cbconnectit.statuspages.ErrorFailedDelete
import com.cbconnectit.statuspages.ErrorFailedUpdate
import com.cbconnectit.statuspages.ErrorInvalidParameters
import com.cbconnectit.statuspages.ErrorNotFound
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
import java.util.*
import kotlin.test.assertNotNull

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class JobPositionControllerTest : BaseControllerTest() {

    private val jobPositionDao: IJobPositionDao = mockk()
    private val controller: JobPositionController by lazy { JobPositionControllerImpl(jobPositionDao) }

    @BeforeEach
    override fun before() {
        super.before()
        clearMocks(jobPositionDao)
    }

    // <editor-fold desc="Get all jobPositions">
    @Test
    fun `when requesting all jobPositions, we return valid list`() {
        val createdJobPosition = givenAJobPosition()

        coEvery { jobPositionDao.getJobPositions() } returns listOf(createdJobPosition)

        runBlocking {
            val responseJobPositions = controller.getJobPositions()

            assertThat(responseJobPositions).hasSize(1)
            assertThat(responseJobPositions).allMatch { it is JobPositionDto }
        }
    }
    // </editor-fold>

    // <editor-fold desc="Get specific jobPosition">
    @Test
    fun `when requesting specific jobPosition by ID, we return valid jobPositionDto`() {
        val createdJobPosition = givenAJobPosition()

        coEvery { jobPositionDao.getJobPositionById(any() as UUID) } returns createdJobPosition

        runBlocking {
            val responseJobPosition = controller.getJobPositionById(UUID.randomUUID())

            assertThat(responseJobPosition.name).isEqualTo(createdJobPosition.name)
            assertNotNull(responseJobPosition.createdAt)
            assertNotNull(responseJobPosition.updatedAt)
        }
    }

    @Test
    fun `when requesting specific jobPosition by ID where the ID does not exist, we throw exception`() {
        coEvery { jobPositionDao.getJobPositionById(any() as UUID) } throws ErrorNotFound

        assertThrows<ErrorNotFound> {
            runBlocking { controller.getJobPositionById(UUID.randomUUID()) }
        }
    }
    // </editor-fold>

    // <editor-fold desc="Create new jobPosition">
    @Test
    fun `when creating jobPosition with incorrect information, we throw exception`() {
        val postJobPosition = givenAnInvalidInsertJobPosition()

        assertThrows<ErrorInvalidParameters> {
            runBlocking { controller.postJobPosition(postJobPosition) }
        }
    }

    @Test
    fun `when creating jobPosition with correct information and jobPosition not taken, we return valid jobPositionDto`() {
        val postJobPosition = givenAValidInsertJobPosition()
        val createdJobPosition = givenAJobPosition()

        coEvery { jobPositionDao.jobPositionUnique(any()) } returns true
        coEvery { jobPositionDao.insertJobPosition(any()) } returns createdJobPosition

        runBlocking {
            val responseJobPosition = controller.postJobPosition(postJobPosition)

            assertThat(responseJobPosition.name).isEqualTo(createdJobPosition.name)
        }
    }

    @Test
    fun `when creating jobPosition and database returns error, we throw exception`() {
        val postJobPosition = givenAValidInsertJobPosition()

        coEvery { jobPositionDao.jobPositionUnique(any()) } returns true
        coEvery { jobPositionDao.insertJobPosition(any()) } returns null

        assertThrows<ErrorFailedCreate> {
            runBlocking { controller.postJobPosition(postJobPosition) }
        }
    }

    @Test
    fun `when creating jobPosition with name already taken, we throw exception`() {
        val postJobPosition = givenAValidInsertJobPosition()

        coEvery { jobPositionDao.jobPositionUnique(any()) } returns false

        assertThrows<ErrorDuplicateEntity> {
            runBlocking { controller.postJobPosition(postJobPosition) }
        }
    }
    // </editor-fold>

    // <editor-fold desc="Update specific jobPosition">
    @Test
    fun `when updating specific jobPosition, we return valid jobPositionDto`() {
        val updateJobPosition = givenAValidUpdateJobPosition()
        val createdJobPosition = givenAJobPosition()

        coEvery { jobPositionDao.updateJobPosition(any(), any()) } returns createdJobPosition
        coEvery { jobPositionDao.jobPositionUnique(any()) } returns true

        runBlocking {
            val responseJobPosition = controller.updateJobPositionById(UUID.randomUUID(), updateJobPosition)

            // Assertion
            assertThat(responseJobPosition.name).isEqualTo(createdJobPosition.name)
        }
    }

    @Test
    fun `when updating specific jobPosition where new data is not unique, we throw exception`() {
        val updateJobPosition = givenAValidUpdateJobPosition()

        coEvery { jobPositionDao.jobPositionUnique(any()) } returns false

        assertThrows<ErrorDuplicateEntity> {
            runBlocking { controller.updateJobPositionById(UUID.randomUUID(), updateJobPosition) }
        }
    }

    @Test
    fun `when updating specific jobPosition which has invalid data, we throw exception`() {
        val updateJobPosition = givenAnInvalidUpdateJobPosition()

        assertThrows<ErrorInvalidParameters> {
            runBlocking { controller.updateJobPositionById(UUID.randomUUID(), updateJobPosition) }
        }
    }

    @Test
    fun `when updating specific jobPosition which does not exist, we throw exception`() {
        val updateJobPosition = givenAValidUpdateJobPosition()

        coEvery { jobPositionDao.updateJobPosition(any(), any()) } throws ErrorFailedUpdate
        coEvery { jobPositionDao.jobPositionUnique(any()) } returns true

        assertThrows<ErrorFailedUpdate> {
            runBlocking { controller.updateJobPositionById(UUID.randomUUID(), updateJobPosition) }
        }
    }
    // </editor-fold>

    // <editor-fold desc="Delete jobPosition">
    @Test
    fun `when deleting specific jobPosition, we return valid jobPositionDto`() {
        coEvery { jobPositionDao.deleteJobPosition(any()) } returns true

        assertDoesNotThrow {
            runBlocking {
                controller.deleteJobPositionById(UUID.randomUUID())
            }
        }
    }

    @Test
    fun `when deleting specific jobPosition which does not exist, we throw exception`() {
        coEvery { jobPositionDao.deleteJobPosition(any()) } returns false

        assertThrows<ErrorFailedDelete> {
            runBlocking { controller.deleteJobPositionById(UUID.randomUUID()) }
        }
    }
    // </editor-fold>
}
