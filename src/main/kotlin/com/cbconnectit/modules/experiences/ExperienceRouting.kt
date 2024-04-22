package com.cbconnectit.modules.experiences

import com.cbconnectit.data.dto.requests.experience.InsertNewExperience
import com.cbconnectit.data.dto.requests.experience.UpdateExperience
import com.cbconnectit.utils.ParamConstants
import com.cbconnectit.utils.getExperienceId
import com.cbconnectit.utils.receiveOrRespondWithError
import com.cbconnectit.utils.sendOk
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.experienceRouting() {

    val experienceController by inject<ExperienceController>()

    route("experiences") {
        get {
            val experiences = experienceController.getExperiences()
            call.respond(experiences)
        }

        get("/{${ParamConstants.EXPERIENCE_ID_KEY}}") {
            val experienceIdentifier = call.getExperienceId()
            val experience = experienceController.getExperienceById(experienceIdentifier)
            call.respond(experience)
        }

        authenticate {
            post {
                val insertNewExperience = call.receiveOrRespondWithError<InsertNewExperience>()
                val experience = experienceController.postExperience(insertNewExperience)
                call.respond(HttpStatusCode.Created, experience)
            }

            put("{${ParamConstants.EXPERIENCE_ID_KEY}}") {
                val experienceId = call.getExperienceId()
                val updateExperience = call.receiveOrRespondWithError<UpdateExperience>()
                val experience = experienceController.updateExperienceById(experienceId, updateExperience)
                call.respond(experience)
            }

            delete("{${ParamConstants.EXPERIENCE_ID_KEY}}") {
                val experienceId = call.getExperienceId()
                experienceController.deleteExperienceById(experienceId)
                sendOk()
            }
        }
    }
}
