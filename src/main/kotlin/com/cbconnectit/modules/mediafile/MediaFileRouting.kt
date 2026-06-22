package com.cbconnectit.modules.mediafile

import com.cbconnectit.data.dto.requests.mediafile.InsertMediaFile
import com.cbconnectit.data.dto.requests.mediafile.UpdateMediaFile
import com.cbconnectit.plugins.statuspages.ErrorInvalidParameters
import com.cbconnectit.utils.ParamConstants
import com.cbconnectit.utils.ParamConstants.ADMIN_AUTHENTICATE_KEY
import com.cbconnectit.utils.getFile
import com.cbconnectit.utils.getMediaFileId
import com.cbconnectit.utils.getPayload
import com.cbconnectit.utils.receiveOrRespondWithError
import com.cbconnectit.utils.toParts
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json

fun Route.mediaFileRouting(
    json: Json,
    controller: MediaFileController
) {
    route("media-files") {
        authenticate(ADMIN_AUTHENTICATE_KEY) {
            // Admin endpoints
            post {
                val parts = call
                    // `formFieldLimit` can be used to limit the size of files, by default, this is 50MB
                    .receiveMultipart()
                    .toParts()

                val imageFile = parts.getFile("image") ?: throw ErrorInvalidParameters
                val insertRequest = parts.getPayload<InsertMediaFile>(json) ?: throw ErrorInvalidParameters

                val mediaFile = controller.create(insertRequest, imageFile)

                call.respond(HttpStatusCode.Created, mediaFile)
            }

            get {
                val mediaFiles = controller.readAll()
                call.respond(mediaFiles)
            }

            get("{${ParamConstants.MEDIA_FILE_ID_KEY}}") {
                val id = call.getMediaFileId()
                val mediaFile = controller.readById(id)
                call.respond(mediaFile)
            }

            put("{${ParamConstants.MEDIA_FILE_ID_KEY}}") {
                val id = call.getMediaFileId()
                val request = call.receiveOrRespondWithError<UpdateMediaFile>()
                val updated = controller.update(id, request)
                call.respond(updated)
            }

            delete("{${ParamConstants.MEDIA_FILE_ID_KEY}}") {
                val id = call.getMediaFileId()
                controller.delete(id)
                call.respond(HttpStatusCode.NoContent)
            }
        }
    }
}
