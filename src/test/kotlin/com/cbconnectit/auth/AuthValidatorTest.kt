package com.cbconnectit.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.Payload
import com.cbconnectit.controllers.BaseControllerTest
import com.cbconnectit.data.database.tables.Constants
import com.cbconnectit.domain.interfaces.IUserDao
import com.cbconnectit.domain.models.user.User
import com.cbconnectit.modules.auth.JwtConfig
import com.cbconnectit.modules.auth.TokenType
import com.cbconnectit.modules.auth.setupAuthentication
import com.cbconnectit.modules.auth.validateUser
import com.cbconnectit.modules.auth.validateUserIsAdmin
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.time.LocalDateTime
import java.util.*
import kotlin.test.assertNull

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AuthValidatorTest : BaseControllerTest() {

    private val userDao: IUserDao = mockk()
    private val payload: Payload = mockk()
    private val jwtSecret = "test-secret"
    private val jwtAlgorithm = Algorithm.HMAC256(jwtSecret)

    @BeforeEach
    override fun before() {
        super.before()
        clearMocks(userDao)
    }

    @Test
    fun `when validating user where payload does not contain UserId, return null -- unauthorized`() {
        coEvery { payload.claims[any()]?.asString() } returns null

        val jwt = JWTCredential(payload)

        runBlocking {
            val response = jwt.validateUser(userDao)
            assertNull(response)
        }
    }

    @Test
    fun `when validating user where payload does not contain correct audience, return null -- unauthorized`() {
        coEvery { payload.claims[any()]?.asString() } returns "00000000-0000-0000-0000-000000000001"
        coEvery { userDao.getUser(any()) } returns User()
        coEvery { payload.audience.contains(any()) } returns false

        val jwt = JWTCredential(payload)

        runBlocking {
            val response = jwt.validateUser(userDao)
            assertNull(response)
        }
    }

    @Test
    fun `when validating user where every thing is correct, return user as principal`() {
        coEvery { payload.claims["userId"]?.asString() } returns "00000000-0000-0000-0000-000000000001"
        coEvery { payload.claims["token_type"]?.asString() } returns TokenType.Access.name
        coEvery { userDao.getUser(any()) } returns User().copy(createdAt = LocalDateTime.now().minusDays(5))
        coEvery { payload.audience.contains(any()) } returns true

        val jwt = JWTCredential(payload)

        runBlocking {
            val response = jwt.validateUser(userDao)
            assertThat(response).isInstanceOf(User::class.java)
        }
    }

    @Test
    fun `when validating user as admin where payload does not contain UserId, return null -- unauthorized`() {
        coEvery { payload.claims["userId"]?.asString() } returns null
        coEvery { payload.claims["token_type"]?.asString() } returns TokenType.Access.name
        coEvery { payload.audience.contains(any()) } returns true

        val jwt = JWTCredential(payload)

        runBlocking {
            val response = jwt.validateUserIsAdmin(userDao)
            assertNull(response)
        }
    }

    @Test
    fun `when validating user as admin where payload does not contain correct audience, return null -- unauthorized`() {
        coEvery { payload.claims[any()]?.asString() } returns "00000000-0000-0000-0000-000000000001"
        coEvery { userDao.getUser(any()) } returns User()
        coEvery { userDao.isUserRoleAdmin(any()) } returns true
        coEvery { payload.audience.contains(any()) } returns false

        val jwt = JWTCredential(payload)

        runBlocking {
            val response = jwt.validateUserIsAdmin(userDao)
            assertNull(response)
        }
    }

    @Test
    fun `when validating user as admin where user is not admin, return null -- unauthorized`() {
        coEvery { payload.claims[any()]?.asString() } returns "00000000-0000-0000-0000-000000000001"
        coEvery { userDao.getUser(any()) } returns User()
        coEvery { userDao.isUserRoleAdmin(any()) } returns false
        coEvery { payload.audience.contains(any()) } returns true

        val jwt = JWTCredential(payload)

        runBlocking {
            val response = jwt.validateUserIsAdmin(userDao)
            assertNull(response)
        }
    }

    @Test
    fun `when validating user as admin where every thing is correct, return user as principal`() {
        coEvery { payload.claims["userId"]?.asString() } returns "00000000-0000-0000-0000-000000000001"
        coEvery { payload.claims["token_type"]?.asString() } returns TokenType.Access.name
        coEvery { userDao.getUser(any()) } returns User().copy(role = User.Role.Admin)
        coEvery { payload.audience.contains(any()) } returns true

        val jwt = JWTCredential(payload)

        runBlocking {
            val response = jwt.validateUserIsAdmin(userDao)
            assertThat(response).isInstanceOf(User::class.java)
        }
    }

    // <editor-fold desc="Authentication Source Behavior Tests">

    @Test
    fun `trusted web client should ignore authorization header and require cookie token`() = testApplication {
        configureProtectedRoute()
        val token = createAccessToken()

        val response = client.get("/protected") {
            header(HttpHeaders.Origin, "https://cb-connect-it.com")
            header(Constants.CLIENT_TYPE_HEADER, "web")
            header(HttpHeaders.Authorization, "Bearer $token")
        }

        assertThat(response.status).isEqualTo(HttpStatusCode.Unauthorized)
    }

    @Test
    fun `trusted web client should authenticate with cookie token`() = testApplication {
        configureProtectedRoute()
        val token = createAccessToken()

        val response = client.get("/protected") {
            header(HttpHeaders.Origin, "https://cb-connect-it.com")
            header(Constants.CLIENT_TYPE_HEADER, "web")
            header(HttpHeaders.Authorization, "Bearer invalid-token")
            header(HttpHeaders.Cookie, "${JwtConfig.ACCESS_TOKEN_COOKIE}=$token")
        }

        assertThat(response.status).isEqualTo(HttpStatusCode.OK)
    }

    @Test
    fun `trusted web client should authenticate with cookie token when origin is missing but referer is trusted`() = testApplication {
        configureProtectedRoute()
        val token = createAccessToken()

        val response = client.get("/protected") {
            header(HttpHeaders.Referrer, "https://cb-connect-it.com/some/page")
            header(Constants.CLIENT_TYPE_HEADER, "web")
            header(HttpHeaders.Authorization, "Bearer invalid-token")
            header(HttpHeaders.Cookie, "${JwtConfig.ACCESS_TOKEN_COOKIE}=$token")
        }

        assertThat(response.status).isEqualTo(HttpStatusCode.OK)
    }

    @Test
    fun `untrusted web client should not authenticate with cookie only`() = testApplication {
        configureProtectedRoute()
        val token = createAccessToken()

        val response = client.get("/protected") {
            header(HttpHeaders.Origin, "https://evil.example")
            header(Constants.CLIENT_TYPE_HEADER, "web")
            header(HttpHeaders.Cookie, "${JwtConfig.ACCESS_TOKEN_COOKIE}=$token")
        }

        assertThat(response.status).isEqualTo(HttpStatusCode.Unauthorized)
    }

    @Test
    fun `api client should authenticate with authorization header from any origin`() = testApplication {
        configureProtectedRoute()
        val token = createAccessToken()

        val response = client.get("/protected") {
            header(HttpHeaders.Origin, "https://evil.example")
            header(Constants.CLIENT_TYPE_HEADER, "mobile")
            header(HttpHeaders.Authorization, "Bearer $token")
            header(HttpHeaders.Cookie, "${JwtConfig.ACCESS_TOKEN_COOKIE}=invalid-token")
        }

        assertThat(response.status).isEqualTo(HttpStatusCode.OK)
    }

    private fun ApplicationTestBuilder.configureProtectedRoute() {
        application {
            install(Authentication) {
                jwt("auth") {
                    val verifier = JWT
                        .require(jwtAlgorithm)
                        .withAudience(JwtConfig.USERS_AUDIENCE)
                        .build()

                    setupAuthentication(verifier) { User() }
                }
            }

            routing {
                authenticate("auth") {
                    get("/protected") {
                        call.respondText("ok")
                    }
                }
            }
        }
    }

    private fun createAccessToken(userId: String = UUID.randomUUID().toString()): String = JWT.create()
        .withAudience(JwtConfig.USERS_AUDIENCE)
        .withClaim(JwtConfig.TOKEN_CLAIM_USER_ID_KEY, userId)
        .withClaim(JwtConfig.TOKEN_CLAIM_TOKEN_TYPE, TokenType.Access.name)
        .sign(jwtAlgorithm)

    // </editor-fold>
}
