package com.cbconnectit.dao

import com.cbconnectit.data.database.dao.CompanyDaoImpl
import com.cbconnectit.data.database.tables.CompaniesLinksPivotTable
import com.cbconnectit.data.database.tables.CompaniesTable
import com.cbconnectit.data.database.tables.LinksTable
import com.cbconnectit.instrumentation.CompanyInstrumentation
import com.cbconnectit.instrumentation.CompanyInstrumentation.givenAValidInsertCompany
import com.cbconnectit.instrumentation.CompanyInstrumentation.givenAValidUpdateCompany
import com.cbconnectit.instrumentation.CompanyInstrumentation.givenAnUnknownCompany
import com.cbconnectit.instrumentation.LinkInstrumentation
import kotlinx.coroutines.delay
import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.exposed.sql.insert
import org.junit.jupiter.api.Test
import java.util.*
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class CompanyDaoImplTest : BaseDaoTest() {

    private val dao = CompanyDaoImpl()

    override suspend fun seedData() {
        // Seed links first (foreign key dependency)
        LinkInstrumentation.givenLinkList().forEach { data ->
            LinksTable.insert {
                it[id] = data.id
                it[url] = data.url
                it[type] = data.type
                it[createdAt] = data.createdAt
                it[updatedAt] = data.updatedAt
            }
        }

        // Seed additional links needed for company tests
        listOf(
            LinkInstrumentation.givenALink("00000000-0000-0000-0000-000000000005", "https://www.unknown_company.be"),
            LinkInstrumentation.givenALink("00000000-0000-0000-0000-000000000006", "https://www.updated_company.be"),
        ).forEach { data ->
            LinksTable.insert {
                it[id] = data.id
                it[url] = data.url
                it[type] = data.type
                it[createdAt] = data.createdAt
                it[updatedAt] = data.updatedAt
            }
        }

        // Seed companies
        CompanyInstrumentation.givenCompanyList().take(3).forEachIndexed { index, data ->
            CompaniesTable.insert {
                it[id] = UUID.fromString("00000000-0000-0000-0000-00000000000${index + 1}")
                it[name] = data.name
                it[createdAt] = data.createdAt
                it[updatedAt] = data.updatedAt
            }
        }

        // Seed company-link relationships
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
    }

    // <editor-fold desc="Get all companies">
    @Test
    fun `getCompanies but none exists, return empty list`() = runTest(shouldSeedData = false) {
        val list = dao.getCompanies()
        assertThat(list).isEmpty()
    }

    @Test
    fun `getCompanies return the list`() = runTest {
        val list = dao.getCompanies()
        assertThat(list).hasSize(3)
    }
    // </editor-fold>

    // <editor-fold desc="Get specific company by id">
    @Test
    fun `getCompany where item exists, return correct company`() = runTest {
        val company = dao.getCompanyById(UUID.fromString("00000000-0000-0000-0000-000000000001"))

        assertThat(company).matches {
            it?.name == CompanyInstrumentation.givenCompanyList()[0].name
        }
    }

    @Test
    fun `getCompany where item does not exists, return 'null'`() = runTest {
        val company = dao.getCompanyById(UUID.randomUUID())

        assertNull(company)
    }
    // </editor-fold>

    // <editor-fold desc="Create new company">
    @Test
    fun `insertCompany where information is correct, database is storing company and returning correct content`() = runTest {
        val validCompany = givenAnUnknownCompany()
        val company = dao.insertCompany(validCompany)

        assertThat(company).matches {
            it?.name == validCompany.name &&
                    it.createdAt == it.updatedAt
        }
    }

    @Test
    fun `insertCompany where information is correct with a link, database is storing company and returning correct content`() = runTest {
        val validCompany = givenAnUnknownCompany(links = listOf("00000000-0000-0000-0000-000000000005"))
        val company = dao.insertCompany(validCompany)

        assertThat(company).matches {
            it?.name == validCompany.name &&
                    it.createdAt == it.updatedAt &&
                    it.links.isNotEmpty()
        }
    }
    // </editor-fold>

    // <editor-fold desc="Update company">
    @Test
    fun `updateCompany where information is correct, database is storing information and returning the correct content`() = runTest {
        // adding a delay so there is a clear difference between `updatedAt` and `createdAt`
        delay(1000)

        val validUpdateCompany = givenAValidUpdateCompany()
        val company = dao.updateCompany(UUID.fromString("00000000-0000-0000-0000-000000000001"), validUpdateCompany)

        assertThat(company).matches {
            it?.name == validUpdateCompany.name &&
                    it.createdAt != it.updatedAt
        }
    }

    @Test
    fun `updateCompany where information is correct and a link is provided, database is storing information and returning the correct content`() = runTest {
        // adding a delay so there is a clear difference between `updatedAt` and `createdAt`
        delay(1000)

        val validUpdateCompany = givenAValidUpdateCompany(links = listOf("00000000-0000-0000-0000-000000000001"))
        val company = dao.updateCompany(UUID.fromString("00000000-0000-0000-0000-000000000001"), validUpdateCompany)

        assertThat(company).matches {
            it?.name == validUpdateCompany.name &&
                    it.createdAt != it.updatedAt &&
                    it.links.isNotEmpty() &&
                    it.links.size == 1
        }
    }

    @Test
    fun `updateCompany where information is correct but company with id does not exist, database does nothing and returns 'null'`() = runTest {
        val validCompany = givenAValidUpdateCompany()
        val company = dao.updateCompany(UUID.randomUUID(), validCompany)

        assertNull(company)
    }
    // </editor-fold>

    // <editor-fold desc="Delete company">
    @Test
    fun `deleteCompany for id that exists, return true`() = runTest {
        val deleted = dao.deleteCompany(UUID.fromString("00000000-0000-0000-0000-000000000001"))
        assertTrue(deleted)
    }

    @Test
    fun `deleteCompany for id that does not exist, return false`() = runTest {
        val deleted = dao.deleteCompany(UUID.randomUUID())
        assertFalse(deleted)
    }
    // </editor-fold>

    // <editor-fold desc="Check if position is unique">
    @Test
    fun `companyUnique() for id that exists, return false`() = runTest {
        dao.insertCompany(givenAValidInsertCompany())
        val unique = dao.companyUnique(givenAValidInsertCompany().name)
        assertFalse(unique)
    }

    @Test
    fun `companyUnique() for id that does not exist, return true`() = runTest {
        val unique = dao.companyUnique("Random name")
        assertTrue(unique)
    }

    // </editor-fold>

    // <editor-fold desc="List of Existing Company IDs">
    @Test
    fun `getListOfExistingCompanyIds where ids do not exist, should return empty list`() = runTest(shouldSeedData = false) {
        val list = dao.getListOfExistingCompanyIds(listOf(UUID.fromString("10000000-0000-0000-0000-000000000000"), UUID.fromString("20000000-0000-0000-0000-000000000000")))
        assertThat(list).isEmpty()
    }

    @Test
    fun `getListOfExistingCompanyIds where some ids exist, should return list of existing items`() = runTest {
        val id = dao.insertCompany(givenAnUnknownCompany())?.id
        val list = dao.getListOfExistingCompanyIds(listOf(id!!, UUID.fromString("20000000-0000-0000-0000-000000000000")))
        assertThat(list).hasSize(1)
    }
    // </editor-fold>
}
