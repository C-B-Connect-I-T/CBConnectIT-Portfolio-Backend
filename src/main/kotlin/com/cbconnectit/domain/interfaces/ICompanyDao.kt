package com.cbconnectit.domain.interfaces

import com.cbconnectit.data.dto.requests.company.InsertNewCompany
import com.cbconnectit.data.dto.requests.company.UpdateCompany
import com.cbconnectit.domain.models.company.Company
import java.util.*

interface ICompanyDao {

    fun getCompanyById(id: UUID): Company?
    fun getCompanies(): List<Company>
    fun insertCompany(insertNewCompany: InsertNewCompany): Company?
    fun updateCompany(id: UUID, updateCompany: UpdateCompany): Company?
    fun deleteCompany(id: UUID): Boolean
    fun companyUnique(name: String): Boolean
    fun getListOfExistingCompanyIds(companyIds: List<UUID>): List<UUID>
}
