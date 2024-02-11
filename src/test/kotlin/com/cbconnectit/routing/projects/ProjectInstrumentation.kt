package com.cbconnectit.routing.projects

import com.cbconnectit.data.dto.requests.project.InsertNewProject
import com.cbconnectit.data.dto.requests.project.ProjectDto
import com.cbconnectit.data.dto.requests.project.UpdateProject
import com.cbconnectit.utils.toDatabaseString
import java.time.LocalDateTime
import java.util.*

object ProjectInstrumentation {
    fun givenAValidInsertProject() = InsertNewProject(title = "New Project", description = "Project description", shortDescription = "project short description", links = listOf(), tags = listOf())
    fun givenAValidUpdateProjectBody() = UpdateProject(title = "Updated project", description = "Project description", shortDescription = "project short description", links = listOf(), tags = listOf())

    fun givenAnEmptyInsertProjectBody() = InsertNewProject(title = "    ", description = "", shortDescription = "", links = listOf(), tags = listOf())

    fun givenProjectList() = listOf(
        givenAProject("First project"),
        givenAProject("Second project"),
        givenAProject("Third project"),
        givenAProject("Fourth project"),
    )

    fun givenAProject(title: String = "First project") = run {
        val time = LocalDateTime.now().toDatabaseString()
        ProjectDto(
            id = UUID.randomUUID().toString(),
            title = title,
            description = "Project description",
            shortDescription = "Project short description",
            createdAt = time,
            updatedAt = time
        )
    }
}