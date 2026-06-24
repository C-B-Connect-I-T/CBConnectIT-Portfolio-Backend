package com.cbconnectit.routing

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.cbconnectit.domain.models.user.User
import com.cbconnectit.plugins.statuspages.ApiException
import com.cbconnectit.plugins.statuspages.ErrorUnauthorized
import com.cbconnectit.plugins.statuspages.generalStatusPages
import com.cbconnectit.utils.ParamConstants.ADMIN_AUTHENTICATE_KEY
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.http.auth.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.testing.*
import io.ktor.util.*
import kotlinx.serialization.json.Json
import org.koin.core.context.stopKoin
import org.koin.core.module.Module
import org.koin.ktor.plugin.Koin
import java.time.LocalDateTime
import java.util.*

abstract class BaseRoutingTest {

    protected val json = Json {
        ignoreUnknownKeys = false
        isLenient = false
    }
    protected var koinModules: Module? = null
    protected var moduleList: Application.() -> Unit = { }

    private val AuthenticationExceptionKey = AttributeKey<ApiException>("AuthenticationException")

    init {
        stopKoin()
    }

    fun withBaseTestApplication(
        vararg authenticationTest: AuthenticationInstrumentation = emptyArray(),
        test: suspend ApplicationTestBuilder.() -> Unit
    ) {
        testApplication {
            application {
                install(ContentNegotiation) {
                    json(json)
                }

                install(StatusPages) {
                    generalStatusPages()
                }

                install(Koin) {
                    koinModules?.let {
                        modules(it)
                    }
                }

                if (authenticationTest.isNotEmpty()) {
                    install(Authentication) {
                        authenticationTest.forEach {
                            jwtTest(it)
                        }
                    }
                }

                moduleList() // your function to register routes/modules
            }

            test()
        }
    }

    protected inline fun <reified T> toJsonBody(obj: T): String = json.encodeToString(obj)

    protected suspend inline fun <reified T> HttpResponse.parseBody(): T {
        return json.decodeFromString(this.bodyAsText())
    }

    private fun AuthenticationConfig.jwtTest(authenticationTest: AuthenticationInstrumentation) = jwt(authenticationTest.name) {
        verifier(JWT.require(Algorithm.HMAC256("secret")).build())

        validate { _ ->
            try {
                val result = when (authenticationTest.name) {
                    ADMIN_AUTHENTICATE_KEY -> {
                        // Real validator: throws ErrorUnauthorized if user is authenticated but not an admin
                        val user = User(
                            UUID.fromString("00000000-0000-0000-0000-000000000001"),
                            "Chris Bol",
                            "chris.bol@example.com",
                            role = authenticationTest.userRole
                        )
                        if (user.role != User.Role.Admin) {
                            throw ErrorUnauthorized
                        }
                        user
                    }

                    "error" -> null // Will be used whenever we want to force an invalid user during the tests!
                    else -> User(UUID.fromString("00000000-0000-0000-0000-000000000001"), "Chris Bol", "chris.bol@example.com", LocalDateTime.now(), LocalDateTime.now(), authenticationTest.userRole)
                }
                result
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
                        mapOf(HttpAuthHeader.Parameters.Realm to "Test Realm")
                    )
                )
            )
        }
    }

    private val bearerToken = JWT.create().sign(Algorithm.HMAC256("secret"))
    protected fun buildBearerToken(): String = bearerToken

    protected suspend fun ApplicationTestBuilder.doCall(
        method: HttpMethod,
        uri: String,
        body: String? = null,
        authorized: Boolean = true,
        multipartCall: Boolean = false,
        clientType: String? = "web"
    ) = client.request(uri) {
        this.method = method

        if (multipartCall) {
            contentType(ContentType.MultiPart.FormData)
        } else {
            contentType(ContentType.Application.Json)
        }

        if (authorized && bearerToken != null) {
            header(HttpHeaders.Authorization, "${AuthScheme.Bearer} $bearerToken")
        }

        if (clientType != null) {
            header("X-Client-Type", clientType)
        }

        if (body != null) {
            setBody(body)
        }
    }

    /**
     * Make a call with a custom invalid/expired token to simulate authentication failure
     */
    protected suspend fun ApplicationTestBuilder.doCallWithInvalidToken(
        method: HttpMethod,
        uri: String,
        body: String? = null,
        clientType: String? = "web",
        useInvalidToken: Boolean = true
    ) = client.request(uri) {
        this.method = method
        contentType(ContentType.Application.Json)

        if (useInvalidToken) {
            // Provide an invalid token that won't match any authentication strategy
            header(HttpHeaders.Authorization, "${AuthScheme.Bearer} invalid-token-that-will-fail-validation")
        }

        if (clientType != null) {
            header("X-Client-Type", clientType)
        }

        if (body != null) {
            setBody(body)
        }
    }
}

data class AuthenticationInstrumentation(val name: String? = null, val userRole: User.Role = User.Role.User)
