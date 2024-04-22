package com.cbconnectit.controllers.experiences

import com.cbconnectit.data.dto.requests.experience.InsertNewExperience
import com.cbconnectit.data.dto.requests.experience.UpdateExperience
import com.cbconnectit.domain.models.experience.Experience
import com.cbconnectit.utils.toDatabaseString
import java.time.LocalDateTime
import java.util.*

object ExperienceInstrumentation {

    fun givenAnInvalidInsertExperience() = InsertNewExperience(
        shortDescription = "  ",
        "",
        from = LocalDateTime.now().toDatabaseString(),
        to = LocalDateTime.now().toDatabaseString(),
        companyId = "00000000-0000-0000-0000-000000000001",
        jobPositionId = "00000000-0000-0000-0000-000000000001"
    )

    fun givenAnInvalidUpdateExperience() = UpdateExperience(
        shortDescription = "  ",
        "",
        from = LocalDateTime.now().toDatabaseString(),
        to = LocalDateTime.now().toDatabaseString(),
        companyId = "00000000-0000-0000-0000-000000000001",
        jobPositionId = "00000000-0000-0000-0000-000000000001"
    )

    fun givenAValidInsertExperience() = InsertNewExperience(
        shortDescription = "New experience",
        description = "First Experience",
        from = LocalDateTime.now().toDatabaseString(),
        to = LocalDateTime.now().toDatabaseString(),
        companyId = "00000000-0000-0000-0000-000000000001",
        jobPositionId = "00000000-0000-0000-0000-000000000001",
        tags = listOf("00000000-0000-0000-0000-000000000001")
    )

    fun givenAValidUpdateExperience() = UpdateExperience(
        shortDescription = "Updated experience",
        description = "Updated experience",
        from = LocalDateTime.now().toDatabaseString(),
        to = LocalDateTime.now().toDatabaseString(),
        companyId = "00000000-0000-0000-0000-000000000001",
        jobPositionId = "00000000-0000-0000-0000-000000000001",
        tags = listOf("00000000-0000-0000-0000-000000000002")
    )

    fun givenExperienceList() = listOf(
        givenAExperience(id = UUID.fromString("00000000-0000-0000-0000-000000000001"), shortDescription = "First Experience"),
        givenAExperience(id = UUID.fromString("00000000-0000-0000-0000-000000000002"), shortDescription = "Second Experience"),
        givenAExperience(id = UUID.fromString("00000000-0000-0000-0000-000000000001"), shortDescription = "Third Experience"),
        givenAExperience(id = UUID.fromString("00000000-0000-0000-0000-000000000001"), shortDescription = "Fourth Experience"),
    )

    fun givenAExperience(id: UUID = UUID.randomUUID(), shortDescription: String = "Experience") = Experience(id = id, shortDescription = shortDescription)
}
