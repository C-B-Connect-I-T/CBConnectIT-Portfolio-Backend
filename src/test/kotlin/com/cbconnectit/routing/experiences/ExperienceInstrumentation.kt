package com.cbconnectit.routing.experiences

import com.cbconnectit.data.dto.requests.experience.ExperienceDto
import com.cbconnectit.data.dto.requests.experience.InsertNewExperience
import com.cbconnectit.data.dto.requests.experience.UpdateExperience
import com.cbconnectit.utils.toDatabaseString
import java.time.LocalDateTime
import java.util.*

object ExperienceInstrumentation {
    fun givenAValidInsertExperience() = InsertNewExperience(
        shortDescription = "New Parent Experience",
        description = "",
        from = LocalDateTime.now().toDatabaseString(),
        to = LocalDateTime.now().toDatabaseString(),
        companyId = "00000000-0000-0000-0000-000000000001",
        jobPositionId = "00000000-0000-0000-0000-000000000001"
    )

    fun givenAValidUpdateExperienceBody() = UpdateExperience(
        shortDescription = "Updated Parent experience",
        description = "",
        from = LocalDateTime.now().toDatabaseString(),
        to = LocalDateTime.now().toDatabaseString(),
        companyId = "00000000-0000-0000-0000-000000000001",
        jobPositionId = "00000000-0000-0000-0000-000000000001"
    )

    fun givenAnEmptyInsertExperienceBody() =
        InsertNewExperience(
            shortDescription = "    ",
            description = "      ",
            from = LocalDateTime.now().toDatabaseString(),
            to = LocalDateTime.now().toDatabaseString(),
            companyId = "00000000-0000-0000-0000-000000000001",
            jobPositionId = "00000000-0000-0000-0000-000000000001"
        )

    fun givenExperienceList() = listOf(
        givenAExperience("First experience"),
        givenAExperience("Second experience"),
        givenAExperience("Third experience"),
        givenAExperience("Fourth experience"),
    )

    fun givenAExperience(shortDescription: String = "First experience") = run {
        val time = LocalDateTime.now().toDatabaseString()
        ExperienceDto(
            id = UUID.randomUUID().toString(),
            shortDescription = shortDescription,
            createdAt = time,
            updatedAt = time
        )
    }
}