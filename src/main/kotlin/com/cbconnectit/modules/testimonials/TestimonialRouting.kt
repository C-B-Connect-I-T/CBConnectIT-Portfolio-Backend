package com.cbconnectit.modules.testimonials

import com.cbconnectit.data.dto.requests.testimonial.InsertTestimonial
import com.cbconnectit.data.dto.requests.testimonial.UpdateTestimonial
import com.cbconnectit.plugins.statuspages.ErrorInvalidParameters
import com.cbconnectit.utils.ParamConstants
import com.cbconnectit.utils.Parts
import com.cbconnectit.utils.getFile
import com.cbconnectit.utils.getPayload
import com.cbconnectit.utils.getTestimonialId
import com.cbconnectit.utils.receiveOrRespondWithError
import com.cbconnectit.utils.sendOk
import com.cbconnectit.utils.toParts
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json

fun Route.testimonialRouting(
    json: Json,
    testimonialController: TestimonialController
) {

    route("testimonials") {
        get {
            val testimonials = testimonialController.readAll()
            call.respond(testimonials)
        }

        get("/{${ParamConstants.TESTIMONIAL_ID_KEY}}") {
            val testimonialIdentifier = call.getTestimonialId()
            val testimonial = testimonialController.readById(testimonialIdentifier)
            call.respond(testimonial)
        }

        authenticate {
            post {
                val (imageFile, insertTestimonial) = getImageFileAndData<InsertTestimonial>(json)

                val testimonial = testimonialController.create(insertTestimonial, imageFile)
                call.respond(HttpStatusCode.Created, testimonial)
            }

            route("{${ParamConstants.TESTIMONIAL_ID_KEY}}") {
                put {
                    val testimonialId = call.getTestimonialId()
                    val (imageFile, updateTestimonial) = getImageFileAndData<UpdateTestimonial>(json)

                    val testimonial = testimonialController.updateById(testimonialId, updateTestimonial, imageFile)
                    call.respond(testimonial)
                }

                delete {
                    val testimonialId = call.getTestimonialId()
                    testimonialController.deleteById(testimonialId)
                    sendOk()
                }

                // Dedicated image management endpoints
                route("image") {
                    put {
                        val categoryId = call.getTestimonialId()

                        val (imageFile, request) = getImageFileAndData<Map<String, String>>(json)
                        if (imageFile == null) throw ErrorInvalidParameters // Image file is required for this endpoint
                        val altText = request["altText"] ?: ""

                        val updatedCategory = testimonialController.updateTestimonialAvatar(categoryId, imageFile, altText)
                        call.respond(updatedCategory)
                    }

                    delete {
                        val categoryId = call.getTestimonialId()
                        val updatedCategory = testimonialController.deleteTestimonialAvatar(categoryId)
                        call.respond(updatedCategory)
                    }
                }
            }
        }
    }
}

private suspend inline fun <reified T> RoutingContext.getImageFileAndData(json: Json): Pair<Parts.File?, T> {
    val contentType = call.request.contentType()

    return if (contentType.match(ContentType.MultiPart.FormData)) {
        // Multipart request with image
        val parts = call.receiveMultipart().toParts()
        val imageFile = parts.getFile("image") // Optional
        val insertCategory = parts.getPayload<T>(json) ?: throw ErrorInvalidParameters

        Pair(imageFile, insertCategory)
    } else {
        // JSON request without image
        val insertCategory = call.receiveOrRespondWithError<T>()
        Pair(null, insertCategory)
    }
}
