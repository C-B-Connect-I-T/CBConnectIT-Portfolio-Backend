package com.cbconnectit.data.database.tables

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table

object CompaniesLinksPivotTable: Table() {
    val linkId = reference("link_id", LinksTable, ReferenceOption.CASCADE)
    val companyId = reference("company_id", CompaniesTable, ReferenceOption.CASCADE)
}