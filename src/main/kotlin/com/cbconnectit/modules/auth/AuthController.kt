package com.cbconnectit.modules.auth

import com.cbconnectit.data.dto.requests.CreateTokenDto
import com.cbconnectit.data.dto.requests.RefreshTokenDto
import com.cbconnectit.data.dto.requests.hasData
import com.cbconnectit.data.dto.responses.AuthStatusResponse
import com.cbconnectit.data.dto.responses.CredentialsResponse
import com.cbconnectit.domain.interfaces.IUserDao
import com.cbconnectit.plugins.dbTransactionalQuery
import com.cbconnectit.plugins.statuspages.ErrorInvalidCredentials
import com.cbconnectit.plugins.statuspages.ErrorInvalidParameters
import com.cbconnectit.plugins.statuspages.ErrorInvalidToken
import com.cbconnectit.utils.PasswordManagerContract
import java.util.*

class AuthControllerImpl(
    private val userDao: IUserDao,
    private val tokenProvider: TokenProvider,
    private val passwordManager: PasswordManagerContract
) : AuthController {

    override suspend fun authorizeUser(tokenDto: CreateTokenDto): CredentialsResponse {
        if (!tokenDto.hasData()) throw ErrorInvalidParameters

        return dbTransactionalQuery {
            val userHashable = userDao.getUserHashableByUsername(tokenDto.username) ?: throw ErrorInvalidCredentials

            val isValidPassword = passwordManager.validatePassword(tokenDto.password, userHashable.password ?: "")
            if (!isValidPassword) throw ErrorInvalidCredentials

            tokenProvider.createTokens(userHashable.copy(password = null))
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
            val user = userDao.getUser(userId) ?: throw ErrorInvalidToken

            tokenProvider.createTokens(user.copy(password = null))
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

    private suspend fun validateRefreshToken(token: String): AuthStatusResponse {
        return try {
            val decoded = tokenProvider.verifier.verify(token)

            if (!decoded.audience.contains(JwtConfig.USERS_AUDIENCE)) {
                return AuthStatusResponse(authenticated = false)
            }

            if (decoded.claims[JwtConfig.TOKEN_CLAIM_TOKEN_TYPE]?.asString() != TokenType.Refresh.name) {
                return AuthStatusResponse(authenticated = false)
            }

            val userId = decoded.claims[JwtConfig.TOKEN_CLAIM_USER_ID_KEY]?.asString()
                ?: return AuthStatusResponse(authenticated = false)
            val normalizedUserId = try {
                UUID.fromString(userId).toString()
            } catch (_: Exception) {
                return AuthStatusResponse(authenticated = false)
            }

            val username = decoded.claims[JwtConfig.TOKEN_CLAIM_USER_NAME]?.asString()
            val role = decoded.claims[JwtConfig.TOKEN_CLAIM_USER_ROLE_KEY]?.asString()

            AuthStatusResponse(
                authenticated = true,
                role = role,
                userId = normalizedUserId,
                username = username
            )
        } catch (_: Exception) {
            AuthStatusResponse(authenticated = false)
        }
    }

    override suspend fun logout(refreshToken: String?) {
        // Stateless refresh token flow: client clears local token/cookies on logout.
        return
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
