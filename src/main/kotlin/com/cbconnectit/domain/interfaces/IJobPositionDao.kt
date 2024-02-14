package com.cbconnectit.domain.interfaces

import com.cbconnectit.data.dto.requests.jobPosition.InsertNewJobPosition
import com.cbconnectit.data.dto.requests.jobPosition.UpdateJobPosition
import com.cbconnectit.domain.models.jobPosition.JobPosition
import java.util.*

interface IJobPositionDao {

    fun getJobPositionById(id: UUID): JobPosition?
    fun getJobPositions(): List<JobPosition>
    fun insertJobPosition(insertNewJobPosition: InsertNewJobPosition): JobPosition?
    fun updateJobPosition(id: UUID, updateJobPosition: UpdateJobPosition): JobPosition?
    fun deleteJobPosition(id: UUID): Boolean
    fun jobPositionUnique(name: String): Boolean
    fun getListOfExistingJobPositionIds(jobPositionIds: List<UUID>): List<UUID>
}