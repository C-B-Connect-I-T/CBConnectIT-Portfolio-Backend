package com.cbconnectit.routing

import com.cbconnectit.data.dto.requests.CreateTokenDto
import com.cbconnectit.data.dto.requests.RefreshTokenDto
import com.cbconnectit.data.dto.responses.AuthStatusResponse
import com.cbconnectit.data.dto.responses.CredentialsResponse
import com.cbconnectit.data.database.tables.Constants
import com.cbconnectit.modules.auth.AuthController
import com.cbconnectit.modules.auth.authRouting
import com.cbconnectit.plugins.statuspages.ErrorInvalidToken
import com.cbconnectit.plugins.statuspages.ErrorMissingBody
import com.cbconnectit.plugins.statuspages.ErrorResponse
import com.cbconnectit.plugins.statuspages.toErrorResponse
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.mockk
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.koin.dsl.module

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AuthRoutingTest : BaseRoutingTest() {

    private val authController: AuthController = mockk()

    @BeforeAll
    fun setup() {
        koinModules = module {
            single { authController }
        }

        moduleList = {
            routing { authRouting(authController) }
        }
    }

    @BeforeEach
    fun clearMocks() {
        clearMocks(authController)
    }

    private fun withBaseAuthTestApplication(
        block: suspend ApplicationTestBuilder.() -> Unit
    ) = withBaseTestApplication(
        AuthenticationInstrumentation(),
    ) { block() }

    // <editor-fold desc="login">
    @Test
    fun `when logging in without a body, we return ErrorMissingBody`() = withBaseAuthTestApplication {
        val response = doCall(HttpMethod.Post, "/api/oauth/token")

        Assertions.assertThat(response.status).isEqualTo(ErrorMissingBody.statusCode)
        Assertions.assertThat(response.parseBody<ErrorResponse>()).isEqualTo(ErrorMissingBody.toErrorResponse())
    }

    @Test
    fun `when logging in with invalid body, we return ErrorMissingBody`() = withBaseAuthTestApplication {
        val body = toJsonBody(mapOf("key_1" to "unknown"))
        val response = doCall(HttpMethod.Post, "/api/oauth/token", body)

        Assertions.assertThat(response.status).isEqualTo(ErrorMissingBody.statusCode)
        Assertions.assertThat(response.parseBody<ErrorResponse>()).isEqualTo(ErrorMissingBody.toErrorResponse())
    }

    @Test
    fun `when logging in with body, we return tokens`() = withBaseAuthTestApplication {
        val authResponse = CredentialsResponse("", "", 0)
        coEvery { authController.authorizeUser(any()) } returns authResponse

        val body = toJsonBody(CreateTokenDto("", ""))
        val response = doCall(HttpMethod.Post, "/api/oauth/token", body, authorized = false, clientType = null)

        Assertions.assertThat(response.status).isEqualTo(HttpStatusCode.OK)
        Assertions.assertThat(response.parseBody<CredentialsResponse>()).isEqualTo(authResponse)
    }
    // </editor-fold>

    // <editor-fold desc="Refresh">
    @Test
    fun `when refreshing without a body, we return ErrorInvalidToken`() = withBaseAuthTestApplication {
        val response = doCall(HttpMethod.Post, "/api/oauth/refresh")

        Assertions.assertThat(response.status).isEqualTo(ErrorInvalidToken.statusCode)
        Assertions.assertThat(response.parseBody<ErrorResponse>()).isEqualTo(ErrorInvalidToken.toErrorResponse())
    }

    @Test
    fun `when refreshing with invalid body, we return ErrorInvalidToken`() = withBaseAuthTestApplication {
        val body = toJsonBody(mapOf("key_1" to "unknown"))
        val response = doCall(HttpMethod.Post, "/api/oauth/refresh", body)

        Assertions.assertThat(response.status).isEqualTo(ErrorInvalidToken.statusCode)
        Assertions.assertThat(response.parseBody<ErrorResponse>()).isEqualTo(ErrorInvalidToken.toErrorResponse())
    }

    @Test
    fun `when refreshing with body, we return tokens`() = withBaseAuthTestApplication {
        val authResponse = CredentialsResponse("", "", 0)
        coEvery { authController.refreshTokens(any()) } returns authResponse

        val body = toJsonBody(RefreshTokenDto("valid-refresh-token"))
        val response = doCall(HttpMethod.Post, "/api/oauth/refresh", body, authorized = false, clientType = null)

        Assertions.assertThat(response.status).isEqualTo(HttpStatusCode.OK)
        Assertions.assertThat(response.parseBody<CredentialsResponse>()).isEqualTo(authResponse)
    }
    // </editor-fold>

    // <editor-fold desc="Status">
    @Test
    fun `when getting status without authentication, we return unauthenticated status`() = withBaseAuthTestApplication {
        val unauthenticatedStatus = AuthStatusResponse(
            authenticated = false
        )
        coEvery { authController.getAuthStatus(any(), any()) } returns unauthenticatedStatus

        val response = doCall(HttpMethod.Get, "/api/oauth/status", authorized = false)

        Assertions.assertThat(response.status).isEqualTo(HttpStatusCode.OK)
        val status = response.parseBody<AuthStatusResponse>()
        Assertions.assertThat(status.authenticated).isFalse()
    }

    @Test
    fun `when getting status with authentication, we return authenticated status`() = withBaseAuthTestApplication {
        val authenticatedStatus = AuthStatusResponse(
            authenticated = true,
            role = "user",
            userId = "test-user-id",
            username = "test@example.com"
        )
        coEvery { authController.getAuthStatus(any(), any()) } returns authenticatedStatus

        val response = doCall(HttpMethod.Get, "/api/oauth/status")

        Assertions.assertThat(response.status).isEqualTo(HttpStatusCode.OK)
        val status = response.parseBody<AuthStatusResponse>()
        Assertions.assertThat(status.authenticated).isTrue()
        Assertions.assertThat(status.role).isEqualTo("user")
        Assertions.assertThat(status.userId).isEqualTo("test-user-id")
        Assertions.assertThat(status.username).isEqualTo("test@example.com")
    }
    // </editor-fold>

    // <editor-fold desc="Logout">
    @Test
    fun `when logging out, we clear cookies and return success`() = withBaseAuthTestApplication {
        coEvery { authController.logout(any()) } returns Unit

        val response = doCall(HttpMethod.Post, "/api/oauth/logout")

        Assertions.assertThat(response.status).isEqualTo(HttpStatusCode.OK)

        // Check that cookies are cleared (maxAge = 0)
        val cookies = response.headers.getAll("Set-Cookie") ?: emptyList()
        val accessTokenCookie = cookies.find { it.contains("access_token") }
        val refreshTokenCookie = cookies.find { it.contains("refresh_token") }

        Assertions.assertThat(accessTokenCookie).isNotNull
        Assertions.assertThat(refreshTokenCookie).isNotNull
        Assertions.assertThat(accessTokenCookie).contains("Max-Age=0")
        Assertions.assertThat(refreshTokenCookie).contains("Max-Age=0")
    }
    // </editor-fold>

    // <editor-fold desc="Cookies">
    @Test
    fun `when logging in, we set auth cookies`() = withBaseAuthTestApplication {
        val authResponse = CredentialsResponse("access-token-value", "refresh-token-value", 86400)
        coEvery { authController.authorizeUser(any()) } returns authResponse

        val response = client.request("/api/oauth/token") {
            method = HttpMethod.Post
            contentType(ContentType.Application.Json)
            header(Constants.CLIENT_TYPE_HEADER, "web")
            header(HttpHeaders.Origin, "https://cb-connect-it.com")
            setBody(toJsonBody(CreateTokenDto("test@example.com", "password")))
        }

        Assertions.assertThat(response.status).isEqualTo(HttpStatusCode.OK)

        // Check that cookies are set
        val cookies = response.headers.getAll("Set-Cookie") ?: emptyList()
        val accessTokenCookie = cookies.find { it.contains("access_token") }
        val refreshTokenCookie = cookies.find { it.contains("refresh_token") }

        Assertions.assertThat(accessTokenCookie).isNotNull
        Assertions.assertThat(refreshTokenCookie).isNotNull

        // Check cookie properties
        Assertions.assertThat(accessTokenCookie).contains("access-token-value")
        Assertions.assertThat(accessTokenCookie).contains("HttpOnly")
        Assertions.assertThat(accessTokenCookie).contains("Path=/api")
        Assertions.assertThat(accessTokenCookie).contains("SameSite=Lax")

        Assertions.assertThat(refreshTokenCookie).contains("refresh-token-value")
        Assertions.assertThat(refreshTokenCookie).contains("HttpOnly")
        Assertions.assertThat(refreshTokenCookie).contains("Path=/api/oauth")
        Assertions.assertThat(refreshTokenCookie).contains("SameSite=Lax")
    }

    @Test
    fun `when logging in from web client without origin but trusted referer, we set auth cookies`() = withBaseAuthTestApplication {
        val authResponse = CredentialsResponse("access-token-value", "refresh-token-value", 86400)
        coEvery { authController.authorizeUser(any()) } returns authResponse

        val response = client.request("/api/oauth/token") {
            method = HttpMethod.Post
            contentType(ContentType.Application.Json)
            header(Constants.CLIENT_TYPE_HEADER, "web")
            header(HttpHeaders.Referrer, "https://cb-connect-it.com/some/page")
            setBody(toJsonBody(CreateTokenDto("test@example.com", "password")))
        }

        Assertions.assertThat(response.status).isEqualTo(HttpStatusCode.OK)

        val cookies = response.headers.getAll("Set-Cookie") ?: emptyList()
        val accessTokenCookie = cookies.find { it.contains("access_token") }
        val refreshTokenCookie = cookies.find { it.contains("refresh_token") }

        Assertions.assertThat(accessTokenCookie).isNotNull
        Assertions.assertThat(refreshTokenCookie).isNotNull
    }

    @Test
    fun `when refreshing tokens, we set new auth cookies`() = withBaseAuthTestApplication {
        val authResponse = CredentialsResponse("new-access-token", "new-refresh-token", 86400)
        coEvery { authController.refreshTokens(any()) } returns authResponse

        val response = client.request("/api/oauth/refresh") {
            method = HttpMethod.Post
            contentType(ContentType.Application.Json)
            header(Constants.CLIENT_TYPE_HEADER, "web")
            header(HttpHeaders.Origin, "https://cb-connect-it.com")
            setBody(toJsonBody(RefreshTokenDto("old-refresh-token")))
        }

        Assertions.assertThat(response.status).isEqualTo(HttpStatusCode.OK)

        // Check that new cookies are set
        val cookies = response.headers.getAll("Set-Cookie") ?: emptyList()
        val accessTokenCookie = cookies.find { it.contains("access_token") }
        val refreshTokenCookie = cookies.find { it.contains("refresh_token") }

        Assertions.assertThat(accessTokenCookie).contains("new-access-token")
        Assertions.assertThat(refreshTokenCookie).contains("new-refresh-token")
    }

    @Test
    fun `when logging in as non-web client, we return body and do not set cookies`() = withBaseAuthTestApplication {
        val authResponse = CredentialsResponse("access-token-value", "refresh-token-value", 86400)
        coEvery { authController.authorizeUser(any()) } returns authResponse

        val body = toJsonBody(CreateTokenDto("test@example.com", "password"))
        val response = doCall(HttpMethod.Post, "/api/oauth/token", body, authorized = false, clientType = "mobile")

        Assertions.assertThat(response.status).isEqualTo(HttpStatusCode.OK)
        Assertions.assertThat(response.parseBody<CredentialsResponse>()).isEqualTo(authResponse)
        Assertions.assertThat(response.headers.getAll("Set-Cookie")).isNull()
    }

    @Test
    fun `when logging in as web client from untrusted origin, we return body and do not set cookies`() = withBaseAuthTestApplication {
        val authResponse = CredentialsResponse("access-token-value", "refresh-token-value", 86400)
        coEvery { authController.authorizeUser(any()) } returns authResponse

        val response = client.request("/api/oauth/token") {
            method = HttpMethod.Post
            contentType(ContentType.Application.Json)
            header(Constants.CLIENT_TYPE_HEADER, "web")
            header(HttpHeaders.Origin, "https://evil.example")
            setBody(toJsonBody(CreateTokenDto("test@example.com", "password")))
        }

        Assertions.assertThat(response.status).isEqualTo(HttpStatusCode.OK)
        Assertions.assertThat(response.parseBody<CredentialsResponse>()).isEqualTo(authResponse)
        Assertions.assertThat(response.headers.getAll("Set-Cookie")).isNull()
    }

    @Test
    fun `when forwarding https proto on login, auth cookies are secure`() = withBaseAuthTestApplication {
        val authResponse = CredentialsResponse("access-token-value", "refresh-token-value", 86400)
        coEvery { authController.authorizeUser(any()) } returns authResponse

        val response = client.request("/api/oauth/token") {
            method = HttpMethod.Post
            contentType(ContentType.Application.Json)
            header(Constants.CLIENT_TYPE_HEADER, "web")
            header(HttpHeaders.Origin, "https://cb-connect-it.com")
            header("X-Forwarded-Proto", "https")
            setBody(toJsonBody(CreateTokenDto("test@example.com", "password")))
        }

        Assertions.assertThat(response.status).isEqualTo(HttpStatusCode.OK)

        val cookies = response.headers.getAll("Set-Cookie") ?: emptyList()
        val accessTokenCookie = cookies.find { it.contains("access_token") }
        val refreshTokenCookie = cookies.find { it.contains("refresh_token") }

        Assertions.assertThat(accessTokenCookie).contains("Secure")
        Assertions.assertThat(refreshTokenCookie).contains("Secure")
    }

    @Test
    fun `when forwarding http proto on login, auth cookies are not secure`() = withBaseAuthTestApplication {
        val authResponse = CredentialsResponse("access-token-value", "refresh-token-value", 86400)
        coEvery { authController.authorizeUser(any()) } returns authResponse

        val response = client.request("/api/oauth/token") {
            method = HttpMethod.Post
            contentType(ContentType.Application.Json)
            header(Constants.CLIENT_TYPE_HEADER, "web")
            header(HttpHeaders.Origin, "https://cb-connect-it.com")
            header("X-Forwarded-Proto", "http")
            setBody(toJsonBody(CreateTokenDto("test@example.com", "password")))
        }

        Assertions.assertThat(response.status).isEqualTo(HttpStatusCode.OK)

        val cookies = response.headers.getAll("Set-Cookie") ?: emptyList()
        val accessTokenCookie = cookies.find { it.contains("access_token") }
        val refreshTokenCookie = cookies.find { it.contains("refresh_token") }

        Assertions.assertThat(accessTokenCookie).doesNotContain("Secure")
        Assertions.assertThat(refreshTokenCookie).doesNotContain("Secure")
    }

    @Test
    fun `when forwarding https proto on logout, cleared cookies remain secure`() = withBaseAuthTestApplication {
        coEvery { authController.logout(any()) } returns Unit

        val response = client.request("/api/oauth/logout") {
            method = HttpMethod.Post
            contentType(ContentType.Application.Json)
            header("X-Forwarded-Proto", "https")
        }

        Assertions.assertThat(response.status).isEqualTo(HttpStatusCode.OK)

        val cookies = response.headers.getAll("Set-Cookie") ?: emptyList()
        val accessTokenCookie = cookies.find { it.contains("access_token") }
        val refreshTokenCookie = cookies.find { it.contains("refresh_token") }

        Assertions.assertThat(accessTokenCookie).contains("Max-Age=0")
        Assertions.assertThat(refreshTokenCookie).contains("Max-Age=0")
        Assertions.assertThat(accessTokenCookie).contains("Secure")
        Assertions.assertThat(refreshTokenCookie).contains("Secure")
    }
    // </editor-fold>
}
