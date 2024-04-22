package com.cbconnectit.data.database.dao

import com.cbconnectit.data.database.tables.CompaniesLinksPivotTable
import com.cbconnectit.data.database.tables.CompaniesTable
import com.cbconnectit.data.database.tables.ExperiencesTable
import com.cbconnectit.data.database.tables.JobPositionsTable
import com.cbconnectit.data.database.tables.LinksTable
import com.cbconnectit.data.database.tables.TagsExperiencesPivotTable
import com.cbconnectit.data.database.tables.TagsTable
import com.cbconnectit.data.database.tables.parseLinks
import com.cbconnectit.data.database.tables.parseTags
import com.cbconnectit.data.database.tables.toExperience
import com.cbconnectit.data.dto.requests.experience.InsertNewExperience
import com.cbconnectit.data.dto.requests.experience.UpdateExperience
import com.cbconnectit.domain.interfaces.IExperienceDao
import com.cbconnectit.domain.models.experience.Experience
import com.cbconnectit.domain.models.link.Link
import com.cbconnectit.domain.models.tag.Tag
import com.cbconnectit.utils.toLocalDateTime
import org.jetbrains.exposed.sql.Query
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.notInList
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import java.time.LocalDateTime
import java.util.*

class ExperienceDaoImpl : IExperienceDao {

    override fun getExperienceById(id: UUID): Experience? {

        val experienceWithRelations = ExperiencesTable leftJoin TagsExperiencesPivotTable leftJoin TagsTable leftJoin JobPositionsTable leftJoin CompaniesTable leftJoin CompaniesLinksPivotTable leftJoin LinksTable

        val results = experienceWithRelations.select { ExperiencesTable.id eq id }
        val tags = parseTags(results)
        val links = parseLinks(results)

        return results
            .distinctBy { it[ExperiencesTable.id].value }
            .map { row ->
                val temp = row.toExperience()
                temp.copy(
                    tags = tags[id]?.distinctBy { it.id } ?: emptyList(),
                    company = temp.company.copy(
                        links = links[id]?.distinctBy { it.id } ?: emptyList()
                    )
                )
            }
            .firstOrNull()
    }

    override fun getExperiences(): List<Experience> {
        val experienceWithRelations = ExperiencesTable leftJoin JobPositionsTable leftJoin(TagsExperiencesPivotTable leftJoin TagsTable) leftJoin CompaniesTable leftJoin(CompaniesLinksPivotTable leftJoin LinksTable)

        val results = experienceWithRelations.selectAll()
        val tags = parseTags(results)
        val links = parseLinks(results)

        // TODO: getting to many relations is "messing" up the parsing for tags and links. (to many rows)... should be fixed because of to many repetitions

        return results
            .distinctBy { it[ExperiencesTable.id].value }
            .map { row ->
                val id = row[ExperiencesTable.id].value
                val companyId = row[CompaniesTable.id].value
                val temp = row.toExperience()
                temp.copy(
                    tags = tags[id]?.distinctBy { it.id } ?: emptyList(),
                    company = temp.company.copy(
                        links = links[companyId]?.distinctBy { it.id } ?: emptyList()
                    )
                )
            }
    }

    private fun parseTags(results: Query): MutableMap<UUID, List<Tag>> {
        return parseTags(results) {
            it[ExperiencesTable.id].value
        }
    }

    private fun parseLinks(results: Query): MutableMap<UUID, List<Link>> {
        return parseLinks(results) {
            it[CompaniesTable.id].value
        }
    }

    override fun insertExperience(insertNewExperience: InsertNewExperience): Experience? {
        val id = ExperiencesTable.insertAndGetId {
            it[shortDescription] = insertNewExperience.shortDescription
            it[description] = insertNewExperience.description
            it[from] = insertNewExperience.from.toLocalDateTime()
            it[to] = insertNewExperience.to.toLocalDateTime()
            it[asFreelance] = insertNewExperience.asFreelance
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
            it[asFreelance] = updateExperience.asFreelance
            it[jobPositionId] = UUID.fromString(updateExperience.jobPositionId)
            it[companyId] = UUID.fromString(updateExperience.companyId)

            it[updatedAt] = LocalDateTime.now()
        }

        TagsExperiencesPivotTable.deleteWhere {
            experienceId eq id and (tagId notInList (updateExperience.tags?.map { tagId -> UUID.fromString(tagId) } ?: emptyList()))
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
