package com.cbconnectit.plugins.statuspages

import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureStatusPages() {
    // "example/" should resolve to "example/index.html" if present, but default ktor behavior rejects trailing slashes.
    install(IgnoreTrailingSlash)

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

        exception<Exception> { call, _ ->
            val cause = InternalServerException()
            call.respond(cause.statusCode, cause.toErrorResponse())
        }
    }
}
