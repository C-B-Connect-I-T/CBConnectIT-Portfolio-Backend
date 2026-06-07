package com.cbconnectit.modules.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.cbconnectit.data.dto.responses.CredentialsResponse
import com.cbconnectit.domain.models.Environment
import com.cbconnectit.domain.models.user.User
import io.ktor.server.config.*
import java.util.*
import java.util.concurrent.TimeUnit

private const val VALID_MINUTES = 15L
private const val REFRESH_VALID_DAYS = 30L

@SuppressWarnings("MagicNumber")
class JwtConfig(
    private val issuer: String,
    private val audience: String,
    secret: String
) : TokenProvider {

    private val validityInMs: Long = TimeUnit.MINUTES.toMillis(VALID_MINUTES) // 15 minutes
    private val refreshValidityInMs: Long = TimeUnit.DAYS.toMillis(REFRESH_VALID_DAYS) // 30 days
    private val algorithm = Algorithm.HMAC256(secret)

    companion object {
        const val TOKEN_CLAIM_USER_NAME = "username"
        const val TOKEN_CLAIM_USER_ID_KEY = "userId"
        const val TOKEN_CLAIM_USER_ROLE_KEY = "role"
        const val USERS_AUDIENCE = "users"
        const val TOKEN_CLAIM_TOKEN_TYPE = "token_type"

        // Cookie names
        const val ACCESS_TOKEN_COOKIE = "access_token"
        const val REFRESH_TOKEN_COOKIE = "refresh_token"

        // Cookie max ages (in seconds)
        const val ACCESS_TOKEN_MAX_AGE = (VALID_MINUTES * 60).toInt()
        const val REFRESH_TOKEN_MAX_AGE = (REFRESH_VALID_DAYS * 24 * 60 * 60).toInt()
    }

    constructor(config: ApplicationConfig, secret: String) : this(
        config.property("jwt.issuer").getString(),
        config.property("jwt.audience").getString(),
        secret
    )

    constructor(issuer: String, audience: String, environment: Environment) : this(
        issuer, audience, environment.jwtSecret
    )

    override val verifier: JWTVerifier = JWT
        .require(algorithm)
        .withAudience(audience)
        .withIssuer(issuer)
        .withClaimPresence(TOKEN_CLAIM_USER_ID_KEY)
        .build()

    private fun createToken(user: User, expiration: Date, tokenType: TokenType): String {
        val tokenId = UUID.randomUUID().toString()

        return JWT.create()
            .withAudience(audience)
            .withIssuer(issuer)
            .withJWTId(tokenId)
            .withClaim(TOKEN_CLAIM_USER_ID_KEY, user.id.toString())
            .withClaim(TOKEN_CLAIM_USER_ROLE_KEY, user.role.toString().lowercase())
            .withClaim(TOKEN_CLAIM_USER_NAME, user.username)
            .withClaim(TOKEN_CLAIM_TOKEN_TYPE, tokenType.name)
            .withExpiresAt(expiration)
            .sign(algorithm)
    }

    override fun createTokens(user: User): CredentialsResponse = CredentialsResponse(
        createToken(user, getTokenExpiration(), TokenType.Access),
        createToken(user, getTokenExpiration(refreshValidityInMs), TokenType.Refresh),
        validityInMs
    )

    override fun verifyToken(token: String): String? {
        return verifier.verify(token).claims[TOKEN_CLAIM_USER_ID_KEY]?.asString()
    }

    /**
     * Calculate the expiration Date based on current time + the given validity
     */
    private fun getTokenExpiration(validity: Long = validityInMs) = Date(System.currentTimeMillis() + validity)
}

interface TokenProvider {
    fun createTokens(user: User): CredentialsResponse
    fun verifyToken(token: String): String?

    // This should not be here, another tokenProvider might not use any JWT...
    // Need to find another fix for this
    val verifier: JWTVerifier
}

enum class TokenType {
    Access,
    Refresh
}
