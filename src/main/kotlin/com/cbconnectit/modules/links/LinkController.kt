package com.cbconnectit.modules.links

import com.cbconnectit.data.dto.requests.link.InsertNewLink
import com.cbconnectit.data.dto.requests.link.LinkDto
import com.cbconnectit.data.dto.requests.link.UpdateLink
import com.cbconnectit.domain.interfaces.ILinkDao
import com.cbconnectit.domain.models.link.LinkType
import com.cbconnectit.domain.models.link.toDto
import com.cbconnectit.plugins.dbQuery
import com.cbconnectit.statuspages.ErrorFailedCreate
import com.cbconnectit.statuspages.ErrorFailedDelete
import com.cbconnectit.statuspages.ErrorFailedUpdate
import com.cbconnectit.statuspages.ErrorInvalidParameters
import com.cbconnectit.statuspages.ErrorNotFound
import io.ktor.http.*
import java.util.*

class LinkControllerImpl(
    private val linkDao: ILinkDao
) : LinkController {

    override suspend fun getLinks(): List<LinkDto> = dbQuery {
        linkDao.getLinks().map { it.toDto() }
    }

    override suspend fun getLinkById(linkId: UUID): LinkDto = dbQuery {
        linkDao.getLinkById(linkId)?.toDto() ?: throw ErrorNotFound
    }

    override suspend fun postLink(insertNewLink: InsertNewLink): LinkDto = dbQuery {
        if (!insertNewLink.isValid) throw ErrorInvalidParameters

        val url = Url(insertNewLink.url)
        val linkType = LinkType.getTypeByUrl(url)

        linkDao.insertLink(insertNewLink, linkType)?.toDto() ?: throw ErrorFailedCreate
    }

    override suspend fun updateLinkById(linkId: UUID, updateLink: UpdateLink): LinkDto = dbQuery {
        if (!updateLink.isValid) throw ErrorInvalidParameters

        val url = Url(updateLink.url)
        val linkType = LinkType.getTypeByUrl(url)

        linkDao.updateLink(linkId, updateLink, linkType)?.toDto() ?: throw ErrorFailedUpdate
    }

    override suspend fun deleteLinkById(linkId: UUID) = dbQuery {
        val deleted = linkDao.deleteLink(linkId)
        if (!deleted) throw ErrorFailedDelete
    }
}

interface LinkController {
    suspend fun getLinks(): List<LinkDto>
    suspend fun getLinkById(linkId: UUID): LinkDto
    suspend fun postLink(insertNewLink: InsertNewLink): LinkDto
    suspend fun updateLinkById(linkId: UUID, updateLink: UpdateLink): LinkDto
    suspend fun deleteLinkById(linkId: UUID)
}
