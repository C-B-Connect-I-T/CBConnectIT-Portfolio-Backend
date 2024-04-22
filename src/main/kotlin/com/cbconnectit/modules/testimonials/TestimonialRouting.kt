package com.cbconnectit.modules.testimonials

import com.cbconnectit.data.dto.requests.testimonial.InsertNewTestimonial
import com.cbconnectit.data.dto.requests.testimonial.UpdateTestimonial
import com.cbconnectit.utils.ParamConstants
import com.cbconnectit.utils.getTestimonialId
import com.cbconnectit.utils.receiveOrRespondWithError
import com.cbconnectit.utils.sendOk
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.testimonialRouting() {

    val testimonialController by inject<TestimonialController>()

    route("testimonials") {
        get {
            val testimonials = testimonialController.getTestimonials()
            call.respond(testimonials)
        }

        get("/{${ParamConstants.TESTIMONIAL_ID_KEY}}") {
            val testimonialIdentifier = call.getTestimonialId()
            val testimonial = testimonialController.getTestimonialById(testimonialIdentifier)
            call.respond(testimonial)
        }

        authenticate {
            post {
                val insertNewTestimonial = call.receiveOrRespondWithError<InsertNewTestimonial>()
                val testimonial = testimonialController.postTestimonial(insertNewTestimonial)
                call.respond(HttpStatusCode.Created, testimonial)
            }

            put("{${ParamConstants.TESTIMONIAL_ID_KEY}}") {
                val testimonialId = call.getTestimonialId()
                val updateTestimonial = call.receiveOrRespondWithError<UpdateTestimonial>()
                val testimonial = testimonialController.updateTestimonialById(testimonialId, updateTestimonial)
                call.respond(testimonial)
            }

            delete("{${ParamConstants.TESTIMONIAL_ID_KEY}}") {
                val testimonialId = call.getTestimonialId()
                testimonialController.deleteTestimonialById(testimonialId)
                sendOk()
            }
        }
    }
}
