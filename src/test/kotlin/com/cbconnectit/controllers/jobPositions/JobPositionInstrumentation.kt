package com.cbconnectit.controllers.jobPositions

import com.cbconnectit.data.dto.requests.jobPosition.InsertNewJobPosition
import com.cbconnectit.data.dto.requests.jobPosition.UpdateJobPosition
import com.cbconnectit.domain.models.jobPosition.JobPosition
import java.util.*

object JobPositionInstrumentation {

    fun givenAnInvalidInsertJobPosition() = InsertNewJobPosition("  ")
    fun givenAnInvalidUpdateJobPosition() = UpdateJobPosition("  ")
    fun givenAValidInsertJobPosition() = InsertNewJobPosition("New Job Position")
    fun givenAValidUpdateJobPosition() = UpdateJobPosition("Updated Job Position")

    fun givenJobPositionList() = listOf(
        givenAJobPosition("First Job Position"),
        givenAJobPosition("Second Job Position"),
        givenAJobPosition("Third Job Position"),
        givenAJobPosition("Fourth Job Position"),
    )

    fun givenAJobPosition(name: String = "First Job Position") = JobPosition(UUID.randomUUID(), name)
}