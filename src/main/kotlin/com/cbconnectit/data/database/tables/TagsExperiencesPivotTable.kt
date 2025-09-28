package com.cbconnectit.data.database.tables

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table

object TagsExperiencesPivotTable: Table() {
    val tagId = reference("tag_id", TagsTable, ReferenceOption.CASCADE)
    val experienceId = reference("experience_id", ExperiencesTable, ReferenceOption.CASCADE)

    init {
        // Only a single pair can exist, duplicates are not allowed/necessary
        uniqueIndex(tagId, experienceId)
    }
}
