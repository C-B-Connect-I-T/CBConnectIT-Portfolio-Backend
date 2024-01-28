package com.cbconnectit.routing

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.cbconnectit.domain.models.user.User
import com.cbconnectit.domain.models.user.UserRoles
import com.cbconnectit.modules.auth.adminOnly
import com.cbconnectit.utils.toDatabaseString
import com.google.gson.Gson
import io.ktor.http.*
import io.ktor.serialization.gson.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.testing.*
import org.koin.core.context.stopKoin
import org.koin.core.module.Module
import org.koin.ktor.plugin.Koin
import java.time.LocalDateTime
import java.util.*

abstract class BaseRoutingTest {

    private val gson = Gson()
    protected var koinModules: Module? = null
    protected var moduleList: Application.() -> Unit = { }

    init {
        stopKoin()
    }

    fun <R> withBaseTestApplication(vararg authenticationTest: AuthenticationInstrumentation = emptyArray(), test: TestApplicationEngine.() -> R) {
        withTestApplication({
            install(ContentNegotiation) {
                gson()
            }
            koinModules?.let {
                install(Koin) {
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
            moduleList()
        }) {
            test()
        }
    }

    fun toJsonBody(obj: Any): String = gson.toJson(obj)

    fun <R> TestApplicationResponse.parseBody(clazz: Class<R>): R {
        return gson.fromJson(content, clazz)
    }

    private fun AuthenticationConfig.jwtTest(authenticationTest: AuthenticationInstrumentation) = jwt(authenticationTest.name) {
        validate { User() }

        verifier(JWT.require(Algorithm.HMAC256("secret")).build())

        validate { _ ->
            val time = LocalDateTime.now().toDatabaseString()

            return@validate when (authenticationTest.name) {
                adminOnly -> {
                    if (authenticationTest.userRole != UserRoles.Admin) return@validate null

                    User(UUID.fromString("00000000-0000-0000-0000-000000000001"), "Chris Bol", "chris.bol@example.com", time, time, authenticationTest.userRole)
                }

                "error" -> null // Will be used whenever we want to force an invalid user during the tests!
                else -> User(UUID.fromString("00000000-0000-0000-0000-000000000001"), "Chris Bol", "chris.bol@example.com", time, time, authenticationTest.userRole)
            }
        }
    }

    private val bearerToken = JWT.create().sign(Algorithm.HMAC256("secret"))

    protected fun TestApplicationEngine.doCall(
        method: HttpMethod,
        uri: String,
        body: String? = null,
        authorized: Boolean = true
    ) = handleRequest(method, uri) {
        addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
        if (authorized) {
            addHeader(HttpHeaders.Authorization, "Bearer $bearerToken")
        }
        body?.let(::setBody)
    }
}

data class AuthenticationInstrumentation(val name: String? = null, val userRole: UserRoles = UserRoles.User)