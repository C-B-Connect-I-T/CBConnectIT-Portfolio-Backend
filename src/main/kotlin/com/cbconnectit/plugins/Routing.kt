package com.cbconnectit.plugins

import com.cbconnectit.modules.auth.authRouting
import com.cbconnectit.modules.companies.companyRouting
import com.cbconnectit.modules.jobPositions.jobPositionRouting
import com.cbconnectit.modules.links.linkRouting
import com.cbconnectit.modules.projects.projectRouting
import com.cbconnectit.modules.services.serviceRouting
import com.cbconnectit.modules.tags.tagRouting
import com.cbconnectit.modules.testimonials.testimonialRouting
import com.cbconnectit.modules.users.userRouting
import com.cbconnectit.statuspages.ErrorMissingParameters
import com.cbconnectit.statuspages.InternalServerException
import com.cbconnectit.statuspages.generalStatusPages
import com.cbconnectit.statuspages.toErrorResponse
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    // "example/" should resolve to "example/index.html" if present, but default ktor behavior rejects trailing slashes.
    this.install(IgnoreTrailingSlash)

    install(StatusPages) {
        generalStatusPages()

        exception<NullPointerException> { call, _ ->
            val some = ErrorMissingParameters
            call.respond(some.statusCode, some.toErrorResponse())
        }

        exception<Throwable> { call, _ ->
            val cause = InternalServerException()
            call.respond(cause.statusCode, cause.toErrorResponse())
        }
    }
    routing {
        staticResources("/", "/static/site")

        authRouting()

        route("api/v1") {
            userRouting()
            tagRouting()
            linkRouting()
            serviceRouting()
            projectRouting()
            jobPositionRouting()
            companyRouting()
            testimonialRouting()
        }
    }
}
