package com.cbconnectit.plugins

import com.auth0.jwt.interfaces.JWTVerifier
import com.cbconnectit.data.database.dao.*
import com.cbconnectit.domain.interfaces.*
import com.cbconnectit.modules.auth.AuthController
import com.cbconnectit.modules.auth.AuthControllerImpl
import com.cbconnectit.modules.auth.JwtConfig
import com.cbconnectit.modules.auth.TokenProvider
import com.cbconnectit.modules.companies.CompanyController
import com.cbconnectit.modules.companies.CompanyControllerImpl
import com.cbconnectit.modules.jobPositions.JobPositionController
import com.cbconnectit.modules.jobPositions.JobPositionControllerImpl
import com.cbconnectit.modules.links.LinkController
import com.cbconnectit.modules.links.LinkControllerImpl
import com.cbconnectit.modules.projects.ProjectController
import com.cbconnectit.modules.projects.ProjectControllerImpl
import com.cbconnectit.modules.services.ServiceController
import com.cbconnectit.modules.services.ServiceControllerImpl
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
                controllerModule(),
                daoModule()
            )
        }
    }
}

fun controllerModule() = module {
    singleOf(::AuthControllerImpl) { bind<AuthController>() }
    singleOf(::UserControllerImpl) { bind<UserController>() }
    singleOf(::TagControllerImpl) { bind<TagController>() }
    singleOf(::LinkControllerImpl) { bind<LinkController>() }
    singleOf(::ServiceControllerImpl) { bind<ServiceController>() }
    singleOf(::ProjectControllerImpl) { bind<ProjectController>() }
    singleOf(::JobPositionControllerImpl) { bind<JobPositionController>() }
    singleOf(::CompanyControllerImpl) { bind<CompanyController>() }
}

fun daoModule() = module {
    singleOf(::UserDaoImpl) { bind<IUserDao>() }
    singleOf(::TagDaoImpl) { bind<ITagDao>() }
    singleOf(::ServiceDaoImpl) { bind<IServiceDao>() }
    singleOf(::LinkDaoImpl) { bind<ILinkDao>() }
    singleOf(::ProjectDaoImpl) { bind<IProjectDao>() }
    singleOf(::JobPositionDaoImpl) { bind<IJobPositionDao>() }
    singleOf(::CompanyDaoImpl) { bind<ICompanyDao>() }
}