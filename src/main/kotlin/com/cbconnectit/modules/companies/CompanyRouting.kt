package com.cbconnectit.modules.companies

import com.cbconnectit.data.dto.requests.company.InsertNewCompany
import com.cbconnectit.data.dto.requests.company.UpdateCompany
import com.cbconnectit.utils.ParamConstants
import com.cbconnectit.utils.getCompanyId
import com.cbconnectit.utils.receiveOrRespondWithError
import com.cbconnectit.utils.sendOk
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.companyRouting(companyController: CompanyController) {

    route("companies") {
        get {
            val companies = companyController.getCompanies()
            call.respond(companies)
        }

        get("/{${ParamConstants.COMPANY_ID_KEY}}") {
            val companyId = call.getCompanyId()
            val company = companyController.getCompanyById(companyId)
            call.respond(company)
        }

        authenticate {
            post {
                val insertNewCompany = call.receiveOrRespondWithError<InsertNewCompany>()
                val company = companyController.postCompany(insertNewCompany)
                call.respond(HttpStatusCode.Created, company)
            }

            put("{${ParamConstants.COMPANY_ID_KEY}}") {
                val companyId = call.getCompanyId()
                val updateCompany = call.receiveOrRespondWithError<UpdateCompany>()
                val company = companyController.updateCompanyById(companyId, updateCompany)
                call.respond(company)
            }

            delete("{${ParamConstants.COMPANY_ID_KEY}}") {
                val companyId = call.getCompanyId()
                companyController.deleteCompanyById(companyId)
                sendOk()
            }
        }
    }
}
