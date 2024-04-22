package com.cbconnectit.data.database.dao.projects

import com.cbconnectit.data.dto.requests.project.InsertNewProject
import com.cbconnectit.data.dto.requests.project.UpdateProject

object ProjectInstrumentation {

    fun givenAValidInsertProjectBody() = InsertNewProject(title = "First Project", description = "", shortDescription = "", links = listOf("00000000-0000-0000-0000-000000000001"), tags = listOf("00000000-0000-0000-0000-000000000001"))
    fun givenAValidSecondInsertProjectBody() = InsertNewProject(title = "Second Project", description = "", shortDescription = "", links = listOf("00000000-0000-0000-0000-000000000001"), tags = listOf("00000000-0000-0000-0000-000000000001"))

    fun givenAValidUpdateProjectBody() = UpdateProject(title = "Updated Project", description = "", shortDescription = "", links = listOf("00000000-0000-0000-0000-000000000003"), tags = listOf("00000000-0000-0000-0000-000000000003"))

    fun givenAnEmptyUpdateProjectBody() = UpdateProject(title = "   ", description = "", shortDescription = "", links = listOf("00000000-0000-0000-0000-000000000001"), tags = listOf("00000000-0000-0000-0000-000000000001"))
    fun givenAnUnknownProject() = InsertNewProject(title = "Unknown", description = "", shortDescription = "", links = listOf("00000000-0000-0000-0000-000000000001"), tags = listOf("00000000-0000-0000-0000-000000000001"))
}
