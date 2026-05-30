package com.cbconnectit.utils

import com.cbconnectit.domain.models.user.User
import com.cbconnectit.plugins.statuspages.ApiException
import com.cbconnectit.plugins.statuspages.ErrorInvalidParameters
import com.cbconnectit.plugins.statuspages.ErrorInvalidUUID
import com.cbconnectit.plugins.statuspages.ErrorMissingBody
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.net.URL
import java.util.*

val ApplicationCall.authenticatedUser get() = authentication.principal<User>()!!

@SuppressWarnings("TooGenericExceptionCaught", "SwallowedException")
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

fun ApplicationCall.getUserId(): UUID = getParameterUUID(ParamConstants.USER_ID_KEY)
fun ApplicationCall.getLinkId(): UUID = getParameterUUID(ParamConstants.LINK_ID_KEY)
fun ApplicationCall.getJobPositionId(): UUID = getParameterUUID(ParamConstants.JOB_POSITION_ID_KEY)
fun ApplicationCall.getCompanyId(): UUID = getParameterUUID(ParamConstants.COMPANY_ID_KEY)
fun ApplicationCall.getServiceId(): UUID = getParameterUUID(ParamConstants.SERVICE_ID_KEY)
fun ApplicationCall.getTestimonialId(): UUID = getParameterUUID(ParamConstants.TESTIMONIAL_ID_KEY)
fun ApplicationCall.getExperienceId(): UUID = getParameterUUID(ParamConstants.EXPERIENCE_ID_KEY)
fun ApplicationCall.getProjectId(): UUID = getParameterUUID(ParamConstants.PROJECT_ID_KEY)

fun ApplicationCall.getTagIdentifier(): String = parameters[ParamConstants.TAG_IDENTIFIER_KEY] ?: throw ErrorInvalidParameters
// fun ApplicationCall.getProjectId(): UUID = getParameterUUID(ParamConstants.PROJECT_ID_KEY)

private fun ApplicationCall.getParameterUUID(key: String): UUID =
    parameters[key]
        ?.let { runCatching { UUID.fromString(it) }.getOrNull() }
        ?: throw ErrorInvalidUUID

suspend fun RoutingContext.sendOk() {
    call.respond(HttpStatusCode.OK)
}

object TBDException : ApiException("TBD_error", "An error, but still under development", HttpStatusCode.InternalServerError)

val String.isValidUrl: Boolean
    @SuppressWarnings("TooGenericExceptionCaught", "SwallowedException")
    get() = try {
        // Attempt to create a URL object from the given string
        URL(this)

        // If no exception is thrown, the URL is valid
        true
    } catch (e: Exception) {
        // If an exception is thrown, the URL is invalid
        false
    }
