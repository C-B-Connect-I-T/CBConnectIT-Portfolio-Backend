package com.cbconnectit.routing

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.cbconnectit.domain.models.user.User
import com.cbconnectit.domain.models.user.UserRoles
import com.cbconnectit.modules.auth.ADMIN_ONLY
import com.cbconnectit.statuspages.InternalServerException
import com.cbconnectit.statuspages.generalStatusPages
import com.cbconnectit.statuspages.toErrorResponse
import com.cbconnectit.utils.toDatabaseString
import com.google.gson.Gson
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.http.auth.*
import io.ktor.serialization.gson.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.testing.*
import org.koin.core.context.stopKoin
import org.koin.core.module.Module
import org.koin.ktor.plugin.Koin
import java.time.LocalDateTime
import java.util.*

abstract class BaseRoutingTest {

    protected val gson = Gson()
    protected var koinModules: Module? = null
    protected var moduleList: Application.() -> Unit = { }

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
                    gson()
                }

                install(StatusPages) {
                    generalStatusPages()

                    exception<Throwable> { call, _ ->
                        val cause = InternalServerException()
                        call.respond(cause.statusCode, cause.toErrorResponse())
                    }
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

    fun toJsonBody(obj: Any): String = gson.toJson(obj)

    protected suspend inline fun <reified T> HttpResponse.parseBody(): T {
        return gson.fromJson(this.bodyAsText(), T::class.java)
    }

    private fun AuthenticationConfig.jwtTest(authenticationTest: AuthenticationInstrumentation) = jwt(authenticationTest.name) {
        validate { User() }

        verifier(JWT.require(Algorithm.HMAC256("secret")).build())

        validate { _ ->
            val time = LocalDateTime.now().toDatabaseString()

            return@validate when (authenticationTest.name) {
                ADMIN_ONLY -> {
                    if (authenticationTest.userRole != UserRoles.Admin) return@validate null

                    User(UUID.fromString("00000000-0000-0000-0000-000000000001"), "Chris Bol", "chris.bol@example.com", LocalDateTime.now(), LocalDateTime.now(), authenticationTest.userRole)
                }

                "error" -> null // Will be used whenever we want to force an invalid user during the tests!
                else -> User(UUID.fromString("00000000-0000-0000-0000-000000000001"), "Chris Bol", "chris.bol@example.com", LocalDateTime.now(), LocalDateTime.now(), authenticationTest.userRole)
            }
        }
    }

    private val bearerToken = JWT.create().sign(Algorithm.HMAC256("secret"))

    protected suspend fun ApplicationTestBuilder.doCall(
        method: HttpMethod,
        uri: String,
        body: String? = null,
        authorized: Boolean = true,
        multipartCall: Boolean = false
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

        if (body != null) {
            setBody(body)
        }
    }
}

data class AuthenticationInstrumentation(val name: String? = null, val userRole: UserRoles = UserRoles.User)
