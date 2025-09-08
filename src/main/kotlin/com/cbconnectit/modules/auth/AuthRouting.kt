package com.cbconnectit.modules.auth

import com.cbconnectit.data.dto.requests.CreateTokenDto
import com.cbconnectit.utils.receiveOrRespondWithError
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.authRouting(authController: AuthController) {

    post("oauth/token") {
        val request = call.receiveOrRespondWithError<CreateTokenDto>()
        val token = authController.authorizeUser(request)
        call.respond(token)
    }
}
