package com.cbconnectit.instrumentation

import com.cbconnectit.data.dto.requests.jobPosition.InsertNewJobPosition
import com.cbconnectit.data.dto.requests.jobPosition.UpdateJobPosition
import com.cbconnectit.domain.models.jobPosition.JobPosition
import java.time.LocalDateTime
import java.util.*

object JobPositionInstrumentation {

    fun givenAnInvalidInsertJobPosition() = InsertNewJobPosition("  ")
    fun givenAnInvalidUpdateJobPosition() = UpdateJobPosition("  ")
    fun givenAValidInsertJobPosition(name: String = "New Job Position") = InsertNewJobPosition(name)
    fun givenAValidUpdateJobPosition(name: String = "Updated Job Position") = UpdateJobPosition(name)

    fun givenJobPositionList() = listOf(
        givenAJobPosition(name = "First Job Position"),
        givenAJobPosition(name = "Second Job Position"),
        givenAJobPosition(name = "Third Job Position"),
        givenAJobPosition(name = "Fourth Job Position"),
    )

    fun givenAJobPosition(
        id: UUID = UUID.randomUUID(),
        name: String = "First Job Position"
    ) = JobPosition(
        id = id,
        name = name
    )
}
