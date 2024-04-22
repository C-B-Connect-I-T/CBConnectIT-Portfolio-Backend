package com.cbconnectit.domain.interfaces

import com.cbconnectit.data.dto.requests.project.InsertNewProject
import com.cbconnectit.data.dto.requests.project.UpdateProject
import com.cbconnectit.domain.models.project.Project
import java.util.*

interface IProjectDao {
    fun getProjectById(id: UUID): Project?
    fun getProjects(): List<Project>
    fun insertProject(insertNewProject: InsertNewProject): Project?
    fun updateProject(id: UUID, updateProject: UpdateProject): Project?
    fun deleteProject(id: UUID): Boolean
}
