package com.cbconnectit.plugins

import com.cbconnectit.modules.auth.AuthController
import com.cbconnectit.modules.auth.authRouting
import com.cbconnectit.modules.companies.CompanyController
import com.cbconnectit.modules.companies.companyRouting
import com.cbconnectit.modules.experiences.ExperienceController
import com.cbconnectit.modules.experiences.experienceRouting
import com.cbconnectit.modules.jobPositions.JobPositionController
import com.cbconnectit.modules.jobPositions.jobPositionRouting
import com.cbconnectit.modules.links.LinkController
import com.cbconnectit.modules.links.linkRouting
import com.cbconnectit.modules.projects.ProjectController
import com.cbconnectit.modules.projects.projectRouting
import com.cbconnectit.modules.services.ServiceController
import com.cbconnectit.modules.services.serviceRouting
import com.cbconnectit.modules.tags.TagController
import com.cbconnectit.modules.tags.tagRouting
import com.cbconnectit.modules.testimonials.TestimonialController
import com.cbconnectit.modules.testimonials.testimonialRouting
import com.cbconnectit.modules.users.UserController
import com.cbconnectit.modules.users.userRouting
import com.cbconnectit.statuspages.ErrorMissingParameters
import com.cbconnectit.statuspages.InternalServerException
import com.cbconnectit.statuspages.generalStatusPages
import com.cbconnectit.statuspages.toErrorResponse
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Application.configureRouting() {
    val authController by inject<AuthController>()
    val userController by inject<UserController>()
    val tagController by inject<TagController>()
    val linkController by inject<LinkController>()
    val serviceController by inject<ServiceController>()
    val projectController by inject<ProjectController>()
    val jobPositionController by inject<JobPositionController>()
    val companyController by inject<CompanyController>()
    val testimonialController by inject<TestimonialController>()
    val experienceController by inject<ExperienceController>()

    // "example/" should resolve to "example/index.html" if present, but default ktor behavior rejects trailing slashes.
    this.install(IgnoreTrailingSlash)

    install(StatusPages) {
        generalStatusPages()

        exception<NullPointerException> { call, _ ->
            val some = ErrorMissingParameters
            call.respond(some.statusCode, some.toErrorResponse())
        }

        exception<Throwable> { call, _ ->
            val cause = InternalServerException()
            call.respond(cause.statusCode, cause.toErrorResponse())
        }

        exception<Exception> { call, _ ->
            val cause = InternalServerException()
            call.respond(cause.statusCode, cause.toErrorResponse())
        }
    }
    routing {
        authRouting(authController)

        route("api/v1") {
            userRouting(userController)
            tagRouting(tagController)
            linkRouting(linkController)
            serviceRouting(serviceController)
            projectRouting(projectController)
            jobPositionRouting(jobPositionController)
            companyRouting(companyController)
            testimonialRouting(testimonialController)
            experienceRouting(experienceController)
        }
    }
}
