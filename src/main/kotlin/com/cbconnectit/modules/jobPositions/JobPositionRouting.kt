package com.cbconnectit.modules.jobPositions

import com.cbconnectit.data.dto.requests.jobPosition.InsertNewJobPosition
import com.cbconnectit.data.dto.requests.jobPosition.UpdateJobPosition
import com.cbconnectit.utils.ParamConstants
import com.cbconnectit.utils.getJobPositionId
import com.cbconnectit.utils.receiveOrRespondWithError
import com.cbconnectit.utils.sendOk
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.jobPositionRouting(jobPositionController: JobPositionController) {

    route("job_positions") {
        get {
            val jobPositions = jobPositionController.getJobPositions()
            call.respond(jobPositions)
        }

        get("/{${ParamConstants.JOB_POSITION_ID_KEY}}") {
            val jobPositionId = call.getJobPositionId()
            val jobPosition = jobPositionController.getJobPositionById(jobPositionId)
            call.respond(jobPosition)
        }

        authenticate {
            post {
                val insertNewJobPosition = call.receiveOrRespondWithError<InsertNewJobPosition>()
                val jobPosition = jobPositionController.postJobPosition(insertNewJobPosition)
                call.respond(HttpStatusCode.Created, jobPosition)
            }

            put("{${ParamConstants.JOB_POSITION_ID_KEY}}") {
                val jobPositionId = call.getJobPositionId()
                val updateJobPosition = call.receiveOrRespondWithError<UpdateJobPosition>()
                val jobPosition = jobPositionController.updateJobPositionById(jobPositionId, updateJobPosition)
                call.respond(jobPosition)
            }

            delete("{${ParamConstants.JOB_POSITION_ID_KEY}}") {
                val jobPositionId = call.getJobPositionId()
                jobPositionController.deleteJobPositionById(jobPositionId)
                sendOk()
            }
        }
    }
}
