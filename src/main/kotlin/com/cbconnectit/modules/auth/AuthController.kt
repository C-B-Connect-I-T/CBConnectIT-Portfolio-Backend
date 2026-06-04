package com.cbconnectit.modules.auth

import com.cbconnectit.data.dto.requests.CreateTokenDto
import com.cbconnectit.data.dto.requests.RefreshTokenDto
import com.cbconnectit.data.dto.requests.hasData
import com.cbconnectit.data.dto.responses.AuthStatusResponse
import com.cbconnectit.data.dto.responses.CredentialsResponse
import com.cbconnectit.domain.interfaces.IRefreshTokenDao
import com.cbconnectit.domain.interfaces.IUserDao
import com.cbconnectit.plugins.dbTransactionalQuery
import com.cbconnectit.plugins.statuspages.ErrorInvalidCredentials
import com.cbconnectit.plugins.statuspages.ErrorInvalidParameters
import com.cbconnectit.plugins.statuspages.ErrorInvalidToken
import com.cbconnectit.utils.PasswordManagerContract
import java.time.LocalDateTime
import java.util.*

private const val REFRESH_TOKEN_VALIDITY_DAYS = 30L

class AuthControllerImpl(
    private val userDao: IUserDao,
    private val refreshTokenDao: IRefreshTokenDao,
    private val tokenProvider: TokenProvider,
    private val passwordManager: PasswordManagerContract
) : AuthController {

    override suspend fun authorizeUser(tokenDto: CreateTokenDto): CredentialsResponse {
        if (!tokenDto.hasData()) throw ErrorInvalidParameters

        return dbTransactionalQuery {
            val userHashable = userDao.getUserHashableByUsername(tokenDto.username) ?: throw ErrorInvalidCredentials

            val isValidPassword = passwordManager.validatePassword(tokenDto.password, userHashable.password ?: "")
            if (!isValidPassword) throw ErrorInvalidCredentials

            val tokens = tokenProvider.createTokens(userHashable.copy(password = null))

            // Store refresh token in database
            val expiresAt = LocalDateTime.now().plusDays(REFRESH_TOKEN_VALIDITY_DAYS)
            refreshTokenDao.saveRefreshToken(userHashable.id, tokens.refreshToken, expiresAt)

            tokens
        }
    }

    override suspend fun refreshTokens(refreshDto: RefreshTokenDto): CredentialsResponse {
        if (!refreshDto.hasData()) throw ErrorInvalidToken

        val decoded = try {
            tokenProvider.verifier.verify(refreshDto.refreshToken)
        } catch (_: Exception) {
            throw ErrorInvalidToken
        }

        // Validate audience and token type
        if (!decoded.audience.contains(JwtConfig.USERS_AUDIENCE)) throw ErrorInvalidToken

        val tokenType = decoded.claims[JwtConfig.TOKEN_CLAIM_TOKEN_TYPE]?.asString()
        if (tokenType != TokenType.Refresh.name) throw ErrorInvalidToken

        val userId = UUID.fromString(
            decoded.claims[JwtConfig.TOKEN_CLAIM_USER_ID_KEY]?.asString() ?: throw ErrorInvalidToken
        )

        return dbTransactionalQuery {
            // Check if this token was already used and replaced within grace period
            val replacementTokenHash = refreshTokenDao.getReplacementToken(userId, refreshDto.refreshToken)
            if (replacementTokenHash != null) {
                // Token was already rotated within grace period - find and return the same new tokens
                // This handles concurrent requests gracefully
                val user = userDao.getUser(userId) ?: throw ErrorInvalidToken

                // We need to retrieve the already-generated tokens
                // Since we can't retrieve the plain token from hash, we generate new ones
                // but this is acceptable for the grace period edge case
                val newTokens = tokenProvider.createTokens(user.copy(password = null))

                // Store new refresh token in database
                val expiresAt = LocalDateTime.now().plusDays(REFRESH_TOKEN_VALIDITY_DAYS)
                refreshTokenDao.saveRefreshToken(user.id, newTokens.refreshToken, expiresAt)

                return@dbTransactionalQuery newTokens
            }

            // Validate token hasn't been invalidated in database
            if (!refreshTokenDao.isRefreshTokenValid(userId, refreshDto.refreshToken)) throw ErrorInvalidToken

            val user = userDao.getUser(userId) ?: throw ErrorInvalidToken

            // Generate new tokens
            val newTokens = tokenProvider.createTokens(user.copy(password = null))

            // Mark old refresh token as replaced (token rotation with grace period)
            refreshTokenDao.replaceRefreshToken(userId, refreshDto.refreshToken, newTokens.refreshToken)

            // Store new refresh token in database
            val expiresAt = LocalDateTime.now().plusDays(REFRESH_TOKEN_VALIDITY_DAYS)
            refreshTokenDao.saveRefreshToken(user.id, newTokens.refreshToken, expiresAt)

            newTokens
        }
    }

//    override suspend fun forgotPassword(forgotPasswordDto: ForgotPasswordDto) {
//        if (!forgotPasswordDto.hasData()) throw ErrorInvalidParameters
//
//        return dbTransactionalQuery {
//            // Always return success even if user doesn't exist (security best practice)
//            val user = userDao.getUserHashableByEmail(forgotPasswordDto.email ?: "") ?: return@dbTransactionalQuery
//
//            // Check rate limiting
//            user.passwordResetTokenSentAt?.let { lastSent ->
//                val minutesSinceLastRequest = Duration.between(lastSent, LocalDateTime.now()).toMinutes()
//                if (minutesSinceLastRequest < PASSWORD_RESET_RESEND_COOLDOWN_MINUTES) throw ErrorPasswordResetRateLimitExceeded
//            }
//
//            val resetToken = VerificationUtils.generateVerificationCode()
//
//            userDao.setPasswordResetToken(user.id, resetToken, LocalDateTime.now())
//
//            // Send email
//            emailService.sendPasswordResetMail(user.email, resetToken)
//        }
//    }
//
//    override suspend fun resetPassword(resetPasswordDto: ResetPasswordDto) {
//        if (!resetPasswordDto.isValid) throw ErrorInvalidParameters
//
//        if (!resetPasswordDto.isPasswordStrong) throw ErrorWeakPassword
//        if (!resetPasswordDto.isPasswordSame) throw ErrorPasswordsDoNotMatch
//
//        return dbTransactionalQuery {
//            val user = userDao.getUserByPasswordResetToken(resetPasswordDto.token ?: "") ?: throw ErrorInvalidPasswordResetToken
//
//            // Check if token has expired
//            user.passwordResetTokenSentAt?.let { sentAt ->
//                val hoursSinceRequest = Duration.between(sentAt, LocalDateTime.now()).toHours()
//                if (hoursSinceRequest >= PASSWORD_RESET_VALID_HOURS) throw ErrorPasswordResetTokenExpired
//            } ?: throw ErrorInvalidPasswordResetToken
//
//            if (passwordManager.validatePassword(resetPasswordDto.password, user.password ?: "")) throw ErrorSameAsOldPassword
//
//            // Hash new password
//            val hashedPassword = passwordManager.encryptPassword(resetPasswordDto.password)
//            // Update password and clear reset token
//            val success = userDao.resetPassword(user.id, hashedPassword)
//            if (!success) throw ErrorInvalidPasswordResetToken
//        }
//    }

    override suspend fun getAuthStatus(accessToken: String?, refreshToken: String?): AuthStatusResponse {
        // Try access token first (fast path)
        accessToken?.let {
            validateAccessToken(it)?.let { response -> return response }
        }

        // Fallback to refresh token
        refreshToken?.let {
            return validateRefreshToken(it)
        }

        return AuthStatusResponse(authenticated = false)
    }

    private fun validateAccessToken(token: String): AuthStatusResponse? {
        return try {
            val decoded = tokenProvider.verifier.verify(token)

            if (!decoded.audience.contains(JwtConfig.USERS_AUDIENCE)) return null
            if (decoded.claims[JwtConfig.TOKEN_CLAIM_TOKEN_TYPE]?.asString() != TokenType.Access.name) return null

            val userId = decoded.claims[JwtConfig.TOKEN_CLAIM_USER_ID_KEY]?.asString() ?: return null
            val username = decoded.claims[JwtConfig.TOKEN_CLAIM_USER_NAME]?.asString()
            val role = decoded.claims[JwtConfig.TOKEN_CLAIM_USER_ROLE_KEY]?.asString()

            AuthStatusResponse(
                authenticated = true,
                role = role,
                userId = userId,
                username = username
            )
        } catch (_: Exception) {
            null
        }
    }

    private suspend fun validateRefreshToken(token: String): AuthStatusResponse = dbTransactionalQuery {
        try {
            val decoded = tokenProvider.verifier.verify(token)

            if (!decoded.audience.contains(JwtConfig.USERS_AUDIENCE)) {
                return@dbTransactionalQuery AuthStatusResponse(authenticated = false)
            }

            if (decoded.claims[JwtConfig.TOKEN_CLAIM_TOKEN_TYPE]?.asString() != TokenType.Refresh.name) {
                return@dbTransactionalQuery AuthStatusResponse(authenticated = false)
            }

            val userIdString = decoded.claims[JwtConfig.TOKEN_CLAIM_USER_ID_KEY]?.asString()
                ?: return@dbTransactionalQuery AuthStatusResponse(authenticated = false)

            val userId = UUID.fromString(userIdString)

            if (!refreshTokenDao.isRefreshTokenValid(userId, token)) {
                return@dbTransactionalQuery AuthStatusResponse(authenticated = false)
            }

            val username = decoded.claims[JwtConfig.TOKEN_CLAIM_USER_NAME]?.asString()
            val role = decoded.claims[JwtConfig.TOKEN_CLAIM_USER_ROLE_KEY]?.asString()

            AuthStatusResponse(
                authenticated = true,
                role = role,
                userId = userId.toString(),
                username = username
            )
        } catch (_: Exception) {
            AuthStatusResponse(authenticated = false)
        }
    }

    override suspend fun logout(refreshToken: String?) {
        // If no refresh token provided, just return success (idempotent)
        refreshToken ?: return

        dbTransactionalQuery {
            try {
                // Extract userId from refresh token
                val decoded = tokenProvider.verifier.verify(refreshToken)
                val userIdString = decoded.claims[JwtConfig.TOKEN_CLAIM_USER_ID_KEY]?.asString()
                    ?: return@dbTransactionalQuery

                val userId = UUID.fromString(userIdString)
                refreshTokenDao.invalidateRefreshToken(userId, refreshToken)
            } catch (_: Exception) {
                // Token invalid/expired - that's fine, logout should always succeed
                return@dbTransactionalQuery
            }
        }
    }
}

interface AuthController {
    suspend fun authorizeUser(tokenDto: CreateTokenDto): CredentialsResponse
    suspend fun refreshTokens(refreshDto: RefreshTokenDto): CredentialsResponse

    //    suspend fun forgotPassword(forgotPasswordDto: ForgotPasswordDto)
    //    suspend fun resetPassword(resetPasswordDto: ResetPasswordDto)
    suspend fun getAuthStatus(accessToken: String?, refreshToken: String?): AuthStatusResponse
    suspend fun logout(refreshToken: String?)
}
