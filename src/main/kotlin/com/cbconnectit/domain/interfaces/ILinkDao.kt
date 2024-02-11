package com.cbconnectit.domain.interfaces

import com.cbconnectit.data.dto.requests.link.InsertNewLink
import com.cbconnectit.data.dto.requests.link.UpdateLink
import com.cbconnectit.domain.models.link.Link
import com.cbconnectit.domain.models.link.LinkType
import java.util.*

interface ILinkDao {

    fun getLinkById(id: UUID): Link?
    fun getLinks(): List<Link>
    fun insertLink(insertNewLink: InsertNewLink, linkType: LinkType): Link?
    fun updateLink(id: UUID, updateLink: UpdateLink, linkType: LinkType): Link?
    fun deleteLink(id: UUID): Boolean
    fun getListOfExistingLinkIds(linkIds: List<UUID>): List<UUID>

}