package com.cbconnectit.domain.interfaces

import com.cbconnectit.data.dto.requests.experience.InsertNewExperience
import com.cbconnectit.data.dto.requests.experience.UpdateExperience
import com.cbconnectit.domain.models.experience.Experience
import java.util.*

interface IExperienceDao {

    fun getExperienceById(id: UUID): Experience?
    fun getExperiences(): List<Experience>
    fun insertExperience(insertNewExperience: InsertNewExperience): Experience?
    fun updateExperience(id: UUID, updateExperience: UpdateExperience): Experience?
    fun deleteExperience(id: UUID): Boolean
}
