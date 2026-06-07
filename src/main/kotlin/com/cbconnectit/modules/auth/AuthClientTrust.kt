package com.cbconnectit.modules.auth

import com.cbconnectit.data.database.tables.Constants
import io.ktor.http.*
import io.ktor.server.application.*

internal fun ApplicationCall.isTrustedWebClient(): Boolean {
    val clientType = request.headers[Constants.CLIENT_TYPE_HEADER]
    if (clientType != "web") return false

    val originHeader = request.headers[HttpHeaders.Origin]
    val refererHeader = request.headers[HttpHeaders.Referrer]
    val normalizedOrigin = normalizeTrustedOrigin(originHeader)
        ?: normalizeTrustedOrigin(refererHeader)
        ?: return false

    return Constants.trustedWebOrigins.contains(normalizedOrigin)
}

private fun normalizeTrustedOrigin(rawHeader: String?): String? {
    if (rawHeader.isNullOrBlank()) return null

    val parsedUrl = try {
        Url(rawHeader)
    } catch (_: Throwable) {
        return null
    }

    if (parsedUrl.host.isBlank()) return null

    val normalizedHost = parsedUrl.host.lowercase()
    return when (parsedUrl.port) {
        -1, 80, 443 -> normalizedHost
        else -> "$normalizedHost:${parsedUrl.port}"
    }
}
