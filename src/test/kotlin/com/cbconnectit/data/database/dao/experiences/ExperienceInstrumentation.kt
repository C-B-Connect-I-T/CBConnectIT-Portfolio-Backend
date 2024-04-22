package com.cbconnectit.data.database.dao.experiences

import com.cbconnectit.data.dto.requests.experience.InsertNewExperience
import com.cbconnectit.data.dto.requests.experience.UpdateExperience
import com.cbconnectit.utils.toDatabaseString
import java.time.LocalDateTime

object ExperienceInstrumentation {

    fun givenAValidInsertExperienceBody() = InsertNewExperience(
        shortDescription = "New Experience",
        description = "",
        from = LocalDateTime.now().toDatabaseString(),
        to = LocalDateTime.now().toDatabaseString(),
        jobPositionId = "00000000-0000-0000-0000-000000000001",
        companyId = "00000000-0000-0000-0000-000000000001",
        tags = listOf("00000000-0000-0000-0000-000000000001")
    )

    fun givenAValidSecondInsertExperienceBody() = InsertNewExperience(
        shortDescription = "Second Experience",
        description = "",
        from = LocalDateTime.now().toDatabaseString(),
        to = LocalDateTime.now().toDatabaseString(),
        jobPositionId = "00000000-0000-0000-0000-000000000001",
        companyId = "00000000-0000-0000-0000-000000000001"
    )

    fun givenAValidUpdateExperienceBody() = UpdateExperience(
        shortDescription = "Updated Experience",
        description = "",
        from = LocalDateTime.now().toDatabaseString(),
        to = LocalDateTime.now().toDatabaseString(),
        jobPositionId = "00000000-0000-0000-0000-000000000001",
        companyId = "00000000-0000-0000-0000-000000000001",
        tags = listOf("00000000-0000-0000-0000-000000000002")
    )

    fun givenAnEmptyUpdateExperienceBody() = UpdateExperience(
        shortDescription = "   ",
        description = "",
        from = LocalDateTime.now().toDatabaseString(),
        to = LocalDateTime.now().toDatabaseString(),
        jobPositionId = "00000000-0000-0000-0000-000000000001",
        companyId = "00000000-0000-0000-0000-000000000001"
    )

    fun givenAnUnknownExperience() = InsertNewExperience(
        shortDescription = "Unknown",
        description = "",
        from = LocalDateTime.now().toDatabaseString(),
        to = LocalDateTime.now().toDatabaseString(),
        jobPositionId = "00000000-0000-0000-0000-000000000001",
        companyId = "00000000-0000-0000-0000-000000000001"
    )
}
