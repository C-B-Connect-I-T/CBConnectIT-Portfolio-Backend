package com.cbconnectit.utils

import com.cbconnectit.domain.models.user.User
import com.cbconnectit.statuspages.ApiException
import com.cbconnectit.statuspages.ErrorInvalidParameters
import com.cbconnectit.statuspages.ErrorInvalidUUID
import com.cbconnectit.statuspages.ErrorMissingBody
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.util.pipeline.*
import java.net.URL
import java.util.*

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

fun ApplicationCall.getUserId(): UUID = parameters[ParamConstants.USER_ID_KEY]?.let { UUID.fromString(it) } ?: throw ErrorInvalidUUID
fun ApplicationCall.getLinkId(): UUID = parameters[ParamConstants.LINK_ID_KEY]?.let { UUID.fromString(it) } ?: throw ErrorInvalidUUID
fun ApplicationCall.getTagIdentifier(): String = parameters[ParamConstants.TAG_IDENTIFIER_KEY] ?: throw ErrorInvalidParameters
//fun ApplicationCall.getProjectId(): UUID = parameters[ParamConstants.PROJECT_ID_KEY]?.toIntOrNull() ?: throw ErrorInvalidUUID


suspend fun PipelineContext<Unit, ApplicationCall>.sendOk() {
    call.respond(HttpStatusCode.OK)
}

object TBDException : ApiException("TBD_error", "An error, but still under development", HttpStatusCode.InternalServerError)

val String.isValidUrl: Boolean
    get() = try {
        // Attempt to create a URL object from the given string
        URL(this)

        // If no exception is thrown, the URL is valid
        true
    } catch (e: Exception) {
        // If an exception is thrown, the URL is invalid
        false
    }