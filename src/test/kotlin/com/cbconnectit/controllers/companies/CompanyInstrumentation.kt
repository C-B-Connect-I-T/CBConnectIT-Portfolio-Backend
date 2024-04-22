package com.cbconnectit.controllers.companies

import com.cbconnectit.data.dto.requests.company.InsertNewCompany
import com.cbconnectit.data.dto.requests.company.UpdateCompany
import com.cbconnectit.domain.models.company.Company
import java.util.*

object CompanyInstrumentation {

    fun givenAnInvalidInsertCompany() = InsertNewCompany("  ")
    fun givenAnInvalidUpdateCompany() = UpdateCompany("  ")
    fun givenAValidInsertCompany() = InsertNewCompany("New Company")
    fun givenAValidInsertCompanyWithLink() = InsertNewCompany("New Company", links = listOf("00000000-0000-0000-0000-000000000001"))
    fun givenAValidUpdateCompany() = UpdateCompany("Updated Company")
    fun givenAValidUpdateCompanyWithLink() = UpdateCompany("Updated Company", links = listOf("00000000-0000-0000-0000-000000000001"))

    fun givenCompanyList() = listOf(
        givenACompany("First Company"),
        givenACompany("Second Company"),
        givenACompany("Third Company"),
        givenACompany("Fourth Company"),
    )

    fun givenACompany(name: String = "First Company") = Company(UUID.randomUUID(), name)
}
