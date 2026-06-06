package com.cbconnectit.modules.auth

import com.auth0.jwt.interfaces.JWTVerifier
import com.cbconnectit.domain.interfaces.IUserDao
import com.cbconnectit.domain.models.user.User
import com.cbconnectit.plugins.dbTransactionalQuery
import com.cbconnectit.plugins.statuspages.ApiException
import com.cbconnectit.plugins.statuspages.ErrorUnauthorized
import com.cbconnectit.utils.accessTokenFromCookie
import io.ktor.http.auth.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.util.*
import java.util.*

private val AuthenticationExceptionKey = AttributeKey<ApiException>("AuthenticationException")

fun JWTAuthenticationProvider.Config.setupAuthentication(
    jwtVerifier: JWTVerifier,
    function: suspend (JWTCredential) -> User?
) {
    verifier(jwtVerifier)

    // Origin-based authentication strategy:
    // - Web clients (from trusted origins): MUST use cookies, Authorization header is ignored for security
    // - API clients (other origins): MUST use Authorization header, cookies are ignored
    // This prevents XSS attacks on web clients while supporting API integrations
    // Security: We require BOTH Origin (browser-controlled) and X-Client-Type (client-controlled)
    // so only legitimate web clients from trusted origins can use cookie authentication.
    // TODO: In the future, we could consider more advanced strategies as there is still a risk that an attacker stole a token from the cookie and uses a curl/Postman client to make malicious API calls.
    //  To mitigate this, we could implement additional checks like IP address or user agent validation, or even implement a separate token type for web clients that has stricter validation rules.
    authHeader { call ->
        val isTrustedWebClient = call.isTrustedWebClient()

        when {
            isTrustedWebClient -> {
                // Trusted web client: Only accept cookie-based authentication
                // Ignore Authorization header to prevent XSS attacks
                call.accessTokenFromCookie?.let {
                    try {
                        HttpAuthHeader.Single("Bearer", it)
                    } catch (_: Exception) {
                        null
                    }
                }
            }

            else -> {
                // API client or untrusted origin: Only accept Authorization header
                // Ignore cookies to ensure API clients don't accidentally use them
                call.request.parseAuthorizationHeader()
            }
        }
    }

    // This validate block will catch any ApiException thrown by the validation function and store it in call attributes.
    // The challenge block will then check for this exception and throw it, allowing us to propagate specific error messages instead of just returning a generic UnauthorizedResponse.
    // This is necessary because the `validateUser` function can throw an ErrorUnverifiedAccount exception, and we want to return that specific error message to the client instead of just saying "Unauthorized".
    validate { credential ->
        try {
            function(credential)
        } catch (e: ApiException) {
            // Store the exception in call attributes so the challenge can throw it
            attributes.put(AuthenticationExceptionKey, e)
            null // Return null to trigger the challenge
        }
    }

    challenge { scheme, _ ->
        val exception = call.attributes.getOrNull(AuthenticationExceptionKey)
        if (exception != null) throw exception

        call.respond(
            UnauthorizedResponse(
                HttpAuthHeader.Parameterized(
                    scheme,
                    mapOf(HttpAuthHeader.Parameters.Realm to realm)
                )
            )
        )
    }
}

suspend fun JWTCredential.validateUser(userDao: IUserDao): User? {
    val userUUID = validateCredentialsAndGetUUID() ?: return null
    val user = dbTransactionalQuery { userDao.getUser(userUUID) } ?: return null

    return when {
        else -> user
    }
}

suspend fun JWTCredential.validateUserIsAdmin(userDao: IUserDao): User? {
    val userUUID = validateCredentialsAndGetUUID() ?: return null

    val user = dbTransactionalQuery { userDao.getUser(userUUID) }

    // User is authenticated but is not an admin → return 403 Forbidden instead of 401
    if (user != null && !user.isAdmin) throw ErrorUnauthorized

    // User doesn't exist or other auth failure → return 401 Unauthorized
    if (user == null) return null

    return when {
        else -> user
    }
}

private fun JWTCredential.validateCredentialsAndGetUUID(): UUID? {
    if (!payload.audience.contains(JwtConfig.USERS_AUDIENCE)) return null

    val tokenType = payload.claims[JwtConfig.TOKEN_CLAIM_TOKEN_TYPE]?.asString() ?: return null
    if (tokenType != TokenType.Access.name) return null // Only validate access tokens

    val userId = payload.claims[JwtConfig.TOKEN_CLAIM_USER_ID_KEY]?.asString() ?: return null

    return try {
        UUID.fromString(userId)
    } catch (_: Exception) {
        null
    }
}
