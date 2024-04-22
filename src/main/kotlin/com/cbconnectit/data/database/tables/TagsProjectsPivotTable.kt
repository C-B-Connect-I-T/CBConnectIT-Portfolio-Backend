package com.cbconnectit.data.database.tables

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table

object TagsProjectsPivotTable: Table() {
    val tagId = reference("tag_id", TagsTable, ReferenceOption.CASCADE)
    val projectId = reference("project_id", ProjectsTable, ReferenceOption.CASCADE)
}
