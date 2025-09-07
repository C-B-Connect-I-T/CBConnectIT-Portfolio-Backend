package com.cbconnectit.controllers.experiences

import com.cbconnectit.controllers.BaseControllerTest
import com.cbconnectit.controllers.experiences.ExperienceInstrumentation.givenAExperience
import com.cbconnectit.controllers.experiences.ExperienceInstrumentation.givenAValidInsertExperience
import com.cbconnectit.controllers.experiences.ExperienceInstrumentation.givenAValidUpdateExperience
import com.cbconnectit.controllers.experiences.ExperienceInstrumentation.givenAnInvalidInsertExperience
import com.cbconnectit.controllers.experiences.ExperienceInstrumentation.givenAnInvalidUpdateExperience
import com.cbconnectit.data.dto.requests.experience.ExperienceDto
import com.cbconnectit.domain.interfaces.ICompanyDao
import com.cbconnectit.domain.interfaces.IExperienceDao
import com.cbconnectit.domain.interfaces.IJobPositionDao
import com.cbconnectit.domain.interfaces.ITagDao
import com.cbconnectit.modules.experiences.ExperienceController
import com.cbconnectit.modules.experiences.ExperienceControllerImpl
import com.cbconnectit.statuspages.ErrorFailedCreate
import com.cbconnectit.statuspages.ErrorFailedDelete
import com.cbconnectit.statuspages.ErrorFailedUpdate
import com.cbconnectit.statuspages.ErrorInvalidParameters
import com.cbconnectit.statuspages.ErrorNotFound
import com.cbconnectit.statuspages.ErrorUnknownCompanyIdsForCreateExperience
import com.cbconnectit.statuspages.ErrorUnknownCompanyIdsForUpdateExperience
import com.cbconnectit.statuspages.ErrorUnknownJobPositionIdsForCreateExperience
import com.cbconnectit.statuspages.ErrorUnknownJobPositionIdsForUpdateExperience
import com.cbconnectit.statuspages.ErrorUnknownTagIdsForCreateExperience
import com.cbconnectit.statuspages.ErrorUnknownTagIdsForUpdateExperience
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
class ExperienceControllerTest : BaseControllerTest() {

    private val experienceDao: IExperienceDao = mockk()
    private val companyDao: ICompanyDao = mockk()
    private val jobPositionDao: IJobPositionDao = mockk()
    private val tagDao: ITagDao = mockk()
    private val controller: ExperienceController by lazy { ExperienceControllerImpl(experienceDao, companyDao, jobPositionDao, tagDao) }

    @BeforeEach
    override fun before() {
        super.before()
        clearMocks(experienceDao, companyDao, jobPositionDao, tagDao)
    }

    // <editor-fold desc="Get all experiences">
    @Test
    fun `when requesting all experiences, we return valid list`() {
        val createdExperience = givenAExperience()

        coEvery { experienceDao.getExperiences() } returns listOf(createdExperience)

        runBlocking {
            val responseExperiences = controller.getExperiences()

            assertThat(responseExperiences).hasSize(1)
            assertThat(responseExperiences).allMatch { it is ExperienceDto }
        }
    }
    // </editor-fold>

    // <editor-fold desc="Get specific experience">
    @Test
    fun `when requesting specific experience by ID, we return valid experienceDto`() {
        val createdExperience = givenAExperience()

        coEvery { experienceDao.getExperienceById(any() as UUID) } returns createdExperience

        runBlocking {
            val responseExperience = controller.getExperienceById(UUID.randomUUID())

            assertThat(responseExperience.shortDescription).isEqualTo(createdExperience.shortDescription)
            assertNotNull(responseExperience.createdAt)
            assertNotNull(responseExperience.updatedAt)
        }
    }

    @Test
    fun `when requesting specific experience by ID where the ID does not exist, we throw exception`() {
        coEvery { experienceDao.getExperienceById(any() as UUID) } throws ErrorNotFound

        assertThrows<ErrorNotFound> {
            runBlocking { controller.getExperienceById(UUID.randomUUID()) }
        }
    }
    // </editor-fold>

    // <editor-fold desc="Create new experience">
    @Test
    fun `when creating experience with incorrect information, we throw exception`() {
        val postExperience = givenAnInvalidInsertExperience()

        assertThrows<ErrorInvalidParameters> {
            runBlocking { controller.postExperience(postExperience) }
        }
    }

    @Test
    fun `when creating experience with correct information, we return valid experienceDto`() {
        val postExperience = givenAValidInsertExperience()
        val createdExperience = givenAExperience()

        coEvery { companyDao.getListOfExistingCompanyIds(any()) } returns listOf(UUID.fromString("00000000-0000-0000-0000-000000000001"))
        coEvery { jobPositionDao.getListOfExistingJobPositionIds(any()) } returns listOf(UUID.fromString("00000000-0000-0000-0000-000000000001"))
        coEvery { tagDao.getListOfExistingTagIds(any()) } returns listOf(UUID.fromString("00000000-0000-0000-0000-000000000001"))
        coEvery { experienceDao.insertExperience(any()) } returns createdExperience

        runBlocking {
            val responseExperience = controller.postExperience(postExperience)

            assertThat(responseExperience.shortDescription).isEqualTo(createdExperience.shortDescription)
        }
    }

    @Test
    fun `when creating experience with correct information but companyId does not exist, we throw exception`() {
        val postExperience = givenAValidInsertExperience()
        val createdExperience = givenAExperience()

        coEvery { companyDao.getListOfExistingCompanyIds(any()) } returns listOf()
        coEvery { jobPositionDao.getListOfExistingJobPositionIds(any()) } returns listOf(UUID.fromString("00000000-0000-0000-0000-000000000001"))
        coEvery { experienceDao.insertExperience(any()) } returns createdExperience

        assertThrows<ErrorUnknownCompanyIdsForCreateExperience> {
            runBlocking { controller.postExperience(postExperience) }
        }
    }

    @Test
    fun `when creating experience with correct information but tagId does not exist, we throw exception`() {
        val postExperience = givenAValidInsertExperience()
        val createdExperience = givenAExperience()

        coEvery { companyDao.getListOfExistingCompanyIds(any()) } returns listOf(UUID.fromString("00000000-0000-0000-0000-000000000001"))
        coEvery { jobPositionDao.getListOfExistingJobPositionIds(any()) } returns listOf(UUID.fromString("00000000-0000-0000-0000-000000000001"))
        coEvery { tagDao.getListOfExistingTagIds(any()) } returns emptyList()
        coEvery { experienceDao.insertExperience(any()) } returns createdExperience

        assertThrows<ErrorUnknownTagIdsForCreateExperience> {
            runBlocking { controller.postExperience(postExperience) }
        }
    }

    @Test
    fun `when creating experience with correct information but jobPositionId does not exist, we throw exception`() {
        val postExperience = givenAValidInsertExperience()
        val createdExperience = givenAExperience()

        coEvery { companyDao.getListOfExistingCompanyIds(any()) } returns listOf(UUID.fromString("00000000-0000-0000-0000-000000000001"))
        coEvery { jobPositionDao.getListOfExistingJobPositionIds(any()) } returns listOf()
        coEvery { experienceDao.insertExperience(any()) } returns createdExperience

        assertThrows<ErrorUnknownJobPositionIdsForCreateExperience> {
            runBlocking { controller.postExperience(postExperience) }
        }
    }

    @Test
    fun `when creating experience and database returns error, we throw exception`() {
        val postExperience = givenAValidInsertExperience()

        coEvery { companyDao.getListOfExistingCompanyIds(any()) } returns listOf(UUID.fromString("00000000-0000-0000-0000-000000000001"))
        coEvery { jobPositionDao.getListOfExistingJobPositionIds(any()) } returns listOf(UUID.fromString("00000000-0000-0000-0000-000000000001"))
        coEvery { tagDao.getListOfExistingTagIds(any()) } returns listOf(UUID.fromString("00000000-0000-0000-0000-000000000001"))
        coEvery { experienceDao.insertExperience(any()) } returns null

        assertThrows<ErrorFailedCreate> {
            runBlocking { controller.postExperience(postExperience) }
        }
    }
    // </editor-fold>

    // <editor-fold desc="Update specific experience">
    @Test
    fun `when updating specific experience, we return valid experienceDto`() {
        val updateExperience = givenAValidUpdateExperience()
        val createdExperience = givenAExperience()

        coEvery { companyDao.getListOfExistingCompanyIds(any()) } returns listOf(UUID.fromString("00000000-0000-0000-0000-000000000001"))
        coEvery { jobPositionDao.getListOfExistingJobPositionIds(any()) } returns listOf(UUID.fromString("00000000-0000-0000-0000-000000000001"))
        coEvery { tagDao.getListOfExistingTagIds(any()) } returns listOf(UUID.fromString("00000000-0000-0000-0000-000000000002"))
        coEvery { experienceDao.updateExperience(any(), any()) } returns createdExperience

        runBlocking {
            val responseExperience = controller.updateExperienceById(UUID.randomUUID(), updateExperience)

            // Assertion
            assertThat(responseExperience.shortDescription).isEqualTo(createdExperience.shortDescription)
        }
    }

    @Test
    fun `when updating specific experience but companyId does not exist, we throw exception`() {
        val updateExperience = givenAValidUpdateExperience()
        val createdExperience = givenAExperience()

        coEvery { companyDao.getListOfExistingCompanyIds(any()) } returns listOf()
        coEvery { jobPositionDao.getListOfExistingJobPositionIds(any()) } returns listOf(UUID.fromString("00000000-0000-0000-0000-000000000001"))
        coEvery { experienceDao.updateExperience(any(), any()) } returns createdExperience

        assertThrows<ErrorUnknownCompanyIdsForUpdateExperience> {
            runBlocking { controller.updateExperienceById(UUID.randomUUID(), updateExperience) }
        }
    }

    @Test
    fun `when updating specific experience but tagId does not exist, we throw exception`() {
        val updateExperience = givenAValidUpdateExperience()
        val createdExperience = givenAExperience()

        coEvery { companyDao.getListOfExistingCompanyIds(any()) } returns listOf(UUID.fromString("00000000-0000-0000-0000-000000000001"))
        coEvery { jobPositionDao.getListOfExistingJobPositionIds(any()) } returns listOf(UUID.fromString("00000000-0000-0000-0000-000000000001"))
        coEvery { tagDao.getListOfExistingTagIds(any()) } returns emptyList()
        coEvery { experienceDao.updateExperience(any(), any()) } returns createdExperience

        assertThrows<ErrorUnknownTagIdsForUpdateExperience> {
            runBlocking { controller.updateExperienceById(UUID.randomUUID(), updateExperience) }
        }
    }

    @Test
    fun `when updating specific experience but jobPositionId does not exist, we throw exception`() {
        val updateExperience = givenAValidUpdateExperience()
        val createdExperience = givenAExperience()

        coEvery { companyDao.getListOfExistingCompanyIds(any()) } returns listOf(UUID.fromString("00000000-0000-0000-0000-000000000001"))
        coEvery { jobPositionDao.getListOfExistingJobPositionIds(any()) } returns listOf()
        coEvery { experienceDao.updateExperience(any(), any()) } returns createdExperience

        assertThrows<ErrorUnknownJobPositionIdsForUpdateExperience> {
            runBlocking { controller.updateExperienceById(UUID.randomUUID(), updateExperience) }
        }
    }

    @Test
    fun `when updating specific experience which has invalid data, we throw exception`() {
        val updateExperience = givenAnInvalidUpdateExperience()

        assertThrows<ErrorInvalidParameters> {
            runBlocking { controller.updateExperienceById(UUID.randomUUID(), updateExperience) }
        }
    }

    @Test
    fun `when updating specific experience which does not exist, we throw exception`() {
        val updateExperience = givenAValidUpdateExperience()

        coEvery { companyDao.getListOfExistingCompanyIds(any()) } returns listOf(UUID.fromString("00000000-0000-0000-0000-000000000001"))
        coEvery { jobPositionDao.getListOfExistingJobPositionIds(any()) } returns listOf(UUID.fromString("00000000-0000-0000-0000-000000000001"))
        coEvery { tagDao.getListOfExistingTagIds(any()) } returns listOf(UUID.fromString("00000000-0000-0000-0000-000000000002"))
        coEvery { experienceDao.updateExperience(any(), any()) } throws ErrorFailedUpdate

        assertThrows<ErrorFailedUpdate> {
            runBlocking { controller.updateExperienceById(UUID.randomUUID(), updateExperience) }
        }
    }
    // </editor-fold>

    // <editor-fold desc="Delete experience">
    @Test
    fun `when deleting specific experience, we return valid experienceDto`() {
        coEvery { experienceDao.deleteExperience(any()) } returns true

        assertDoesNotThrow {
            runBlocking {
                controller.deleteExperienceById(UUID.randomUUID())
            }
        }
    }

    @Test
    fun `when deleting specific experience which does not exist, we throw exception`() {
        coEvery { experienceDao.deleteExperience(any()) } returns false

        assertThrows<ErrorFailedDelete> {
            runBlocking { controller.deleteExperienceById(UUID.randomUUID()) }
        }
    }
    // </editor-fold>
}
