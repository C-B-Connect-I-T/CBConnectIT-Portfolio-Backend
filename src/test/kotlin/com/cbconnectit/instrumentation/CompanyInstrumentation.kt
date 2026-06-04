package com.cbconnectit.instrumentation

import com.cbconnectit.data.dto.requests.company.InsertNewCompany
import com.cbconnectit.data.dto.requests.company.UpdateCompany
import com.cbconnectit.domain.models.company.Company
import java.time.LocalDateTime
import java.util.*

object CompanyInstrumentation {

    fun givenAnInvalidInsertCompany() = InsertNewCompany("  ")

    fun givenAnInvalidUpdateCompany() = UpdateCompany("  ")

    fun givenAValidInsertCompany(
        name: String = "New Company",
        links: List<String> = emptyList()
    ) = InsertNewCompany(name, links = links)

    fun givenAValidUpdateCompany(
        name: String = "Updated Company",
        links: List<String> = emptyList()
    ) = UpdateCompany(name, links = links)

    fun givenAnUnknownCompany(links: List<String> = emptyList()) = InsertNewCompany("Unknown", links = links)

    fun givenCompanyList() = listOf(
        givenACompany(name = "First Company"),
        givenACompany(name = "Second Company"),
        givenACompany(name = "Third Company"),
        givenACompany(name = "Fourth Company"),
    )

    fun givenACompany(
        id: UUID = UUID.randomUUID(),
        name: String = "First Company"
    ) = Company(
        id,
        name,
        emptyList(),
        LocalDateTime.now(),
        LocalDateTime.now()
    )
}
