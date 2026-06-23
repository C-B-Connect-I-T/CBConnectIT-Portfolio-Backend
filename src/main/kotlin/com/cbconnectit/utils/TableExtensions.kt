package com.cbconnectit.utils

import com.cbconnectit.data.database.tables.MediaFilesTable
import com.cbconnectit.domain.models.mediafile.OwnerType
import org.jetbrains.exposed.sql.ColumnSet
import org.jetbrains.exposed.sql.Expression
import org.jetbrains.exposed.sql.leftJoin

fun <C1 : ColumnSet> C1.leftJoinMediaFiles(
    onColumn: (C1.() -> Expression<*>)? = null,
    ownerType: OwnerType
) = this.leftJoin(
    MediaFilesTable,
    onColumn,
    { ownerId },
    additionalConstraint = {
        MediaFilesTable.ownerType eq ownerType
    }
)
