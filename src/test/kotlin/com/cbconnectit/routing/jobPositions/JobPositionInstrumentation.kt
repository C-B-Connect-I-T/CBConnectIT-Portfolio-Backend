package com.cbconnectit.routing.jobPositions

import com.cbconnectit.data.dto.requests.jobPosition.InsertNewJobPosition
import com.cbconnectit.data.dto.requests.jobPosition.JobPositionDto
import com.cbconnectit.data.dto.requests.jobPosition.UpdateJobPosition
import com.cbconnectit.utils.toDatabaseString
import java.time.LocalDateTime
import java.util.*

object JobPositionInstrumentation {
    fun givenAValidInsertJobPosition() = InsertNewJobPosition("First jobPosition")
    fun givenAValidUpdateJobPositionBody() = UpdateJobPosition("Updated JobPosition")

    fun givenAnEmptyInsertJobPositionBody() = InsertNewJobPosition("    ")

    fun givenJobPositionList() = listOf(
        givenAJobPosition("JobPosition no. 1"),
        givenAJobPosition("JobPosition no. 2"),
        givenAJobPosition("JobPosition no. 3"),
        givenAJobPosition("Unknown"),
    )

    fun givenAJobPosition(name: String = "First jobPosition") = run {
        val time = LocalDateTime.now().toDatabaseString()
        JobPositionDto(
            id = UUID.randomUUID().toString(),
            name = name,
            createdAt = time,
            updatedAt = time
        )
    }
}
