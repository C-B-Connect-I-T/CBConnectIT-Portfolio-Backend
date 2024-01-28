package com.cbconnectit.plugins

import com.auth0.jwt.interfaces.JWTVerifier
import com.cbconnectit.domain.interfaces.IUserDao
import com.cbconnectit.modules.auth.adminOnly
import com.cbconnectit.modules.auth.setupAuthentication
import com.cbconnectit.modules.auth.validateUser
import com.cbconnectit.modules.auth.validateUserIsAdmin
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import org.koin.ktor.ext.inject

fun Application.configureSecurity() {
    val jwtVerifier by inject<JWTVerifier>()
    val userDao by inject<IUserDao>()

    install(Authentication) {
        jwt {
            setupAuthentication(jwtVerifier) {
                it.validateUser(userDao)
            }
        }

        jwt(adminOnly) {
            setupAuthentication(jwtVerifier) {
                it.validateUserIsAdmin(userDao)
            }
        }
    }
}
