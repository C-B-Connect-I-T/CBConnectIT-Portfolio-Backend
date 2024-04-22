package com.cbconnectit.data.database.dao.companies

import com.cbconnectit.data.database.dao.BaseDaoTest
import com.cbconnectit.data.database.dao.CompanyDaoImpl
import com.cbconnectit.data.database.dao.companies.CompanyInstrumentation.givenAValidInsertCompanyBody
import com.cbconnectit.data.database.dao.companies.CompanyInstrumentation.givenAValidUpdateCompanyBody
import com.cbconnectit.data.database.dao.companies.CompanyInstrumentation.givenAValidUpdateCompanyBodyWithLink
import com.cbconnectit.data.database.dao.companies.CompanyInstrumentation.givenAnUnknownCompany
import com.cbconnectit.data.database.dao.companies.CompanyInstrumentation.givenAnUnknownCompanyWithLink
import com.cbconnectit.data.database.tables.CompaniesLinksPivotTable
import com.cbconnectit.data.database.tables.CompaniesTable
import com.cbconnectit.data.database.tables.LinksTable
import com.cbconnectit.domain.models.company.Company
import com.cbconnectit.domain.models.link.Link
import com.cbconnectit.domain.models.link.LinkType
import kotlinx.coroutines.delay
import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.insert
import org.junit.jupiter.api.Test
import java.util.*
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class CompanyDaoImplTest : BaseDaoTest() {

    private val dao = CompanyDaoImpl()

    // <editor-fold desc="Get all companys">
    @Test
    fun `getCompanies but none exists, return empty list`() {
        withTables(
            CompaniesTable,
            CompaniesLinksPivotTable,
            LinksTable
        ) {
            val list = dao.getCompanies()
            assertThat(list).isEmpty()
        }
    }

    @Test
    fun `getCompanies return the list`() {
        baseTest {
            val list = dao.getCompanies()
            assertThat(list).hasSize(3)
        }
    }
    // </editor-fold>

    // <editor-fold desc="Get specific company by id">
    @Test
    fun `getCompany where item exists, return correct company`() {
        baseTest {
            val validCompany = givenAValidInsertCompanyBody()
            val company = dao.getCompanyById(UUID.fromString("00000000-0000-0000-0000-000000000001"))

            assertThat(company).matches {
                it?.name == validCompany.name
            }
        }
    }

    @Test
    fun `getCompany where item does not exists, return 'null'`() {
        baseTest {
            val company = dao.getCompanyById(UUID.randomUUID())

            assertNull(company)
        }
    }
    // </editor-fold>

    // <editor-fold desc="Create new company">
    @Test
    fun `insertCompany where information is correct, database is storing company and returning correct content`() {
        baseTest {
            val validCompany = givenAnUnknownCompany()
            val company = dao.insertCompany(validCompany)

            assertThat(company).matches {
                it?.name == validCompany.name &&
                        it.createdAt == it.updatedAt
            }
        }
    }

    @Test
    fun `insertCompany where information is correct with a link, database is storing company and returning correct content`() {
        baseTest {
            val validCompany = givenAnUnknownCompanyWithLink()
            val company = dao.insertCompany(validCompany)

            assertThat(company).matches {
                it?.name == validCompany.name &&
                        it.createdAt == it.updatedAt &&
                        it.links.isNotEmpty()
            }
        }
    }
    // </editor-fold>

    // <editor-fold desc="Update company">
    @Test
    fun `updateCompany where information is correct, database is storing information and returning the correct content`() {
        baseTest {
            // adding a delay so there is a clear difference between `updatedAt` and `createdAt`
            delay(1000)

            val validUpdateCompany = givenAValidUpdateCompanyBody()
            val company = dao.updateCompany(UUID.fromString("00000000-0000-0000-0000-000000000001"), validUpdateCompany)

            assertThat(company).matches {
                it?.name == validUpdateCompany.name &&
                        it.createdAt != it.updatedAt
            }
        }
    }

    @Test
    fun `updateCompany where information is correct and a link is provided, database is storing information and returning the correct content`() {
        baseTest {
            // adding a delay so there is a clear difference between `updatedAt` and `createdAt`
            delay(1000)

            val validUpdateCompany = givenAValidUpdateCompanyBodyWithLink()
            val company = dao.updateCompany(UUID.fromString("00000000-0000-0000-0000-000000000001"), validUpdateCompany)

            assertThat(company).matches {
                it?.name == validUpdateCompany.name &&
                        it.createdAt != it.updatedAt &&
                        it.links.isNotEmpty() &&
                        it.links.size == 1
            }
        }
    }

    @Test
    fun `updateCompany where information is correct but company with id does not exist, database does nothing and returns 'null'`() {
        baseTest {
            val validCompany = givenAValidUpdateCompanyBody()
            val company = dao.updateCompany(UUID.randomUUID(), validCompany)

            assertNull(company)
        }
    }
    // </editor-fold>

    // <editor-fold desc="Delete company">
    @Test
    fun `deleteCompany for id that exists, return true`() {
        baseTest {
            val deleted = dao.deleteCompany(UUID.fromString("00000000-0000-0000-0000-000000000001"))
            assertTrue(deleted)
        }
    }

    @Test
    fun `deleteCompany for id that does not exist, return false`() {
        baseTest {
            val deleted = dao.deleteCompany(UUID.randomUUID())
            assertFalse(deleted)
        }
    }
    // </editor-fold>

    // <editor-fold desc="Check if position is unique">
    @Test
    fun `companyUnique() for id that exists, return false`() {
        baseTest {
            dao.insertCompany(givenAValidInsertCompanyBody())
            val unique = dao.companyUnique(givenAValidInsertCompanyBody().name)
            assertFalse(unique)
        }
    }

    @Test
    fun `companyUnique() for id that does not exist, return true`() {
        baseTest {
            val unique = dao.companyUnique("Random name")
            assertTrue(unique)
        }
    }

    // </editor-fold>

    // <editor-fold desc="List of Existing Company IDs">
    @Test
    fun `getListOfExistingCompanyIds where ids do not exist, should return empty list`() {
        withTables(CompaniesTable) {
            val list = dao.getListOfExistingCompanyIds(listOf(UUID.fromString("10000000-0000-0000-0000-000000000000"), UUID.fromString("20000000-0000-0000-0000-000000000000")))
            assertThat(list).isEmpty()
        }
    }

    @Test
    fun `getListOfExistingCompanyIds where some ids exist, should return list of existing items`() {
        baseTest {
            val id = dao.insertCompany(givenAnUnknownCompany())?.id
            val list = dao.getListOfExistingCompanyIds(listOf(id!!, UUID.fromString("20000000-0000-0000-0000-000000000000")))
            assertThat(list).hasSize(1)
        }
    }
    // </editor-fold>

    private fun baseTest(
        test: suspend Transaction.() -> Unit
    ) {
        withTables(
            CompaniesTable,
            CompaniesLinksPivotTable,
            LinksTable
        ) {
            listOf(
                Link(UUID.fromString("00000000-0000-0000-0000-000000000001"), url = "https://www.first_company.be"),
                Link(UUID.fromString("00000000-0000-0000-0000-000000000002"), url = "https://www.first_company.be/play_store", type = LinkType.PlayStore),
                Link(UUID.fromString("00000000-0000-0000-0000-000000000003"), url = "https://www.second_company.be"),
                Link(UUID.fromString("00000000-0000-0000-0000-000000000004"), url = "https://www.third_company.be"),
                Link(UUID.fromString("00000000-0000-0000-0000-000000000005"), url = "https://www.unknown_company.be"),
                Link(UUID.fromString("00000000-0000-0000-0000-000000000006"), url = "https://www.updated_company.be"),
            ).forEach { data ->
                LinksTable.insert {
                    it[id] = data.id
                    it[url] = data.url
                }
            }

            listOf(
                Company(UUID.fromString("00000000-0000-0000-0000-000000000001"), name = "First Company"),
                Company(UUID.fromString("00000000-0000-0000-0000-000000000002"), name = "Second Company"),
                Company(UUID.fromString("00000000-0000-0000-0000-000000000003"), name = "Third Company"),
            ).forEach { data ->
                CompaniesTable.insert {
                    it[id] = data.id
                    it[name] = data.name
                }
            }

            listOf(
                Pair(UUID.fromString("00000000-0000-0000-0000-000000000001"), UUID.fromString("00000000-0000-0000-0000-000000000001")),
                Pair(UUID.fromString("00000000-0000-0000-0000-000000000002"), UUID.fromString("00000000-0000-0000-0000-000000000001")),
                Pair(UUID.fromString("00000000-0000-0000-0000-000000000003"), UUID.fromString("00000000-0000-0000-0000-000000000002")),
                Pair(UUID.fromString("00000000-0000-0000-0000-000000000004"), UUID.fromString("00000000-0000-0000-0000-000000000003")),
            ).forEach { data ->
                CompaniesLinksPivotTable.insert {
                    it[linkId] = data.first
                    it[companyId] = data.second
                }
            }

            test()
        }
    }
}
