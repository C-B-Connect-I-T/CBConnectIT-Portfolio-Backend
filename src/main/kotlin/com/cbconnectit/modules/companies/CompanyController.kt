package com.cbconnectit.modules.companies

import com.cbconnectit.data.dto.requests.company.InsertNewCompany
import com.cbconnectit.data.dto.requests.company.CompanyDto
import com.cbconnectit.data.dto.requests.company.UpdateCompany
import com.cbconnectit.domain.interfaces.ICompanyDao
import com.cbconnectit.domain.models.company.toDto
import com.cbconnectit.modules.BaseController
import com.cbconnectit.modules.companies.CompanyController
import com.cbconnectit.plugins.dbQuery
import com.cbconnectit.statuspages.*
import org.koin.core.component.inject
import java.util.*

class CompanyControllerImpl : BaseController(), CompanyController {

    private val companyDao by inject<ICompanyDao>()

    override suspend fun getCompanies(): List<CompanyDto> = dbQuery {
        companyDao.getCompanies().map { it.toDto() }
    }

    override suspend fun getCompanyById(companyId: UUID): CompanyDto = dbQuery {
        companyDao.getCompanyById(companyId)?.toDto() ?: throw ErrorNotFound
    }

    override suspend fun postCompany(insertNewCompany: InsertNewCompany): CompanyDto = dbQuery {
        if (!insertNewCompany.isValid) throw ErrorInvalidParameters

        val positionUnique = companyDao.companyUnique(insertNewCompany.name)
        if (!positionUnique) throw ErrorDuplicateEntity

        companyDao.insertCompany(insertNewCompany)?.toDto() ?: throw ErrorFailedCreate
    }

    override suspend fun updateCompanyById(companyId: UUID, updateCompany: UpdateCompany): CompanyDto = dbQuery {
        if (!updateCompany.isValid) throw ErrorInvalidParameters

        val positionUnique = companyDao.companyUnique(updateCompany.name)
        if (!positionUnique) throw ErrorDuplicateEntity

        companyDao.updateCompany(companyId, updateCompany)?.toDto() ?: throw ErrorFailedUpdate
    }

    override suspend fun deleteCompanyById(companyId: UUID) = dbQuery {
        val deleted = companyDao.deleteCompany(companyId)
        if (!deleted) throw ErrorFailedDelete
    }
}

interface CompanyController {
    suspend fun getCompanies(): List<CompanyDto>
    suspend fun getCompanyById(companyId: UUID): CompanyDto
    suspend fun postCompany(insertNewCompany: InsertNewCompany): CompanyDto
    suspend fun updateCompanyById(companyId: UUID, updateCompany: UpdateCompany): CompanyDto
    suspend fun deleteCompanyById(companyId: UUID)
}