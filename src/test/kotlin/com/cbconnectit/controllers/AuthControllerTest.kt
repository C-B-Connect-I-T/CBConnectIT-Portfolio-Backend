package com.cbconnectit.controllers

import com.auth0.jwt.exceptions.JWTVerificationException
import com.auth0.jwt.interfaces.DecodedJWT
import com.cbconnectit.data.dto.requests.RefreshTokenDto
import com.cbconnectit.data.dto.responses.CredentialsResponse
import com.cbconnectit.domain.interfaces.IRefreshTokenDao
import com.cbconnectit.domain.interfaces.IUserDao
import com.cbconnectit.domain.models.user.User
import com.cbconnectit.instrumentation.AuthInstrumentation
import com.cbconnectit.modules.auth.AuthController
import com.cbconnectit.modules.auth.AuthControllerImpl
import com.cbconnectit.modules.auth.JwtConfig
import com.cbconnectit.modules.auth.TokenProvider
import com.cbconnectit.modules.auth.TokenType
import com.cbconnectit.plugins.statuspages.ErrorInvalidCredentials
import com.cbconnectit.plugins.statuspages.ErrorInvalidParameters
import com.cbconnectit.plugins.statuspages.ErrorInvalidToken
import com.cbconnectit.utils.PasswordManagerContract
import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.util.*
import kotlin.test.assertFailsWith

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AuthControllerTest : BaseControllerTest() {

    private val userDao: IUserDao = mockk()
    private val refreshTokenDao: IRefreshTokenDao = mockk()
    private val tokenProvider: TokenProvider = mockk()
    private val passwordEncryption: PasswordManagerContract = mockk()
    private val decodedJwt: DecodedJWT = mockk()

    private val controller: AuthController by lazy { AuthControllerImpl(userDao, refreshTokenDao, tokenProvider, passwordEncryption) }

    @BeforeEach
    fun setupBeforeEach() {
        clearMocks(userDao, refreshTokenDao, tokenProvider, passwordEncryption)
    }

    // <editor-fold desc="authorizeUser">
    @Test
    fun `when authorizing user with invalid data, we throw exception`() = runTest {
        coEvery { userDao.getUserHashableByUsername(any()) } returns null

        assertFailsWith<ErrorInvalidParameters> {
            controller.authorizeUser(AuthInstrumentation.givenAnInvalidCreateToken())
        }
    }

    @Test
    fun `when authorizing user with no password, we throw exception`() = runTest {
        coEvery { userDao.getUserHashableByUsername(any()) } returns null

        assertFailsWith<ErrorInvalidParameters> {
            controller.authorizeUser(AuthInstrumentation.givenAnInvalidCreateTokenWithoutPassword())
        }
    }

    @Test
    fun `when authorizing user with a valid email as a username which does not exist, we throw exception`() = runTest {
        coEvery { userDao.getUserHashableByUsername(any()) } returns null

        assertFailsWith<ErrorInvalidCredentials> {
            controller.authorizeUser(AuthInstrumentation.givenAValidEmailCreateToken())
        }
    }

    @Test
    fun `when authorizing user with a valid email as a username and password is not correct, we throw exception`() = runTest {
        coEvery { userDao.getUserHashableByUsername(any()) } returns User()
        coEvery { passwordEncryption.validatePassword(any(), any()) } returns false

        assertFailsWith<ErrorInvalidCredentials> {
            controller.authorizeUser(AuthInstrumentation.givenAValidEmailCreateToken())
        }
    }

    @Test
    fun `when authorizing user with a valid email as a username when everything is valid, we return an accessToken`() = runTest {
        val createdToken = CredentialsResponse("", "", 0)

        coEvery { userDao.getUserHashableByUsername(any()) } returns User()
        coEvery { passwordEncryption.validatePassword(any(), any()) } returns true
        coEvery { tokenProvider.createTokens(any()) } returns createdToken
        coEvery { refreshTokenDao.saveRefreshToken(any(), any(), any()) } returns Unit

        val response = controller.authorizeUser(AuthInstrumentation.givenAValidEmailCreateToken())
        assertThat(response).isEqualTo(createdToken)
    }
    // </editor-fold>

    // <editor-fold desc="Refresh">
    @Test
    fun `when refreshing tokens with invalid data, we throw exception`() = runTest {
        assertFailsWith<ErrorInvalidToken> {
            controller.refreshTokens(RefreshTokenDto(""))
        }
    }

    @Test
    fun `when refreshing tokens with valid data, but verify throws error, we throw exception`() = runTest {
        every { tokenProvider.verifier.verify(any<String>()) } throws JWTVerificationException("Invalid token")
        every { decodedJwt.audience.contains(JwtConfig.USERS_AUDIENCE) } returns false

        assertFailsWith<ErrorInvalidToken> {
            controller.refreshTokens(RefreshTokenDto("some_random_token"))
        }
    }

    @Test
    fun `when refreshing tokens with valid data, but without USERS_AUDIENCE, we throw exception`() = runTest {
        every { tokenProvider.verifier.verify(any<String>()) } returns decodedJwt
        every { decodedJwt.audience.contains(JwtConfig.USERS_AUDIENCE) } returns false

        assertFailsWith<ErrorInvalidToken> {
            controller.refreshTokens(RefreshTokenDto("some_random_token"))
        }
    }

    @Test
    fun `when refreshing tokens with valid data, but without Refresh as TokenType, we throw exception`() = runTest {
        every { tokenProvider.verifier.verify(any<String>()) } returns decodedJwt
        every { decodedJwt.audience.contains(JwtConfig.USERS_AUDIENCE) } returns true
        every { decodedJwt.claims[JwtConfig.TOKEN_CLAIM_TOKEN_TYPE]?.asString() } returns TokenType.Access.name

        assertFailsWith<ErrorInvalidToken> {
            controller.refreshTokens(RefreshTokenDto("some_random_token"))
        }
    }

    @Test
    fun `when refreshing tokens with valid data, but without userId, we throw exception`() = runTest {
        every { tokenProvider.verifier.verify(any<String>()) } returns decodedJwt
        every { decodedJwt.audience.contains(JwtConfig.USERS_AUDIENCE) } returns true
        every { decodedJwt.claims[JwtConfig.TOKEN_CLAIM_TOKEN_TYPE]?.asString() } returns TokenType.Refresh.name
        every { decodedJwt.claims[JwtConfig.TOKEN_CLAIM_USER_ID_KEY]?.asString() } returns null

        assertFailsWith<ErrorInvalidToken> {
            controller.refreshTokens(RefreshTokenDto("some_random_token"))
        }
    }

    @Test
    fun `when refreshing tokens with valid data, but userId does not exist, we throw exception`() = runTest {
        val userId = UUID.randomUUID()
        every { tokenProvider.verifier.verify(any<String>()) } returns decodedJwt
        every { decodedJwt.audience.contains(JwtConfig.USERS_AUDIENCE) } returns true
        every { decodedJwt.claims[JwtConfig.TOKEN_CLAIM_TOKEN_TYPE]?.asString() } returns TokenType.Refresh.name
        every { decodedJwt.claims[JwtConfig.TOKEN_CLAIM_USER_ID_KEY]?.asString() } returns userId.toString()
        coEvery { refreshTokenDao.getReplacementToken(any(), any()) } returns null
        coEvery { refreshTokenDao.isRefreshTokenValid(any(), any()) } returns true
        coEvery { userDao.getUser(any()) } returns null

        assertFailsWith<ErrorInvalidToken> {
            controller.refreshTokens(RefreshTokenDto("some_random_token"))
        }
    }

    @Test
    fun `when refreshing tokens with valid data, we return tokens`() = runTest {
        val createdToken = CredentialsResponse("", "", 0)
        val userId = UUID.randomUUID()

        every { tokenProvider.verifier.verify(any<String>()) } returns decodedJwt
        every { decodedJwt.audience.contains(JwtConfig.USERS_AUDIENCE) } returns true
        every { decodedJwt.claims[JwtConfig.TOKEN_CLAIM_TOKEN_TYPE]?.asString() } returns TokenType.Refresh.name
        every { decodedJwt.claims[JwtConfig.TOKEN_CLAIM_USER_ID_KEY]?.asString() } returns userId.toString()
        coEvery { refreshTokenDao.getReplacementToken(any(), any()) } returns null
        coEvery { refreshTokenDao.isRefreshTokenValid(any(), any()) } returns true
        coEvery { refreshTokenDao.replaceRefreshToken(any(), any(), any()) } returns Unit
        coEvery { refreshTokenDao.saveRefreshToken(any(), any(), any()) } returns Unit
        coEvery { userDao.getUser(any()) } returns User()
        coEvery { tokenProvider.createTokens(any()) } returns createdToken

        val response = controller.refreshTokens(RefreshTokenDto("some_random_token"))
        assertThat(response).isEqualTo(createdToken)
    }

    @Test
    fun `when refreshing tokens with already replaced token within grace period, we return new tokens`() = runTest {
        val createdToken = CredentialsResponse("new_access", "new_refresh", 3600)
        val userId = UUID.randomUUID()

        every { tokenProvider.verifier.verify(any<String>()) } returns decodedJwt
        every { decodedJwt.audience.contains(JwtConfig.USERS_AUDIENCE) } returns true
        every { decodedJwt.claims[JwtConfig.TOKEN_CLAIM_TOKEN_TYPE]?.asString() } returns TokenType.Refresh.name
        every { decodedJwt.claims[JwtConfig.TOKEN_CLAIM_USER_ID_KEY]?.asString() } returns userId.toString()
        coEvery { refreshTokenDao.getReplacementToken(any(), any()) } returns "hashed_replacement_token"
        coEvery { userDao.getUser(userId) } returns User(id = userId)
        coEvery { tokenProvider.createTokens(any()) } returns createdToken
        coEvery { refreshTokenDao.saveRefreshToken(any(), any(), any()) } returns Unit

        val response = controller.refreshTokens(RefreshTokenDto("some_random_token"))
        assertThat(response).isEqualTo(createdToken)

        // Verify that we didn't call isRefreshTokenValid or replaceRefreshToken
        // because we detected the token was already replaced
        coVerify(exactly = 0) { refreshTokenDao.isRefreshTokenValid(any(), any()) }
        coVerify(exactly = 0) { refreshTokenDao.replaceRefreshToken(any(), any(), any()) }
    }

    @Test
    fun `concurrent refresh requests within grace period both succeed`() = runTest {
        val userId = UUID.randomUUID()
        val user = User(id = userId, username = "test@example.com")
        val originalRefreshToken = "original-refresh-token"
        val createdToken1 = CredentialsResponse("access1", "refresh1", 3600)
        val createdToken2 = CredentialsResponse("access2", "refresh2", 3600)

        // Mock token validation
        every { tokenProvider.verifier.verify(originalRefreshToken) } returns decodedJwt
        every { decodedJwt.audience.contains(JwtConfig.USERS_AUDIENCE) } returns true
        every { decodedJwt.claims[JwtConfig.TOKEN_CLAIM_TOKEN_TYPE]?.asString() } returns TokenType.Refresh.name
        every { decodedJwt.claims[JwtConfig.TOKEN_CLAIM_USER_ID_KEY]?.asString() } returns userId.toString()
        coEvery { userDao.getUser(userId) } returns user

        // Simulate race condition:
        // First call - no replacement yet
        coEvery { refreshTokenDao.getReplacementToken(any(), originalRefreshToken) } returns null andThen "hashed_replacement"
        coEvery { refreshTokenDao.isRefreshTokenValid(any(), originalRefreshToken) } returns true
        coEvery { refreshTokenDao.replaceRefreshToken(any(), any(), any()) } returns Unit
        coEvery { refreshTokenDao.saveRefreshToken(any(), any(), any()) } returns Unit
        coEvery { tokenProvider.createTokens(any()) } returns createdToken1 andThen createdToken2

        // Make two concurrent refresh requests
        val response1 = controller.refreshTokens(RefreshTokenDto(originalRefreshToken))
        val response2 = controller.refreshTokens(RefreshTokenDto(originalRefreshToken))

        // Both should succeed
        assertThat(response1).isNotNull
        assertThat(response2).isNotNull

        // Verify first request followed normal flow
        coVerify(exactly = 1) { refreshTokenDao.replaceRefreshToken(any(), originalRefreshToken, any()) }

        // Verify second request detected replacement (called getReplacementToken twice)
        coVerify(exactly = 2) { refreshTokenDao.getReplacementToken(any(), originalRefreshToken) }

        // Both should have generated new tokens
        coVerify(exactly = 2) { tokenProvider.createTokens(any()) }
        coVerify(exactly = 2) { refreshTokenDao.saveRefreshToken(any(), any(), any()) }
    }

    // </editor-fold>

    // <editor-fold desc="GetAuthStatus">
    @Test
    fun `when getting auth status with null user, we return unauthenticated status`() = runTest {
        val status = controller.getAuthStatus(accessToken = null, refreshToken = null)

        assertThat(status.authenticated).isFalse()
        assertThat(status.role).isNull()
        assertThat(status.userId).isNull()
        assertThat(status.username).isNull()
    }

    @Test
    fun `when getting auth status with valid user, we return authenticated status`() = runTest {
        val userId = UUID.randomUUID()
        val accessToken = "valid-access-token"

        // Mock JWT verification for access token
        val userIdClaim = mockk<com.auth0.jwt.interfaces.Claim>()
        val tokenTypeClaim = mockk<com.auth0.jwt.interfaces.Claim>()
        val roleClaim = mockk<com.auth0.jwt.interfaces.Claim>()
        val emailClaim = mockk<com.auth0.jwt.interfaces.Claim>()

        every { userIdClaim.asString() } returns userId.toString()
        every { tokenTypeClaim.asString() } returns TokenType.Access.name
        every { roleClaim.asString() } returns "user"
        every { emailClaim.asString() } returns "test@example.com"

        every { decodedJwt.audience } returns listOf(JwtConfig.USERS_AUDIENCE)
        every { decodedJwt.claims } returns mapOf(
            JwtConfig.TOKEN_CLAIM_USER_ID_KEY to userIdClaim,
            JwtConfig.TOKEN_CLAIM_TOKEN_TYPE to tokenTypeClaim,
            JwtConfig.TOKEN_CLAIM_USER_ROLE_KEY to roleClaim,
            JwtConfig.TOKEN_CLAIM_USER_NAME to emailClaim
        )

        every { tokenProvider.verifier.verify(accessToken) } returns decodedJwt

        val status = controller.getAuthStatus(accessToken, refreshToken = null)

        assertThat(status.authenticated).isTrue()
        assertThat(status.role).isEqualTo("user")
        assertThat(status.userId).isEqualTo(userId.toString())
        assertThat(status.username).isEqualTo("test@example.com")
    }

    @Test
    fun `when getting auth status with admin user, we return admin role`() = runTest {
        val userId = UUID.randomUUID()
        val accessToken = "valid-access-token"

        // Mock JWT verification for access token
        val userIdClaim = mockk<com.auth0.jwt.interfaces.Claim>()
        val tokenTypeClaim = mockk<com.auth0.jwt.interfaces.Claim>()
        val roleClaim = mockk<com.auth0.jwt.interfaces.Claim>()
        val emailClaim = mockk<com.auth0.jwt.interfaces.Claim>()

        every { userIdClaim.asString() } returns userId.toString()
        every { tokenTypeClaim.asString() } returns TokenType.Access.name
        every { roleClaim.asString() } returns "admin"
        every { emailClaim.asString() } returns "admin@example.com"

        every { decodedJwt.audience } returns listOf(JwtConfig.USERS_AUDIENCE)
        every { decodedJwt.claims } returns mapOf(
            JwtConfig.TOKEN_CLAIM_USER_ID_KEY to userIdClaim,
            JwtConfig.TOKEN_CLAIM_TOKEN_TYPE to tokenTypeClaim,
            JwtConfig.TOKEN_CLAIM_USER_ROLE_KEY to roleClaim,
            JwtConfig.TOKEN_CLAIM_USER_NAME to emailClaim
        )

        every { tokenProvider.verifier.verify(accessToken) } returns decodedJwt

        val status = controller.getAuthStatus(accessToken, refreshToken = null)

        assertThat(status.authenticated).isTrue()
        assertThat(status.role).isEqualTo("admin")
        assertThat(status.userId).isEqualTo(userId.toString())
        assertThat(status.username).isEqualTo("admin@example.com")
    }

    @Test
    fun `when getting auth status with expired access token but valid refresh token, we return authenticated status`() = runTest {
        val userId = UUID.randomUUID()
        val accessToken = "expired-access-token"
        val refreshToken = "valid-refresh-token"

        // Mock JWT verification - access token throws exception (expired)
        every { tokenProvider.verifier.verify(accessToken) } throws com.auth0.jwt.exceptions.TokenExpiredException("Token expired", null)

        // Mock JWT verification for refresh token
        val userIdClaim = mockk<com.auth0.jwt.interfaces.Claim>()
        val tokenTypeClaim = mockk<com.auth0.jwt.interfaces.Claim>()
        val roleClaim = mockk<com.auth0.jwt.interfaces.Claim>()
        val emailClaim = mockk<com.auth0.jwt.interfaces.Claim>()

        every { userIdClaim.asString() } returns userId.toString()
        every { tokenTypeClaim.asString() } returns TokenType.Refresh.name
        every { roleClaim.asString() } returns "user"
        every { emailClaim.asString() } returns "test@example.com"

        every { decodedJwt.audience } returns listOf(JwtConfig.USERS_AUDIENCE)
        every { decodedJwt.claims } returns mapOf(
            JwtConfig.TOKEN_CLAIM_USER_ID_KEY to userIdClaim,
            JwtConfig.TOKEN_CLAIM_TOKEN_TYPE to tokenTypeClaim,
            JwtConfig.TOKEN_CLAIM_USER_ROLE_KEY to roleClaim,
            JwtConfig.TOKEN_CLAIM_USER_NAME to emailClaim
        )

        every { tokenProvider.verifier.verify(refreshToken) } returns decodedJwt
        every { refreshTokenDao.isRefreshTokenValid(userId, refreshToken) } returns true

        val status = controller.getAuthStatus(accessToken, refreshToken)

        assertThat(status.authenticated).isTrue()
        assertThat(status.role).isEqualTo("user")
        assertThat(status.userId).isEqualTo(userId.toString())
        assertThat(status.username).isEqualTo("test@example.com")
    }

    @Test
    fun `when getting auth status with both null user and null refresh token, we return unauthenticated`() = runTest {
        val status = controller.getAuthStatus(accessToken = null, refreshToken = null)

        assertThat(status.authenticated).isFalse()
        assertThat(status.role).isNull()
        assertThat(status.userId).isNull()
        assertThat(status.username).isNull()
    }

    @Test
    fun `when getting auth status with invalidated refresh token, we return unauthenticated`() = runTest {
        val userId = UUID.randomUUID()
        val accessToken = "expired-access-token"
        val refreshToken = "invalidated-refresh-token"

        // Mock JWT verification - access token throws exception (expired)
        every { tokenProvider.verifier.verify(accessToken) } throws com.auth0.jwt.exceptions.TokenExpiredException("Token expired", null)

        // Mock JWT verification for refresh token
        val userIdClaim = mockk<com.auth0.jwt.interfaces.Claim>()
        val tokenTypeClaim = mockk<com.auth0.jwt.interfaces.Claim>()

        every { userIdClaim.asString() } returns userId.toString()
        every { tokenTypeClaim.asString() } returns TokenType.Refresh.name

        every { decodedJwt.audience } returns listOf(JwtConfig.USERS_AUDIENCE)
        every { decodedJwt.claims } returns mapOf(
            JwtConfig.TOKEN_CLAIM_USER_ID_KEY to userIdClaim,
            JwtConfig.TOKEN_CLAIM_TOKEN_TYPE to tokenTypeClaim
        )

        every { tokenProvider.verifier.verify(refreshToken) } returns decodedJwt
        every { refreshTokenDao.isRefreshTokenValid(userId, refreshToken) } returns false

        val status = controller.getAuthStatus(accessToken, refreshToken)

        assertThat(status.authenticated).isFalse()
    }
    // </editor-fold>

    // <editor-fold desc="Logout">
    @Test
    fun `when logging out with null refresh token, it completes successfully`() = runTest {
        // Logout should always succeed, even with null token (idempotent)
        controller.logout(null)

        // No exception should be thrown
        coVerify(exactly = 0) { refreshTokenDao.invalidateRefreshToken(any(), any()) }
    }

    @Test
    fun `when logging out with valid refresh token, it invalidates the token`() = runTest {
        val userId = UUID.randomUUID()
        val refreshToken = "valid-refresh-token"

        // Mock JWT verification for refresh token
        val userIdClaim = mockk<com.auth0.jwt.interfaces.Claim>()
        every { userIdClaim.asString() } returns userId.toString()

        every { decodedJwt.claims } returns mapOf(
            JwtConfig.TOKEN_CLAIM_USER_ID_KEY to userIdClaim
        )

        every { tokenProvider.verifier.verify(refreshToken) } returns decodedJwt
        coEvery { refreshTokenDao.invalidateRefreshToken(userId, refreshToken) } returns Unit

        controller.logout(refreshToken)

        coVerify { refreshTokenDao.invalidateRefreshToken(userId, refreshToken) }
    }

    @Test
    fun `when logging out with invalid refresh token, it handles gracefully`() = runTest {
        val refreshToken = "invalid-token"

        // Token verification fails
        every { tokenProvider.verifier.verify(refreshToken) } throws com.auth0.jwt.exceptions.TokenExpiredException("Token expired", null)

        // Should not throw - just handle it gracefully
        controller.logout(refreshToken)

        // Verify invalidateRefreshToken was NOT called since token was invalid
        coVerify(exactly = 0) { refreshTokenDao.invalidateRefreshToken(any(), any()) }
    }
    // </editor-fold>
}
