package com.cbconnectit.data.database.dao.experiences

import com.cbconnectit.data.database.dao.BaseDaoTest
import com.cbconnectit.data.database.dao.ExperienceDaoImpl
import com.cbconnectit.data.database.dao.experiences.ExperienceInstrumentation.givenAValidInsertExperienceBody
import com.cbconnectit.data.database.dao.experiences.ExperienceInstrumentation.givenAValidUpdateExperienceBody
import com.cbconnectit.data.database.tables.CompaniesLinksPivotTable
import com.cbconnectit.data.database.tables.CompaniesTable
import com.cbconnectit.data.database.tables.ExperiencesTable
import com.cbconnectit.data.database.tables.JobPositionsTable
import com.cbconnectit.data.database.tables.TagsExperiencesPivotTable
import com.cbconnectit.data.database.tables.TagsTable
import com.cbconnectit.domain.models.company.Company
import com.cbconnectit.domain.models.experience.Experience
import com.cbconnectit.domain.models.jobPosition.JobPosition
import com.cbconnectit.domain.models.tag.Tag
import kotlinx.coroutines.delay
import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.insert
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.*
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class ExperienceDaoImplTest : BaseDaoTest() {

    private val dao = ExperienceDaoImpl()

    // <editor-fold desc="Get all Experiences">
    @Test
    fun `getExperiences but none exists, return empty list`() {
        withTables(
            CompaniesTable,
            CompaniesLinksPivotTable,
            JobPositionsTable,
            ExperiencesTable,
            TagsTable,
            TagsExperiencesPivotTable
        ) {
            val list = dao.getExperiences()
            assertThat(list).isEmpty()
        }
    }

    @Test
    fun `getExperiences return the list`() {
        baseTest {
            val list = dao.getExperiences()
            assertThat(list).hasSize(4)
        }
    }
    // </editor-fold>

    // <editor-fold desc="Get specific Experience by id">
    @Test
    fun `getExperience where item exists, return correct Experience`() {
        baseTest {
            val experience = dao.getExperienceById(UUID.fromString("00000000-0000-0000-0000-000000000001"))

            assertThat(experience).matches {
                it?.shortDescription == "First Experience"
            }
        }
    }

    @Test
    fun `getExperience where item does not exists, return 'null'`() {
        baseTest {
            val experience = dao.getExperienceById(UUID.randomUUID())

            assertNull(experience)
        }
    }
    // </editor-fold>

    // <editor-fold desc="Create new Experience">
    @Test
    fun `insertExperience where information is correct, database is storing Experience and returning correct content`() {
        baseTest {
            val validExperience = givenAValidInsertExperienceBody()
            val experience = dao.insertExperience(validExperience)

            assertThat(experience).matches {
                it?.shortDescription == validExperience.shortDescription &&
                        it.createdAt == it.updatedAt
            }
        }
    }
    // </editor-fold>

    // <editor-fold desc="Update Experience">
    @Test
    fun `updateExperience where information is correct, database is storing information and returning the correct content`() {
        baseTest {
            // adding a delay so there is a clear difference between `updatedAt` and `createdAt`
            delay(1000)

            val validUpdateExperience = givenAValidUpdateExperienceBody()
            val experience = dao.updateExperience(UUID.fromString("00000000-0000-0000-0000-000000000001"), validUpdateExperience)

            assertThat(experience).matches {
                it?.shortDescription == validUpdateExperience.shortDescription &&
                        it.createdAt != it.updatedAt
            }
        }
    }

    @Test
    fun `updateExperience where information is correct but Experience with id does not exist, database does nothing and returns 'null'`() {
        baseTest {
            val validExperience = givenAValidUpdateExperienceBody()
            val experience = dao.updateExperience(UUID.randomUUID(), validExperience)

            assertNull(experience)
        }
    }
    // </editor-fold>

    // <editor-fold desc="Delete Experience">
    @Test
    fun `deleteExperience for id that exists, return true`() {
        baseTest {
            val deleted = dao.deleteExperience(UUID.fromString("00000000-0000-0000-0000-000000000001"))
            assertTrue(deleted)
        }
    }

    @Test
    fun `deleteExperience for id that does not exist, return false`() {
        baseTest {
            val deleted = dao.deleteExperience(UUID.randomUUID())
            assertFalse(deleted)
        }
    }
    // </editor-fold>

    @SuppressWarnings("LongMethod")
    private fun baseTest(
        test: suspend Transaction.() -> Unit
    ) {
        withTables(
            CompaniesTable,
            CompaniesLinksPivotTable,
            JobPositionsTable,
            ExperiencesTable,
            TagsTable,
            TagsExperiencesPivotTable
        ) {
            listOf(
                Company(UUID.fromString("00000000-0000-0000-0000-000000000001"), name = "First company"),
                Company(UUID.fromString("00000000-0000-0000-0000-000000000002"), name = "Second company"),
            ).forEach { data ->
                CompaniesTable.insert {
                    it[id] = data.id
                    it[name] = data.name
                }
            }

            listOf(
                JobPosition(UUID.fromString("00000000-0000-0000-0000-000000000001"), name = "First jobPosition"),
                JobPosition(UUID.fromString("00000000-0000-0000-0000-000000000002"), name = "Second jobPosition"),
            ).forEach { data ->
                JobPositionsTable.insert {
                    it[id] = data.id
                    it[name] = data.name
                }
            }

            listOf(
                Tag(UUID.fromString("00000000-0000-0000-0000-000000000001"), name = "First Tag", "first-tag"),
                Tag(UUID.fromString("00000000-0000-0000-0000-000000000002"), name = "Second Tag", "second-tag"),
                Tag(UUID.fromString("00000000-0000-0000-0000-000000000003"), name = "Third Tag", "third-tag"),
            ).forEach { data ->
                TagsTable.insert {
                    it[id] = data.id
                    it[name] = data.name
                    it[slug] = data.slug
                }
            }

            listOf(
                Pair(UUID.fromString("00000000-0000-0000-0000-000000000001"), UUID.fromString("00000000-0000-0000-0000-000000000001")),
                Pair(UUID.fromString("00000000-0000-0000-0000-000000000002"), UUID.fromString("00000000-0000-0000-0000-000000000001")),
                Pair(UUID.fromString("00000000-0000-0000-0000-000000000003"), UUID.fromString("00000000-0000-0000-0000-000000000002")),
            ).forEach { data ->
                TagsExperiencesPivotTable.insert {
                    it[tagId] = data.first
                    it[experienceId] = data.second
                }
            }

            listOf(
                Experience(
                    id = UUID.fromString("00000000-0000-0000-0000-000000000001"),
                    shortDescription = "First Experience",
                    company = Company(UUID.fromString("00000000-0000-0000-0000-000000000001")),
                    jobPosition = JobPosition(UUID.fromString("00000000-0000-0000-0000-000000000002"))
                ),
                Experience(
                    id = UUID.fromString("00000000-0000-0000-0000-000000000002"),
                    shortDescription = "Second Experience",
                    company = Company(UUID.fromString("00000000-0000-0000-0000-000000000002")),
                    jobPosition = JobPosition(UUID.fromString("00000000-0000-0000-0000-000000000001"))
                ),
                Experience(
                    id = UUID.fromString("00000000-0000-0000-0000-000000000003"),
                    shortDescription = "Third Experience",
                    company = Company(UUID.fromString("00000000-0000-0000-0000-000000000001")),
                    jobPosition = JobPosition(UUID.fromString("00000000-0000-0000-0000-000000000002"))
                ),
                Experience(
                    id = UUID.fromString("00000000-0000-0000-0000-000000000004"),
                    shortDescription = "Fourth Experience",
                    company = Company(UUID.fromString("00000000-0000-0000-0000-000000000002")),
                    jobPosition = JobPosition(UUID.fromString("00000000-0000-0000-0000-000000000001"))
                )
            ).forEach { data ->
                ExperiencesTable.insert {
                    it[id] = data.id
                    it[shortDescription] = data.shortDescription
                    it[companyId] = data.company.id
                    it[jobPositionId] = data.jobPosition.id
                    it[description] = ""
                    it[to] = LocalDateTime.now()
                    it[from] = LocalDateTime.now()
                }
            }

            test()
        }
    }
}
