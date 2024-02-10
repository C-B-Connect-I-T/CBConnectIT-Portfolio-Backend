package com.cbconnectit.plugins

import com.auth0.jwt.interfaces.JWTVerifier
import com.cbconnectit.data.database.dao.LinkDaoImpl
import com.cbconnectit.data.database.dao.ServiceDaoImpl
import com.cbconnectit.data.database.dao.TagDaoImpl
import com.cbconnectit.data.database.dao.UserDaoImpl
import com.cbconnectit.domain.interfaces.ILinkDao
import com.cbconnectit.domain.interfaces.IServiceDao
import com.cbconnectit.domain.interfaces.ITagDao
import com.cbconnectit.domain.interfaces.IUserDao
import com.cbconnectit.modules.auth.AuthController
import com.cbconnectit.modules.auth.AuthControllerImpl
import com.cbconnectit.modules.auth.JwtConfig
import com.cbconnectit.modules.auth.TokenProvider
import com.cbconnectit.modules.links.LinkController
import com.cbconnectit.modules.links.LinkControllerImpl
import com.cbconnectit.modules.tags.TagController
import com.cbconnectit.modules.tags.TagControllerImpl
import com.cbconnectit.modules.users.UserController
import com.cbconnectit.modules.users.UserControllerImpl
import com.cbconnectit.utils.PasswordManager
import com.cbconnectit.utils.PasswordManagerContract
import io.ktor.server.application.*
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin

fun Application.configureKoin() {
    module {
        install(Koin) {
            modules(
                module {
                    single<PasswordManagerContract> { PasswordManager }
                    single<TokenProvider> {
                        JwtConfig("https://jwt-provider-domain/", JwtConfig.USERS_AUDIENCE, System.getenv("JWT_SECRET"))
                    }
                    single<JWTVerifier> {
                        val tokenProvider = get<TokenProvider>()
                        tokenProvider.verifier
                    }
                },
                routeModule(),
                daoModule()
            )
        }
    }
}

fun routeModule() = module {
    singleOf(::AuthControllerImpl) { bind<AuthController>() }
    singleOf(::UserControllerImpl) { bind<UserController>() }
    singleOf(::TagControllerImpl) { bind<TagController>() }
    singleOf(::LinkControllerImpl) { bind<LinkController>() }
}

fun daoModule() = module {
    singleOf(::UserDaoImpl) { bind<IUserDao>() }
    singleOf(::TagDaoImpl) { bind<ITagDao>() }
    singleOf(::ServiceDaoImpl) { bind<IServiceDao>() }
    singleOf(::LinkDaoImpl) { bind<ILinkDao>() }
}