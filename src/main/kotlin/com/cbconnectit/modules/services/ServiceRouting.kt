package com.cbconnectit.modules.services

import com.cbconnectit.data.dto.requests.service.InsertNewService
import com.cbconnectit.data.dto.requests.service.UpdateService
import com.cbconnectit.plugins.statuspages.ErrorInvalidParameters
import com.cbconnectit.plugins.statuspages.ErrorMissingRequiredMedia
import com.cbconnectit.utils.ParamConstants
import com.cbconnectit.utils.Parts
import com.cbconnectit.utils.getFile
import com.cbconnectit.utils.getPayload
import com.cbconnectit.utils.getServiceId
import com.cbconnectit.utils.receiveOrRespondWithError
import com.cbconnectit.utils.sendOk
import com.cbconnectit.utils.toParts
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json

fun Route.serviceRouting(json: Json, serviceController: ServiceController) {

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
                val (imageFile, bannerImageFile, insertNewService) = getServiceMediaAndPayload<InsertNewService>(json)
                if (imageFile == null || bannerImageFile == null) throw ErrorMissingRequiredMedia

                val service = serviceController.postService(insertNewService, imageFile, bannerImageFile)
                call.respond(HttpStatusCode.Created, service)
            }

            put("{${ParamConstants.SERVICE_ID_KEY}}") {
                val serviceId = call.getServiceId()
                val (imageFile, bannerImageFile, updateService) = getServiceMediaAndPayload<UpdateService>(json)

                val service = serviceController.updateServiceById(serviceId, updateService, imageFile, bannerImageFile)
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

private suspend inline fun <reified T> RoutingContext.getServiceMediaAndPayload(json: Json): Triple<Parts.File?, Parts.File?, T> {
    val contentType = call.request.contentType()

    return if (contentType.match(ContentType.MultiPart.FormData)) {
        val parts = call.receiveMultipart().toParts()
        val imageFile = parts.getFile("image")
        val bannerImageFile = parts.getFile("bannerImage")
        val payload = parts.getPayload<T>(json) ?: throw ErrorInvalidParameters
        Triple(imageFile, bannerImageFile, payload)
    } else {
        val payload = call.receiveOrRespondWithError<T>()
        Triple(null, null, payload)
    }
}
