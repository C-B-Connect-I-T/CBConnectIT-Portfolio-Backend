package com.cbconnectit.data.database.dao

import com.cbconnectit.data.database.tables.JobPositionsTable
import com.cbconnectit.data.database.tables.LinksTable
import com.cbconnectit.data.database.tables.toJobPosition
import com.cbconnectit.data.database.tables.toJobPositions
import com.cbconnectit.data.dto.requests.jobPosition.InsertNewJobPosition
import com.cbconnectit.data.dto.requests.jobPosition.UpdateJobPosition
import com.cbconnectit.domain.interfaces.IJobPositionDao
import com.cbconnectit.domain.models.jobPosition.JobPosition
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.javatime.CurrentDateTime
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import java.util.*

class JobPositionDaoImpl : IJobPositionDao {
    override fun getJobPositionById(id: UUID): JobPosition? =
        JobPositionsTable.selectAll().where { JobPositionsTable.id eq id }.toJobPosition()

    override fun getJobPositions(): List<JobPosition> =
        JobPositionsTable.selectAll().toJobPositions()

    override fun insertJobPosition(insertNewJobPosition: InsertNewJobPosition): JobPosition? {
        val id = JobPositionsTable.insertAndGetId {
            it[name] = insertNewJobPosition.name
        }.value

        return getJobPositionById(id)
    }

    override fun updateJobPosition(id: UUID, updateJobPosition: UpdateJobPosition): JobPosition? {
        JobPositionsTable.update({ JobPositionsTable.id eq id }) {
            it[name] = updateJobPosition.name

            it[LinksTable.updatedAt] = CurrentDateTime
        }

        return getJobPositionById(id)
    }

    override fun deleteJobPosition(id: UUID): Boolean = JobPositionsTable.deleteWhere { JobPositionsTable.id eq id } > 0

    override fun jobPositionUnique(name: String): Boolean =
        JobPositionsTable.selectAll().where { JobPositionsTable.name eq name }.empty()

    override fun getListOfExistingJobPositionIds(jobPositionIds: List<UUID>): List<UUID> =
        JobPositionsTable.selectAll().where { JobPositionsTable.id inList jobPositionIds }.map { it[JobPositionsTable.id].value }
}
