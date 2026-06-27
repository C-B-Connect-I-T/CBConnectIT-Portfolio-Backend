package com.cbconnectit.modules.projects

import com.cbconnectit.data.dto.requests.project.InsertNewProject
import com.cbconnectit.data.dto.requests.project.UpdateProject
import com.cbconnectit.plugins.statuspages.ErrorInvalidParameters
import com.cbconnectit.plugins.statuspages.ErrorMissingRequiredMedia
import com.cbconnectit.utils.ParamConstants
import com.cbconnectit.utils.Parts
import com.cbconnectit.utils.getFile
import com.cbconnectit.utils.getPayload
import com.cbconnectit.utils.getProjectId
import com.cbconnectit.utils.receiveOrRespondWithError
import com.cbconnectit.utils.sendOk
import com.cbconnectit.utils.toParts
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json

fun Route.projectRouting(json: Json, projectController: ProjectController) {

    route("projects") {
        get {
            val projects = projectController.getProjects()
            call.respond(projects)
        }

        get("/{${ParamConstants.PROJECT_ID_KEY}}") {
            val projectIdentifier = call.getProjectId()
            val project = projectController.getProjectById(projectIdentifier)
            call.respond(project)
        }

        authenticate {
            post {
                val (imageFile, bannerImageFile, insertNewProject) = getProjectMediaAndPayload<InsertNewProject>(json)
                if (imageFile == null || bannerImageFile == null) throw ErrorMissingRequiredMedia

                val project = projectController.postProject(insertNewProject, imageFile, bannerImageFile)
                call.respond(HttpStatusCode.Created, project)
            }

            put("{${ParamConstants.PROJECT_ID_KEY}}") {
                val projectId = call.getProjectId()
                val (imageFile, bannerImageFile, updateProject) = getProjectMediaAndPayload<UpdateProject>(json)

                val project = projectController.updateProjectById(projectId, updateProject, imageFile, bannerImageFile)
                call.respond(project)
            }

            delete("{${ParamConstants.PROJECT_ID_KEY}}") {
                val projectId = call.getProjectId()
                projectController.deleteProjectById(projectId)
                sendOk()
            }
        }
    }
}

private suspend inline fun <reified T> RoutingContext.getProjectMediaAndPayload(json: Json): Triple<Parts.File?, Parts.File?, T> {
    val contentType = call.request.contentType()

    return if (contentType.match(ContentType.MultiPart.FormData)) {
        val parts = call.receiveMultipart().toParts()
        val imageFile = parts.getFile("image")
        val bannerImageFile = parts.getFile("bannerImage")
        val payload = parts.getPayload<T>(json) ?: throw ErrorInvalidParameters
        Triple(imageFile, bannerImageFile, payload)
    } else {
        val payload = call.receiveOrRespondWithError<T>()
        Triple(null, null, payload)
    }
}
