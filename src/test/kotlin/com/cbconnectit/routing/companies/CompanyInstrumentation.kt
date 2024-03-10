package com.cbconnectit.routing.companies

import com.cbconnectit.data.dto.requests.company.InsertNewCompany
import com.cbconnectit.data.dto.requests.company.CompanyDto
import com.cbconnectit.data.dto.requests.company.UpdateCompany
import com.cbconnectit.utils.toDatabaseString
import java.time.LocalDateTime
import java.util.*

object CompanyInstrumentation {
    fun givenAValidInsertCompany() = InsertNewCompany("First company")
    fun givenAValidUpdateCompanyBody() = UpdateCompany("Updated Company")

    fun givenAnEmptyInsertCompanyBody() = InsertNewCompany("    ")


    fun givenCompanyList() = listOf(
        givenACompany("Company no. 1"),
        givenACompany("Company no. 2"),
        givenACompany("Company no. 3"),
        givenACompany("Unknown"),
    )

    fun givenACompany(name: String = "First company") = run {
        val time = LocalDateTime.now().toDatabaseString()
        CompanyDto(
            id = UUID.randomUUID().toString(),
            name = name,
            createdAt = time,
            updatedAt = time
        )
    }
}