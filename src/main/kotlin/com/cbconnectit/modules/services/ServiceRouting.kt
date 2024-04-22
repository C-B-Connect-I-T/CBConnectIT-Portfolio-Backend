package com.cbconnectit.modules.services

import com.cbconnectit.data.dto.requests.service.InsertNewService
import com.cbconnectit.data.dto.requests.service.UpdateService
import com.cbconnectit.utils.ParamConstants
import com.cbconnectit.utils.getServiceId
import com.cbconnectit.utils.receiveOrRespondWithError
import com.cbconnectit.utils.sendOk
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.serviceRouting() {

    val serviceController by inject<ServiceController>()

    route("services") {
        get {
            val services = serviceController.getServices()
            call.respond(services)
        }

        get("/{${ParamConstants.SERVICE_ID_KEY}}") {
            val serviceIdentifier = call.getServiceId()
            val service = serviceController.getServiceById(serviceIdentifier)
            call.respond(service)
        }

        authenticate {
            post {
                val insertNewService = call.receiveOrRespondWithError<InsertNewService>()
                val service = serviceController.postService(insertNewService)
                call.respond(HttpStatusCode.Created, service)
            }

            put("{${ParamConstants.SERVICE_ID_KEY}}") {
                val serviceId = call.getServiceId()
                val updateService = call.receiveOrRespondWithError<UpdateService>()
                val service = serviceController.updateServiceById(serviceId, updateService)
                call.respond(service)
            }

            delete("{${ParamConstants.SERVICE_ID_KEY}}") {
                val serviceId = call.getServiceId()
                serviceController.deleteServiceById(serviceId)
                sendOk()
            }
        }
    }
}
