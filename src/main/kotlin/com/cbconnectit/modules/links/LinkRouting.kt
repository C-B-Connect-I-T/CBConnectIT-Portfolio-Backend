package com.cbconnectit.modules.links

import com.cbconnectit.data.dto.requests.link.InsertNewLink
import com.cbconnectit.data.dto.requests.link.UpdateLink
import com.cbconnectit.utils.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.linkRouting() {

    val linkController by inject<LinkController>()

    route("links") {
        get {
            val links = linkController.getLinks()
            call.respond(links)
        }

        get("/{${ParamConstants.LINK_ID_KEY}}") {
            val linkId = call.getLinkId()
            val link = linkController.getLinkById(linkId)
            call.respond(link)
        }

        authenticate {
            post {
                val insertNewLink = call.receiveOrRespondWithError<InsertNewLink>()
                val link = linkController.postLink(insertNewLink)
                call.respond(HttpStatusCode.Created, link)
            }

            put("{${ParamConstants.LINK_ID_KEY}}") {
                val linkId = call.getLinkId()
                val updateLink = call.receiveOrRespondWithError<UpdateLink>()
                val link = linkController.updateLinkById(linkId, updateLink)
                call.respond(link)
            }

            delete("{${ParamConstants.LINK_ID_KEY}}") {
                val linkId = call.getLinkId()
                linkController.deleteLinkById(linkId)
                sendOk()
            }
        }
    }
}