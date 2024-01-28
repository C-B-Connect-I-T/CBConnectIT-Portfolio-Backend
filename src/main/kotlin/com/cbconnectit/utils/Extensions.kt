package com.cbconnectit.utils

import com.cbconnectit.domain.models.user.User
import com.cbconnectit.statuspages.ApiException
import com.cbconnectit.statuspages.ErrorInvalidUUID
import com.cbconnectit.statuspages.ErrorMissingBody
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.util.pipeline.*

val ApplicationCall.authenticatedUser get() = authentication.principal<User>()!!

suspend inline fun <reified T> ApplicationCall.receiveOrRespondWithError(): T {
    return try {
        runCatching { receiveNullable<T>() }.getOrNull() ?: run {
            // TODO: I think this happened when the incoming data could not be parsed!
            throw TBDException
        }
    } catch (e: Exception) {
        // This happens when no "body" was added to the network call
        throw ErrorMissingBody
    }
}

fun ApplicationCall.getUserId(): Int = parameters[ParamConstants.USER_ID_KEY]?.toIntOrNull() ?: throw ErrorInvalidUUID
fun ApplicationCall.getProjectId(): Int = parameters[ParamConstants.PROJECT_ID_KEY]?.toIntOrNull() ?: throw ErrorInvalidUUID


suspend fun PipelineContext<Unit, ApplicationCall>.sendOk() {
    call.respond(HttpStatusCode.OK)
}

object TBDException : ApiException("TBD_error", "An error, but still under development", HttpStatusCode.InternalServerError)