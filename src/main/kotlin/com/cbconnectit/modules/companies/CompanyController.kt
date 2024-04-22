package com.cbconnectit.modules.companies

import com.cbconnectit.data.dto.requests.company.CompanyDto
import com.cbconnectit.data.dto.requests.company.InsertNewCompany
import com.cbconnectit.data.dto.requests.company.UpdateCompany
import com.cbconnectit.domain.interfaces.ICompanyDao
import com.cbconnectit.domain.interfaces.ILinkDao
import com.cbconnectit.domain.models.company.toDto
import com.cbconnectit.modules.BaseController
import com.cbconnectit.plugins.dbQuery
import com.cbconnectit.statuspages.ErrorDuplicateEntity
import com.cbconnectit.statuspages.ErrorFailedCreate
import com.cbconnectit.statuspages.ErrorFailedDelete
import com.cbconnectit.statuspages.ErrorFailedUpdate
import com.cbconnectit.statuspages.ErrorInvalidParameters
import com.cbconnectit.statuspages.ErrorNotFound
import com.cbconnectit.statuspages.ErrorUnknownLinkIdsForCreateCompany
import com.cbconnectit.statuspages.ErrorUnknownLinkIdsForUpdateCompany
import org.koin.core.component.inject
import java.util.*

class CompanyControllerImpl : BaseController(), CompanyController {

    private val companyDao by inject<ICompanyDao>()
    private val linkDao by inject<ILinkDao>()

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

        val links = insertNewCompany.links ?: emptyList()
        val linkUUIDS = links.map { UUID.fromString(it) }
        val existingLinkUUIDs = linkDao.getListOfExistingLinkIds(linkUUIDS)

        // A project can only be added when all the added tags exist
        if (existingLinkUUIDs.count() != links.count()) {
            val nonExistingIds = linkUUIDS.filterNot { existingLinkUUIDs.contains(it) }
            throw ErrorUnknownLinkIdsForCreateCompany(nonExistingIds)
        }

        companyDao.insertCompany(insertNewCompany)?.toDto() ?: throw ErrorFailedCreate
    }

    override suspend fun updateCompanyById(companyId: UUID, updateCompany: UpdateCompany): CompanyDto = dbQuery {
        if (!updateCompany.isValid) throw ErrorInvalidParameters

        val positionUnique = companyDao.companyUnique(updateCompany.name)
        if (!positionUnique) throw ErrorDuplicateEntity

        val links = updateCompany.links ?: emptyList()
        val linkUUIDS = links.map { UUID.fromString(it) }
        val existingLinkUUIDs = linkDao.getListOfExistingLinkIds(linkUUIDS)

        // A project can only be added when all the added tags exist
        if (existingLinkUUIDs.count() != links.count()) {
            val nonExistingIds = linkUUIDS.filterNot { existingLinkUUIDs.contains(it) }
            throw ErrorUnknownLinkIdsForUpdateCompany(nonExistingIds)
        }

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
