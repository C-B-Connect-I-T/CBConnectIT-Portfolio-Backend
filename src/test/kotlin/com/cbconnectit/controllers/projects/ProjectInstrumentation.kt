package com.cbconnectit.controllers.projects

import com.cbconnectit.data.dto.requests.project.InsertNewProject
import com.cbconnectit.data.dto.requests.project.UpdateProject
import com.cbconnectit.domain.models.project.Project
import java.util.*

object ProjectInstrumentation {

    fun givenAnInvalidInsertProject() = InsertNewProject(title = "  ", description = "Updated description", shortDescription = "Updated short description")
    fun givenAnInvalidUpdateProject() = UpdateProject(title = "  ", description = "Updated description", shortDescription = "Updated short description")
    fun givenAValidInsertProject() = InsertNewProject(title = "New project", description = "Updated description", shortDescription = "Updated short description", tags = listOf("00000000-0000-0000-0000-000000000001"), links = listOf("00000000-0000-0000-0000-000000000001"))
    fun givenAValidUpdateProject() = UpdateProject(title = "Updated project", description = "Updated description", shortDescription = "Updated short description", tags = listOf("00000000-0000-0000-0000-000000000002"), links = listOf("00000000-0000-0000-0000-000000000002"))

    fun givenProjectList() = listOf(
        givenAProject(id = UUID.fromString("00000000-0000-0000-0000-000000000001"), title = "First Project"),
        givenAProject(id = UUID.fromString("00000000-0000-0000-0000-000000000002"), title = "Second Project"),
        givenAProject(id = UUID.fromString("00000000-0000-0000-0000-000000000003"), title = "Third Project"),
        givenAProject(id = UUID.fromString("00000000-0000-0000-0000-000000000004"), title = "Fourth Project"),
    )

    fun givenAProject(id: UUID = UUID.randomUUID(), title: String = "First Project") = Project(id = id, title = title)
}