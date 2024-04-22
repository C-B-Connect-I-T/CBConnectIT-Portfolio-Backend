package com.cbconnectit.data.database.dao

import com.cbconnectit.data.database.tables.LinksTable
import com.cbconnectit.data.database.tables.toLink
import com.cbconnectit.data.database.tables.toLinks
import com.cbconnectit.data.dto.requests.link.InsertNewLink
import com.cbconnectit.data.dto.requests.link.UpdateLink
import com.cbconnectit.domain.interfaces.ILinkDao
import com.cbconnectit.domain.models.link.Link
import com.cbconnectit.domain.models.link.LinkType
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.javatime.CurrentDateTime
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import java.util.*

class LinkDaoImpl : ILinkDao {
    override fun getLinkById(id: UUID): Link? =
        LinksTable.select { LinksTable.id eq id }.toLink()

    override fun getLinks(): List<Link> =
        LinksTable.selectAll().toLinks()

    override fun insertLink(insertNewLink: InsertNewLink, linkType: LinkType): Link? {
        val linkId = LinksTable.insertAndGetId {
            it[url] = insertNewLink.url
            it[type] = linkType
        }.value

        return getLinkById(linkId)
    }

    override fun updateLink(id: UUID, updateLink: UpdateLink, linkType: LinkType): Link? {
        LinksTable.update({ LinksTable.id eq id }) {
            it[url] = updateLink.url
            it[type] = linkType

            it[updatedAt] = CurrentDateTime
        }

        return getLinkById(id)
    }

    override fun deleteLink(id: UUID): Boolean = LinksTable.deleteWhere { LinksTable.id eq id } > 0

    override fun getListOfExistingLinkIds(linkIds: List<UUID>): List<UUID> =
        LinksTable.select { LinksTable.id inList linkIds }.map { it[LinksTable.id].value }
}
