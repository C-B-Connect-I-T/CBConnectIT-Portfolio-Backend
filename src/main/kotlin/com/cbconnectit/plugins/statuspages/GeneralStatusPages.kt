package com.cbconnectit.plugins.statuspages

import io.ktor.http.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.*

data class InternalServerException(val body: String? = null) : ApiException("internal_error", "Internal error " + body.orEmpty(), HttpStatusCode.InternalServerError)
//
// object InvalidContributorException : ApiException("invalid_contributor", "Invalid contributor", HttpStatusCode.BadRequest)

object ErrorMissingBody : ApiException("missing_body", "A body should be provided for this call to work, please check what is going wrong", HttpStatusCode.BadRequest)
object ErrorDuplicateEntity : ApiException("duplicate_entity", "The entity already exists", HttpStatusCode.Conflict)
object ErrorUsernameExists : ApiException("username_exists", "The provided username already exists", HttpStatusCode.Conflict)
object ErrorPasswordsDoNotMatch : ApiException("passwords_do_not_match", "The provided passwords do not match", HttpStatusCode.Conflict)
object ErrorSameAsOldPassword : ApiException("same_as_old_password", "The provided password cannot be the same as the old password", HttpStatusCode.Conflict)
object ErrorWeakPassword : ApiException("weak_password", "The provided password is too weak", HttpStatusCode.Conflict)
object ErrorFailedCreate : ApiException("create_failed", "The resource could not be created", HttpStatusCode.InternalServerError) // TODO: are we sure this should be an internal server error?
object ErrorFailedDelete : ApiException("delete_failed", "The resource could not be deleted", HttpStatusCode.InternalServerError) // TODO: are we sure this should be an internal server error?
object ErrorFailedUpdate : ApiException("update_failed", "The resource could not be updated", HttpStatusCode.Conflict)
object ErrorInvalidCredentials : ApiException("invalid_credentials", "The credentials provided are invalid", HttpStatusCode.Unauthorized)
object ErrorInvalidParameters : ApiException("invalid_parameters", "The parameters provided are invalid", HttpStatusCode.BadRequest)

// object ErrorInvalidRequest : ApiException("invalid_request", "Invalid request malformed authorization header", HttpStatusCode.BadRequest)
// object ErrorInvalidScope : ApiException("invalid_scope", "Invalid scope Requested scope is invalid", HttpStatusCode.BadRequest)
object ErrorInvalidToken : ApiException("invalid_scope", "The token provided is not valid", HttpStatusCode.BadRequest)
object ErrorInvalidUUID : ApiException("invalid_uuid", "The uuid provided is not a valid uuid", HttpStatusCode.BadRequest)
object ErrorMissingParameters : ApiException("missing_parameters", "Missing parameters for required field", HttpStatusCode.BadRequest)
object ErrorNotFound : ApiException("not_found", "The resource could not be found", HttpStatusCode.NotFound)
object ErrorUnauthorized : ApiException("unauthorized", "The user is not authorized to perform this action", HttpStatusCode.Forbidden)

data class ErrorUnknownServiceIdsForCreate(private val ids: List<UUID>) : ApiException("unknown_ids_for_create", "Can't create service with unknown parent service ${ids.joinToString(", ")}", HttpStatusCode.BadRequest)

data class ErrorUnknownServiceIdsForUpdate(private val ids: List<UUID>) : ApiException("unknown_ids_for_update", "Can't update service with unknown parent service ${ids.joinToString(", ")}", HttpStatusCode.BadRequest)

data class ErrorUnknownTagIdsForCreate(private val ids: List<UUID>) : ApiException("unknown_ids_for_create", "Can't create service with unknown tag ${ids.joinToString(", ")}", HttpStatusCode.BadRequest)

data class ErrorUnknownTagIdsForUpdate(private val ids: List<UUID>) : ApiException("unknown_ids_for_update", "Can't update service with unknown tag ${ids.joinToString(", ")}", HttpStatusCode.BadRequest)

data class ErrorUnknownLinkIdsForCreateProject(private val ids: List<UUID>) : ApiException("unknown_ids_for_create", "Can't create project with unknown links ${ids.joinToString(", ")}", HttpStatusCode.BadRequest)

data class ErrorUnknownLinkIdsForUpdateProject(private val ids: List<UUID>) : ApiException("unknown_ids_for_update", "Can't update project with unknown links ${ids.joinToString(", ")}", HttpStatusCode.BadRequest)

data class ErrorUnknownTagIdsForCreateProject(private val ids: List<UUID>) : ApiException("unknown_ids_for_create", "Can't create project with unknown tags ${ids.joinToString(", ")}", HttpStatusCode.BadRequest)

data class ErrorUnknownTagIdsForUpdateProject(private val ids: List<UUID>) : ApiException("unknown_ids_for_update", "Can't update project with unknown tags ${ids.joinToString(", ")}", HttpStatusCode.BadRequest)

data class ErrorUnknownLinkIdsForCreateCompany(private val ids: List<UUID>) : ApiException("unknown_ids_for_create", "Can't create company with unknown links ${ids.joinToString(", ")}", HttpStatusCode.BadRequest)

data class ErrorUnknownLinkIdsForUpdateCompany(private val ids: List<UUID>) : ApiException("unknown_ids_for_update", "Can't update company with unknown links ${ids.joinToString(", ")}", HttpStatusCode.BadRequest)

data class ErrorUnknownCompanyIdsForCreateTestimonial(private val ids: List<UUID>) : ApiException("unknown_ids_for_create", "Can't create testimonial with unknown company ${ids.joinToString(", ")}", HttpStatusCode.BadRequest)

data class ErrorUnknownCompanyIdsForUpdateTestimonial(private val ids: List<UUID>) : ApiException("unknown_ids_for_update", "Can't update testimonial with unknown company ${ids.joinToString(", ")}", HttpStatusCode.BadRequest)

data class ErrorUnknownJobPositionIdsForCreateTestimonial(private val ids: List<UUID>) : ApiException("unknown_ids_for_create", "Can't create testimonial with unknown job position ${ids.joinToString(", ")}", HttpStatusCode.BadRequest)

data class ErrorUnknownJobPositionIdsForUpdateTestimonial(private val ids: List<UUID>) : ApiException("unknown_ids_for_update", "Can't update testimonial with unknown job position ${ids.joinToString(", ")}", HttpStatusCode.BadRequest)

data class ErrorUnknownCompanyIdsForCreateExperience(private val ids: List<UUID>) : ApiException("unknown_ids_for_create", "Can't create experience with unknown company ${ids.joinToString(", ")}", HttpStatusCode.BadRequest)

data class ErrorUnknownCompanyIdsForUpdateExperience(private val ids: List<UUID>) : ApiException("unknown_ids_for_update", "Can't update experience with unknown company ${ids.joinToString(", ")}", HttpStatusCode.BadRequest)

data class ErrorUnknownJobPositionIdsForCreateExperience(private val ids: List<UUID>) : ApiException("unknown_ids_for_create", "Can't create experience with unknown job position ${ids.joinToString(", ")}", HttpStatusCode.BadRequest)

data class ErrorUnknownJobPositionIdsForUpdateExperience(private val ids: List<UUID>) : ApiException("unknown_ids_for_update", "Can't update experience with unknown job position ${ids.joinToString(", ")}", HttpStatusCode.BadRequest)

data class ErrorUnknownTagIdsForCreateExperience(private val ids: List<UUID>) : ApiException("unknown_ids_for_create", "Can't create experience with unknown tags ${ids.joinToString(", ")}", HttpStatusCode.BadRequest)

data class ErrorUnknownTagIdsForUpdateExperience(private val ids: List<UUID>) : ApiException("unknown_ids_for_update", "Can't update experience with unknown tags ${ids.joinToString(", ")}", HttpStatusCode.BadRequest)

open class ApiException(
    val error: String,
    @SerialName("error_description")
    val errorDescription: String,
    val statusCode: HttpStatusCode
) : Exception() {
    open var errors: ArrayList<String>? = null
}

@Serializable
data class ErrorResponse(
    val error: String,
    @SerialName("error_description")
    val errorDescription: String,
    val status: Int,
    val errors: ArrayList<String>? = null
)

fun ApiException.toErrorResponse() = ErrorResponse(error, errorDescription.trim(), statusCode.value, errors)
