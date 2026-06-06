package com.cbconnectit.modules.auth

import com.cbconnectit.data.dto.requests.CreateTokenDto
import com.cbconnectit.data.dto.requests.RefreshTokenDto
import com.cbconnectit.data.dto.responses.CredentialsResponse
import com.cbconnectit.plugins.statuspages.ErrorInvalidToken
import com.cbconnectit.utils.accessTokenFromCookie
import com.cbconnectit.utils.accessTokenFromHeader
import com.cbconnectit.utils.receiveOrRespondWithError
import com.cbconnectit.utils.refreshTokenFromCookie
import com.cbconnectit.utils.sendOk
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.authRouting(authController: AuthController) {

    post("api/oauth/token") {
        val request = call.receiveOrRespondWithError<CreateTokenDto>()
        val tokens = authController.authorizeUser(request)

        if (call.isTrustedWebClient()) {
            call.setAuthCookies(tokens)
            sendOk()
        } else {
            call.respond(tokens)
        }
    }

    post("api/oauth/refresh") {
        val refreshToken = call.getRefreshTokenFromCookieOrBody()
        if (refreshToken.isNullOrBlank()) throw ErrorInvalidToken

        val newTokens = authController.refreshTokens(RefreshTokenDto(refreshToken))

        if (call.isTrustedWebClient()) {
            call.setAuthCookies(newTokens)
            sendOk()
        } else {
            call.respond(newTokens)
        }
    }

    get("api/oauth/status") {
        // Extract tokens from cookies (web clients) or headers (API clients)
        val accessToken = call.accessTokenFromCookie ?: call.accessTokenFromHeader
        val refreshToken = call.refreshTokenFromCookie

        val status = authController.getAuthStatus(accessToken, refreshToken)
        call.respond(status)
    }

    post("api/oauth/logout") {
        // Extract refresh token from cookie or body (supports both web and API clients)
        val refreshToken = call.getRefreshTokenFromCookieOrBody()

        authController.logout(refreshToken)
        call.clearAuthCookies()
        sendOk()
    }
//
//    post("api/oauth/forgot-password") {
//        val request = call.receiveOrRespondWithError<ForgotPasswordDto>()
//        authController.forgotPassword(request)
//        sendOk()
//    }
//
//    post("api/oauth/reset-password") {
//        val request = call.receiveOrRespondWithError<ResetPasswordDto>()
//        authController.resetPassword(request)
//        sendOk()
//    }
}

private fun ApplicationCall.isSecureConnection(): Boolean {
    // Check X-Forwarded-Proto header first (set by reverse proxies)
    val forwardedProto = request.headers["X-Forwarded-Proto"]
    if (forwardedProto != null) {
        return forwardedProto.equals("https", ignoreCase = true)
    }
    
    // Fallback to direct connection scheme
    return request.origin.scheme == "https"
}

private fun ApplicationCall.setAuthCookies(tokens: CredentialsResponse) {
    val isSecure = isSecureConnection()

    response.cookies.append(
        Cookie(
            name = JwtConfig.ACCESS_TOKEN_COOKIE,
            value = tokens.accessToken,
            maxAge = JwtConfig.ACCESS_TOKEN_MAX_AGE,
            httpOnly = true,
            secure = isSecure,
            path = "/api",
            extensions = mapOf("SameSite" to "Lax")
        )
    )

    response.cookies.append(
        Cookie(
            name = JwtConfig.REFRESH_TOKEN_COOKIE,
            value = tokens.refreshToken,
            maxAge = JwtConfig.REFRESH_TOKEN_MAX_AGE,
            httpOnly = true,
            secure = isSecure,
            path = "/api/oauth",
            extensions = mapOf("SameSite" to "Lax")
        )
    )
}

private fun ApplicationCall.clearAuthCookies() {
    val isSecure = isSecureConnection()

    response.cookies.append(
        Cookie(
            name = JwtConfig.ACCESS_TOKEN_COOKIE,
            value = "",
            maxAge = 0,
            httpOnly = true,
            secure = isSecure,
            path = "/api",
            extensions = mapOf("SameSite" to "Lax")
        )
    )

    response.cookies.append(
        Cookie(
            name = JwtConfig.REFRESH_TOKEN_COOKIE,
            value = "",
            maxAge = 0,
            httpOnly = true,
            secure = isSecure,
            path = "/api/oauth",
            extensions = mapOf("SameSite" to "Lax")
        )
    )
}

private suspend fun ApplicationCall.getRefreshTokenFromCookieOrBody(): String? {
    val refreshTokenFromBody = try {
        // Get refresh token from body if not found in cookie (e.g. for mobile clients)
        receiveOrRespondWithError<RefreshTokenDto>().refreshToken
    } catch (_: Exception) {
        null
    }

    return refreshTokenFromCookie ?: refreshTokenFromBody
}
