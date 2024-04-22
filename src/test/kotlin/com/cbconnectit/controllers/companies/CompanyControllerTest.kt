package com.cbconnectit.controllers.companies

import com.cbconnectit.controllers.BaseControllerTest
import com.cbconnectit.controllers.companies.CompanyInstrumentation.givenACompany
import com.cbconnectit.controllers.companies.CompanyInstrumentation.givenAValidInsertCompany
import com.cbconnectit.controllers.companies.CompanyInstrumentation.givenAValidInsertCompanyWithLink
import com.cbconnectit.controllers.companies.CompanyInstrumentation.givenAValidUpdateCompany
import com.cbconnectit.controllers.companies.CompanyInstrumentation.givenAValidUpdateCompanyWithLink
import com.cbconnectit.controllers.companies.CompanyInstrumentation.givenAnInvalidInsertCompany
import com.cbconnectit.controllers.companies.CompanyInstrumentation.givenAnInvalidUpdateCompany
import com.cbconnectit.data.dto.requests.company.CompanyDto
import com.cbconnectit.domain.interfaces.ICompanyDao
import com.cbconnectit.domain.interfaces.ILinkDao
import com.cbconnectit.modules.companies.CompanyController
import com.cbconnectit.modules.companies.CompanyControllerImpl
import com.cbconnectit.statuspages.ErrorDuplicateEntity
import com.cbconnectit.statuspages.ErrorFailedCreate
import com.cbconnectit.statuspages.ErrorFailedDelete
import com.cbconnectit.statuspages.ErrorFailedUpdate
import com.cbconnectit.statuspages.ErrorInvalidParameters
import com.cbconnectit.statuspages.ErrorNotFound
import com.cbconnectit.statuspages.ErrorUnknownLinkIdsForCreateCompany
import com.cbconnectit.statuspages.ErrorUnknownLinkIdsForUpdateCompany
import io.mockk.clearMocks
import io.mockk.every
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
class CompanyControllerTest : BaseControllerTest() {

    private val companyDao: ICompanyDao = mockk()
    private val linkDao: ILinkDao = mockk()
    private val controller: CompanyController by lazy { CompanyControllerImpl() }

    init {
        startInjection(
            module {
                single { companyDao }
                single { linkDao }
            }
        )
    }

    @BeforeEach
    override fun before() {
        super.before()
        clearMocks(companyDao, linkDao)
    }

    // <editor-fold desc="Get all companies">
    @Test
    fun `when requesting all companies, we return valid list`() {
        val createdCompany = givenACompany()

        every { companyDao.getCompanies() } returns listOf(createdCompany)

        runBlocking {
            val responseCompanies = controller.getCompanies()

            assertThat(responseCompanies).hasSize(1)
            assertThat(responseCompanies).allMatch { it is CompanyDto }
        }
    }
    // </editor-fold>

    // <editor-fold desc="Get specific company">
    @Test
    fun `when requesting specific company by ID, we return valid companyDto`() {
        val createdCompany = givenACompany()

        every { companyDao.getCompanyById(any() as UUID) } returns createdCompany

        runBlocking {
            val responseCompany = controller.getCompanyById(UUID.randomUUID())

            assertThat(responseCompany.name).isEqualTo(createdCompany.name)
            assertNotNull(responseCompany.createdAt)
            assertNotNull(responseCompany.updatedAt)
        }
    }

    @Test
    fun `when requesting specific company by ID where the ID does not exist, we throw exception`() {
        every { companyDao.getCompanyById(any() as UUID) } throws ErrorNotFound

        assertThrows<ErrorNotFound> {
            runBlocking { controller.getCompanyById(UUID.randomUUID()) }
        }
    }
    // </editor-fold>

    // <editor-fold desc="Create new company">
    @Test
    fun `when creating company with incorrect information, we throw exception`() {
        val postCompany = givenAnInvalidInsertCompany()

        assertThrows<ErrorInvalidParameters> {
            runBlocking { controller.postCompany(postCompany) }
        }
    }

    @Test
    fun `when creating company with correct information and company not taken, we return valid companyDto`() {
        val postCompany = givenAValidInsertCompany()
        val createdCompany = givenACompany()

        every { companyDao.companyUnique(any()) } returns true
        every { companyDao.insertCompany(any()) } returns createdCompany
        every { linkDao.getListOfExistingLinkIds(any()) } returns listOf()

        runBlocking {
            val responseCompany = controller.postCompany(postCompany)

            assertThat(responseCompany.name).isEqualTo(createdCompany.name)
        }
    }

    @Test
    fun `when creating company with correct information and with a link, but linkId does not exist, we throw exception`() {
        val postCompany = givenAValidInsertCompanyWithLink()
        val createdCompany = givenACompany()

        every { companyDao.companyUnique(any()) } returns true
        every { companyDao.insertCompany(any()) } returns createdCompany
        every { linkDao.getListOfExistingLinkIds(any()) } returns listOf()

        assertThrows<ErrorUnknownLinkIdsForCreateCompany> {
            runBlocking { controller.postCompany(postCompany) }
        }
    }

    @Test
    fun `when creating company with correct information and with a link, which exists, we return valid companyDto`() {
        val postCompany = givenAValidInsertCompanyWithLink()
        val createdCompany = givenACompany()

        every { companyDao.companyUnique(any()) } returns true
        every { companyDao.insertCompany(any()) } returns createdCompany
        every { linkDao.getListOfExistingLinkIds(any()) } returns listOf(UUID.fromString("00000000-0000-0000-0000-000000000001"))

        runBlocking {
            val responseCompany = controller.postCompany(postCompany)

            assertThat(responseCompany.name).isEqualTo(createdCompany.name)
        }
    }

    @Test
    fun `when creating company and database returns error, we throw exception`() {
        val postCompany = givenAValidInsertCompany()

        every { companyDao.companyUnique(any()) } returns true
        every { companyDao.insertCompany(any()) } returns null
        every { linkDao.getListOfExistingLinkIds(any()) } returns listOf()

        assertThrows<ErrorFailedCreate> {
            runBlocking { controller.postCompany(postCompany) }
        }
    }

    @Test
    fun `when creating company with name already taken, we throw exception`() {
        val postCompany = givenAValidInsertCompany()

        every { companyDao.companyUnique(any()) } returns false

        assertThrows<ErrorDuplicateEntity> {
            runBlocking { controller.postCompany(postCompany) }
        }
    }
    // </editor-fold>

    // <editor-fold desc="Update specific company">
    @Test
    fun `when updating specific company, we return valid companyDto`() {
        val updateCompany = givenAValidUpdateCompany()
        val createdCompany = givenACompany()

        every { companyDao.updateCompany(any(), any()) } returns createdCompany
        every { companyDao.companyUnique(any()) } returns true
        every { linkDao.getListOfExistingLinkIds(any()) } returns listOf()

        runBlocking {
            val responseCompany = controller.updateCompanyById(UUID.randomUUID(), updateCompany)

            // Assertion
            assertThat(responseCompany.name).isEqualTo(createdCompany.name)
        }
    }

    @Test
    fun `when updating specific company with correct information and with a link, but linkId does not exist, we throw exception`() {
        val updateCompany = givenAValidUpdateCompanyWithLink()
        val createdCompany = givenACompany()

        every { companyDao.updateCompany(any(), any()) } returns createdCompany
        every { companyDao.companyUnique(any()) } returns true
        every { linkDao.getListOfExistingLinkIds(any()) } returns listOf()

        assertThrows<ErrorUnknownLinkIdsForUpdateCompany> {
            runBlocking { controller.updateCompanyById(UUID.randomUUID(), updateCompany) }
        }
    }

    @Test
    fun `when updatind specific company with correct information and with a link, which exists, we return valid companyDto`() {
        val updateCompany = givenAValidUpdateCompanyWithLink()
        val createdCompany = givenACompany()

        every { companyDao.updateCompany(any(), any()) } returns createdCompany
        every { companyDao.companyUnique(any()) } returns true
        every { linkDao.getListOfExistingLinkIds(any()) } returns listOf(UUID.fromString("00000000-0000-0000-0000-000000000001"))

        runBlocking {
            val responseCompany = controller.updateCompanyById(UUID.randomUUID(), updateCompany)

            // Assertion
            assertThat(responseCompany.name).isEqualTo(createdCompany.name)
        }
    }

    @Test
    fun `when updating specific company where new data is not unique, we throw exception`() {
        val updateCompany = givenAValidUpdateCompany()

        every { companyDao.companyUnique(any()) } returns false

        assertThrows<ErrorDuplicateEntity> {
            runBlocking { controller.updateCompanyById(UUID.randomUUID(), updateCompany) }
        }
    }

    @Test
    fun `when updating specific company which has invalid data, we throw exception`() {
        val updateCompany = givenAnInvalidUpdateCompany()

        assertThrows<ErrorInvalidParameters> {
            runBlocking { controller.updateCompanyById(UUID.randomUUID(), updateCompany) }
        }
    }

    @Test
    fun `when updating specific company which does not exist, we throw exception`() {
        val updateCompany = givenAValidUpdateCompany()

        every { companyDao.updateCompany(any(), any()) } throws ErrorFailedUpdate
        every { companyDao.companyUnique(any()) } returns true
        every { linkDao.getListOfExistingLinkIds(any()) } returns listOf()

        assertThrows<ErrorFailedUpdate> {
            runBlocking { controller.updateCompanyById(UUID.randomUUID(), updateCompany) }
        }
    }
    // </editor-fold>

    // <editor-fold desc="Delete company">
    @Test
    fun `when deleting specific company, we return valid companyDto`() {
        every { companyDao.deleteCompany(any()) } returns true

        assertDoesNotThrow {
            runBlocking {
                controller.deleteCompanyById(UUID.randomUUID())
            }
        }
    }

    @Test
    fun `when deleting specific company which does not exist, we throw exception`() {
        every { companyDao.deleteCompany(any()) } returns false

        assertThrows<ErrorFailedDelete> {
            runBlocking { controller.deleteCompanyById(UUID.randomUUID()) }
        }
    }
    // </editor-fold>
}
