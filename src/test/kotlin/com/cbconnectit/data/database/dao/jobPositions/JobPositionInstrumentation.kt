package com.cbconnectit.data.database.dao.jobPositions

import com.cbconnectit.data.dto.requests.jobPosition.InsertNewJobPosition
import com.cbconnectit.data.dto.requests.jobPosition.UpdateJobPosition

object JobPositionInstrumentation {

    fun givenAValidInsertJobPositionBody() = InsertNewJobPosition("First jobPosition")
    fun givenAValidSecondInsertJobPositionBody() = InsertNewJobPosition("Second jobPosition")

    fun givenAValidUpdateJobPositionBody() = UpdateJobPosition("Updated jobPosition")

    fun givenAnEmptyUpdateJobPositionBody() = UpdateJobPosition("   ")
    fun givenAnUnknownJobPosition() = InsertNewJobPosition("Unknown")
}