package com.cbconnectit.routing.companies

import com.cbconnectit.data.dto.requests.company.CompanyDto
import com.cbconnectit.modules.companies.CompanyController
import com.cbconnectit.modules.companies.companyRouting
import com.cbconnectit.routing.AuthenticationInstrumentation
import com.cbconnectit.routing.BaseRoutingTest
import com.cbconnectit.routing.companies.CompanyInstrumentation.givenACompany
import com.cbconnectit.routing.companies.CompanyInstrumentation.givenAValidInsertCompany
import com.cbconnectit.routing.companies.CompanyInstrumentation.givenAValidUpdateCompanyBody
import com.cbconnectit.routing.companies.CompanyInstrumentation.givenCompanyList
import com.cbconnectit.statuspages.ErrorDuplicateEntity
import com.cbconnectit.statuspages.ErrorFailedDelete
import com.cbconnectit.statuspages.ErrorNotFound
import com.cbconnectit.statuspages.ErrorResponse
import com.cbconnectit.statuspages.toErrorResponse
import io.ktor.http.*
import io.ktor.server.routing.*
import io.mockk.coEvery
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.koin.dsl.module

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CompanyRoutingTest : BaseRoutingTest() {

    private val companyController: CompanyController = mockk()

    @BeforeAll
    fun setup() {
        koinModules = module {
            single { companyController }
        }
        moduleList = {
            routing {
                companyRouting(companyController)
            }
        }
    }

    @BeforeEach
    fun clearMocks() {
        io.mockk.clearMocks(companyController)
    }

    // <editor-fold desc="Get all companies">
    @Test
    fun `when fetching all companies, we return a list`() = withBaseTestApplication(
        AuthenticationInstrumentation()
    ) {
        coEvery { companyController.getCompanies() } returns givenCompanyList()

        val response = doCall(HttpMethod.Get, "/companies")

        assertThat(response.status).isEqualTo(HttpStatusCode.OK)
        assertThat(response.parseBody<List<*>>()).hasSize(4)
    }
    // </editor-fold>

    // <editor-fold desc="Get specific company">
    @Test
    fun `when fetching a specific company that exists by id, we return that company`() = withBaseTestApplication(
        AuthenticationInstrumentation()
    ) {
        val companyResponse = givenACompany()
        coEvery { companyController.getCompanyById(any()) } returns companyResponse

        val response = doCall(HttpMethod.Get, "/companies/a63a20c4-14dd-4e11-9e87-5ab361a51f65")

        assertThat(response.status).isEqualTo(HttpStatusCode.OK)
        assertThat(response.parseBody<CompanyDto>()).isEqualTo(companyResponse)
    }

    @Test
    fun `when fetching a specific company by id that does not exists, we return error`() = withBaseTestApplication(
        AuthenticationInstrumentation()
    ) {
        val exception = ErrorNotFound
        coEvery { companyController.getCompanyById(any()) } throws exception

        val response = doCall(HttpMethod.Get, "/companies/a63a20c4-14dd-4e11-9e87-5ab361a51f65")

        assertThat(response.status).isEqualTo(exception.statusCode)
        assertThat(response.parseBody<ErrorResponse>()).isEqualTo(exception.toErrorResponse())
    }
    // </editor-fold>

    // <editor-fold desc="Create new company">
    @Test
    fun `when creating company with successful insertion, we return response company body`() = withBaseTestApplication(
        AuthenticationInstrumentation()
    ) {
        val companyResponse = givenACompany()
        coEvery { companyController.postCompany(any()) } returns companyResponse

        val body = toJsonBody(givenAValidInsertCompany())
        val response = doCall(HttpMethod.Post, "/companies", body)

        assertThat(response.status).isEqualTo(HttpStatusCode.Created)
        assertThat(response.parseBody<CompanyDto>()).isEqualTo(companyResponse)
    }

    @Test
    fun `when creating company already created, we return 409 error`() = withBaseTestApplication(
        AuthenticationInstrumentation()
    ) {
        val exception = ErrorDuplicateEntity
        coEvery { companyController.postCompany(any()) } throws exception

        val body = toJsonBody(givenAValidInsertCompany())
        val response = doCall(HttpMethod.Post, "/companies", body)

        assertThat(response.status).isEqualTo(exception.statusCode)
        assertThat(response.parseBody<ErrorResponse>()).isEqualTo(exception.toErrorResponse())
    }
    // </editor-fold>

    // <editor-fold desc="Update company">
    @Test
    fun `when updating company with successful insertion, we return response company body`() = withBaseTestApplication(
        AuthenticationInstrumentation()
    ) {
        val companyResponse = givenACompany()
        coEvery { companyController.updateCompanyById(any(), any()) } returns companyResponse

        val body = toJsonBody(givenAValidUpdateCompanyBody())
        val response = doCall(HttpMethod.Put, "/companies/a63a20c4-14dd-4e11-9e87-5ab361a51f65", body)

        assertThat(response.status).isEqualTo(HttpStatusCode.OK)
        assertThat(response.parseBody<CompanyDto>()).isEqualTo(companyResponse)
    }

    @Test
    fun `when updating company with wrong companyId, we return error`() = withBaseTestApplication(
        AuthenticationInstrumentation()
    ) {
        val exception = ErrorNotFound
        coEvery { companyController.updateCompanyById(any(), any()) } throws exception

        val body = toJsonBody(givenAValidUpdateCompanyBody())
        val response = doCall(HttpMethod.Put, "/companies/a63a20c4-14dd-4e11-9e87-5ab361a51f65", body)

        assertThat(response.status).isEqualTo(exception.statusCode)
        assertThat(response.parseBody<ErrorResponse>()).isEqualTo(exception.toErrorResponse())
    }
    // </editor-fold>

    // <editor-fold desc="Delete company">
    @Test
    fun `when deleting company successful, we return Ok response`() = withBaseTestApplication(
        AuthenticationInstrumentation()
    ) {
        coEvery { companyController.deleteCompanyById(any()) } returns Unit

        val response = doCall(HttpMethod.Delete, "/companies/a63a20c4-14dd-4e11-9e87-5ab361a51f65")

        assertThat(response.status).isEqualTo(HttpStatusCode.OK)
    }

    @Test
    fun `when deleting company with wrong companyId, we return error`() = withBaseTestApplication(
        AuthenticationInstrumentation()
    ) {
        val exception = ErrorFailedDelete
        coEvery { companyController.deleteCompanyById(any()) } throws exception

        val response = doCall(HttpMethod.Delete, "/companies/a63a20c4-14dd-4e11-9e87-5ab361a51f65")

        assertThat(response.status).isEqualTo(exception.statusCode)
        assertThat(response.parseBody<ErrorResponse>()).isEqualTo(exception.toErrorResponse())
    }
    // </editor-fold>
}
