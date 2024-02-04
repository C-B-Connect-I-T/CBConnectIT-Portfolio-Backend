package com.cbconnectit.data.database.tables

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table

object LinksProjectsPivotTable: Table() {
    val linkId = reference("link_id", LinksTable, ReferenceOption.CASCADE)
    val projectId = reference("project_id", ProjectsTable, ReferenceOption.CASCADE)
}