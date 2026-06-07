package com.cbconnectit.plugins

import com.cbconnectit.data.database.tables.Constants
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.cors.routing.*

fun Application.configureHTTP() {
    install(CORS) {
        // CORS configuration for browser-based web clients
        // Note: CORS only affects browsers - mobile apps and server-to-server API clients
        // are NOT restricted by CORS and should use Authorization header authentication

        // When using allowCredentials = true, you must specify exact hosts (no wildcards)
        // These hosts must match the trustedWebOrigins in Security.kt and Constants.kt

        // Origin-based authentication strategy (see AuthValidator.kt):
        // - Web clients (from these trusted origins): Use HTTP-only cookies for XSS protection
        // - API clients (other origins): Use Authorization header (Bearer token)
        Constants.trustedWebOrigins.forEach {
            allowHost(it)
        }

        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Get)

        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.Accept)
        allowHeader(HttpHeaders.Authorization)
        allowHeader(Constants.CLIENT_TYPE_HEADER) // Required for web/API client detection
        allowHeader(Constants.AUTH_METHOD_HEADER) // Required for web/API client detection

        // Required for HTTP-only cookies
        allowCredentials = true

        // Required for sending application/json content type in requests
        allowNonSimpleContentTypes = true
    }
}
