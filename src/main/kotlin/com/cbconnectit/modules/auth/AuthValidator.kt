package com.cbconnectit.modules.auth

import com.auth0.jwt.interfaces.JWTVerifier
import com.cbconnectit.domain.interfaces.IUserDao
import com.cbconnectit.plugins.dbQuery
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import java.util.UUID

const val adminOnly = "admin"

fun JWTAuthenticationProvider.Config.setupAuthentication(
    jwtVerifier: JWTVerifier,
    function: suspend (JWTCredential) -> Principal?
) {
//    realm = config.property("jwt.realm").getString()
    verifier(jwtVerifier)

    validate { credential ->
        function(credential)
    }
}

suspend fun JWTCredential.validateUser(userDao: IUserDao): Principal? {
    val userId = payload.claims[JwtConfig.TOKEN_CLAIM_USER_ID_KEY]?.asString() ?: return null

    val user = dbQuery {
        userDao.getUser(UUID.fromString(userId))
    }

    return if (payload.audience.contains(JwtConfig.USERS_AUDIENCE))
        user
    else
        null
}

suspend fun JWTCredential.validateUserIsAdmin(userDao: IUserDao): Principal? {
    val userId = payload.claims[JwtConfig.TOKEN_CLAIM_USER_ID_KEY]?.asString() ?: return null
    val userUUID = UUID.fromString(userId)
    val (isUserRoleAdmin, user) = dbQuery {
        val isUserRoleAdmin = userDao.isUserRoleAdmin(userUUID)
        val user = userDao.getUser(userUUID)

        isUserRoleAdmin to user
    }

    return if (payload.audience.contains(JwtConfig.USERS_AUDIENCE) && isUserRoleAdmin)
        user
    else
        null
}

