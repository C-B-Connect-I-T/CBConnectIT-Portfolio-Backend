package com.cbconnectit.modules.tags

import com.cbconnectit.data.dto.requests.tag.InsertNewTag
import com.cbconnectit.data.dto.requests.tag.UpdateTag
import com.cbconnectit.utils.ParamConstants
import com.cbconnectit.utils.getTagSomething
import com.cbconnectit.utils.receiveOrRespondWithError
import com.cbconnectit.utils.sendOk
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import java.util.*

fun Route.tagRouting() {

    val tagController by inject<TagController>()

    route("tags") {
        get {
            val tags = tagController.getTags("")
            call.respond(tags)
        }

        get("/{${ParamConstants.TAG_IDENTIFIER_KEY}}") {
            val tagSomething = call.getTagSomething()
            val tagUUID = try {
                UUID.fromString(tagSomething)
            } catch (e: IllegalArgumentException) {
                null
            }

            val tag = if (tagUUID != null) {
                tagController.getTagById(tagUUID)
            } else {
                tagController.getTagBySlug(tagSomething)
            }
            call.respond(tag)
        }

        authenticate {
            post {
                val insertNewTag = call.receiveOrRespondWithError<InsertNewTag>()
                val tag = tagController.postTag(insertNewTag)
                call.respond(HttpStatusCode.Created, tag)
            }

            put("{${ParamConstants.TAG_IDENTIFIER_KEY}}") {
                val tagId = call.getTagSomething().let { UUID.fromString(it) }
                val updateTag = call.receiveOrRespondWithError<UpdateTag>()
                val tag = tagController.updateTagById(tagId, updateTag)
                call.respond(tag)
            }

            delete("{${ParamConstants.TAG_IDENTIFIER_KEY}}") {
                val tagId = call.getTagSomething().let { UUID.fromString(it) }
                tagController.deleteTagById(tagId)
                sendOk()
            }
        }
    }
}