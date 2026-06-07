package com.cbconnectit.plugins.statuspages

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.exceptions.ExposedSQLException

fun Application.configureStatusPages() {
    // "example/" should resolve to "example/index.html" if present, but default ktor behavior rejects trailing slashes.
    install(IgnoreTrailingSlash)

    install(StatusPages) {
        generalStatusPages()
    }
}

fun StatusPagesConfig.generalStatusPages() {
    exception<ApiException> { call, cause ->
        call.respond(cause.statusCode, cause.toErrorResponse())
    }

    exception<ExposedSQLException> { call, realCause ->
        val cause = ApiException("error", realCause.localizedMessage, HttpStatusCode.InternalServerError)
        call.respond(cause.statusCode, cause.toErrorResponse())
    }

    exception<NullPointerException> { call, _ ->
        val some = ErrorMissingParameters
        call.respond(some.statusCode, some.toErrorResponse())
    }

    exception<Throwable> { call, _ ->
        val cause = InternalServerException()
        call.respond(cause.statusCode, cause.toErrorResponse())
    }
}
