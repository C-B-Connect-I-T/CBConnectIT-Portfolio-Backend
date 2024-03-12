package com.cbconnectit.data.database.dao

import com.cbconnectit.data.database.tables.*
import com.cbconnectit.data.dto.requests.experience.InsertNewExperience
import com.cbconnectit.data.dto.requests.experience.UpdateExperience
import com.cbconnectit.domain.interfaces.IExperienceDao
import com.cbconnectit.domain.models.experience.Experience
import com.cbconnectit.domain.models.tag.Tag
import com.cbconnectit.utils.toLocalDateTime
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.notInList
import java.time.LocalDateTime
import java.util.*

class ExperienceDaoImpl : IExperienceDao {

    override fun getExperienceById(id: UUID): Experience? {

        val experienceWithRelations = (ExperiencesTable leftJoin TagsExperiencesPivotTable leftJoin TagsTable) leftJoin JobPositionsTable leftJoin CompaniesTable

        val results = experienceWithRelations.select { ExperiencesTable.id eq id }
        val tags = parseTags(results)

        return results
            .distinctBy { it[ExperiencesTable.id].value }
            .map { row ->
                row.toExperience().copy(
                    tags = tags[id]?.distinctBy { it.id } ?: emptyList()
                )
            }
            .firstOrNull()
    }

    override fun getExperiences(): List<Experience> {
        val experienceWithRelations = (ExperiencesTable leftJoin TagsExperiencesPivotTable leftJoin TagsTable) leftJoin JobPositionsTable leftJoin CompaniesTable

        val results = experienceWithRelations.selectAll()
        val tags = parseTags(results)

        return results
            .distinctBy { it[ExperiencesTable.id].value }
            .map { row ->
                val id = row[CompaniesTable.id].value
                row.toExperience().copy(
                    tags = tags[id]?.distinctBy { it.id } ?: emptyList()
                )
            }
    }

    private fun parseTags(results: Query): MutableMap<UUID, List<Tag>> {
        val newMap = results
            .distinctBy { it.getOrNull(TagsTable.id)?.value }
            .fold(mutableMapOf<UUID, List<Tag>>()) { map, resultRow ->
                val experienceId = resultRow[ExperiencesTable.id].value

                val tag = if (resultRow.getOrNull(TagsTable.id) != null) {
                    resultRow.toTag()
                } else null

                val current = map.getOrDefault(experienceId, emptyList())
                map[experienceId] = current.toMutableList() + listOfNotNull(tag)
                map
            }

        return newMap
    }

    override fun insertExperience(insertNewExperience: InsertNewExperience): Experience? {
        val id = ExperiencesTable.insertAndGetId {
            it[shortDescription] = insertNewExperience.shortDescription
            it[description] = insertNewExperience.description
            it[from] = insertNewExperience.from.toLocalDateTime()
            it[to] = insertNewExperience.to.toLocalDateTime()
            it[jobPositionId] = UUID.fromString(insertNewExperience.jobPositionId)
            it[companyId] = UUID.fromString(insertNewExperience.companyId)
        }.value

        insertNewExperience.tags?.forEach { tagId ->
            TagsExperiencesPivotTable.insert {
                it[this.tagId] = UUID.fromString(tagId)
                it[experienceId] = id
            }
        }

        return getExperienceById(id)
    }

    override fun updateExperience(id: UUID, updateExperience: UpdateExperience): Experience? {
        ExperiencesTable.update({ ExperiencesTable.id eq id }) {
            it[shortDescription] = updateExperience.shortDescription
            it[description] = updateExperience.description
            it[from] = updateExperience.from.toLocalDateTime()
            it[to] = updateExperience.to.toLocalDateTime()
            it[jobPositionId] = UUID.fromString(updateExperience.jobPositionId)
            it[companyId] = UUID.fromString(updateExperience.companyId)

            it[updatedAt] = LocalDateTime.now()
        }

        TagsExperiencesPivotTable.deleteWhere {
            experienceId eq id and (tagId notInList (updateExperience.tags?.map { tagId -> UUID.fromString(tagId) }?: emptyList()))
        }

        updateExperience.tags?.forEach { tagId ->
            TagsExperiencesPivotTable.insert {
                it[this.tagId] = UUID.fromString(tagId)
                it[experienceId] = id
            }
        }

        return getExperienceById(id)
    }

    override fun deleteExperience(id: UUID): Boolean {
        val result = ExperiencesTable.deleteWhere { ExperiencesTable.id eq id } > 0
        val result2 = TagsExperiencesPivotTable.deleteWhere { experienceId eq id } > 0

        return result && result2
    }
}