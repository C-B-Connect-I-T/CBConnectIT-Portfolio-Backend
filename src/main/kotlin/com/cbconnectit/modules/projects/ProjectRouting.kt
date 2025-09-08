package com.cbconnectit.modules.projects

import com.cbconnectit.data.dto.requests.project.InsertNewProject
import com.cbconnectit.data.dto.requests.project.UpdateProject
import com.cbconnectit.utils.ParamConstants
import com.cbconnectit.utils.getProjectId
import com.cbconnectit.utils.receiveOrRespondWithError
import com.cbconnectit.utils.sendOk
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.projectRouting(projectController: ProjectController) {

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
                val insertNewProject = call.receiveOrRespondWithError<InsertNewProject>()
                val project = projectController.postProject(insertNewProject)
                call.respond(HttpStatusCode.Created, project)
            }

            put("{${ParamConstants.PROJECT_ID_KEY}}") {
                val projectId = call.getProjectId()
                val updateProject = call.receiveOrRespondWithError<UpdateProject>()
                val project = projectController.updateProjectById(projectId, updateProject)
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
