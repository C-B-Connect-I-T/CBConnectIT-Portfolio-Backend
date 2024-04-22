package com.cbconnectit.data.database.dao.companies

import com.cbconnectit.data.dto.requests.company.InsertNewCompany
import com.cbconnectit.data.dto.requests.company.UpdateCompany

object CompanyInstrumentation {

    fun givenAValidInsertCompanyBody() = InsertNewCompany("First Company")
    fun givenAValidSecondInsertCompanyBody() = InsertNewCompany("Second Company")

    fun givenAValidUpdateCompanyBody() = UpdateCompany("Updated Company")
    fun givenAValidUpdateCompanyBodyWithLink() = UpdateCompany("Updated Company", links = listOf("00000000-0000-0000-0000-000000000006"))

    fun givenAnEmptyUpdateCompanyBody() = UpdateCompany("   ")
    fun givenAnUnknownCompany() = InsertNewCompany("Unknown")
    fun givenAnUnknownCompanyWithLink() = InsertNewCompany("Unknown", links = listOf("00000000-0000-0000-0000-000000000005"))
}
