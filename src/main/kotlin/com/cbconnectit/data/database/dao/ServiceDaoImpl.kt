package com.cbconnectit.data.database.dao

import com.cbconnectit.data.database.tables.MediaFilesTable
import com.cbconnectit.data.database.tables.ServicesTable
import com.cbconnectit.data.database.tables.TagsTable
import com.cbconnectit.data.database.tables.toMediaFile
import com.cbconnectit.data.database.tables.toService
import com.cbconnectit.data.database.tables.toTag
import com.cbconnectit.data.dto.requests.service.InsertNewService
import com.cbconnectit.data.dto.requests.service.UpdateService
import com.cbconnectit.domain.interfaces.IServiceDao
import com.cbconnectit.domain.models.mediafile.MediaFile
import com.cbconnectit.domain.models.mediafile.MediaType
import com.cbconnectit.domain.models.mediafile.OwnerType
import com.cbconnectit.domain.models.service.CompactService
import com.cbconnectit.domain.models.service.Service
import com.cbconnectit.domain.models.service.ServiceAdminItem
import com.cbconnectit.domain.models.service.groupedAndSorted
import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.alias
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.time.LocalDateTime
import java.util.*

class ServiceDaoImpl : IServiceDao {

    override fun getServiceById(id: UUID): Service? =
        fetchServicesWithSubServices(id)

    override fun getServices(): List<Service> =
        fetchServicesRecursive(null)

    override fun getServicesOverview(): List<ServiceAdminItem> {
        val parentAlias = ServicesTable.alias("parent_services")
        val parentIdCol = parentAlias[ServicesTable.id]
        val parentTitleCol = parentAlias[ServicesTable.title]

        val items = transaction {
            (ServicesTable leftJoin TagsTable)
                .join(parentAlias, JoinType.LEFT, ServicesTable.parentServiceId, parentIdCol)
                .selectAll()
                .map { row ->
                    ServiceAdminItem(
                        id = row[ServicesTable.id].value,
                        title = row[ServicesTable.title],
                        parentService = row[ServicesTable.parentServiceId]?.value?.let { parentId ->
                            CompactService(
                                id = parentId,
                                title = row[parentTitleCol]
                            )
                        },
                        tag = row[ServicesTable.tagId]?.value?.let { row.toTag() },
                        updatedAt = row[ServicesTable.updatedAt]
                    )
                }
        }
        return items.groupedAndSorted()
    }

    private fun fetchServicesWithSubServices(id: UUID): Service? {
        var service: Service? = null

        transaction {
            service = (ServicesTable leftJoin TagsTable).selectAll().where { ServicesTable.id eq id }
                .toService()

            val subServices = fetchServicesRecursive(id)
            service = service?.copy(subServices = subServices)
        }

        return service?.let { withMedia(listOf(it)).first() }
    }

    private fun fetchServicesRecursive(parentId: UUID? = null): List<Service> {
        val subService = mutableListOf<Service>()

        transaction {
            val services = (ServicesTable leftJoin TagsTable).selectAll().where { ServicesTable.parentServiceId eq parentId }
                .map { it.toService() }

            services.forEach { childService ->
                val grandSubServices = fetchServicesRecursive(childService.id)
                subService.add(childService.copy(subServices = grandSubServices.ifEmpty { null }))
            }
        }

        return withMedia(subService)
    }

    private fun withMedia(services: List<Service>): List<Service> {
        if (services.isEmpty()) return services
        val ids = services.map { it.id }

        val mediaByOwner: Map<UUID, List<MediaFile>> = MediaFilesTable
            .selectAll()
            .where {
                (MediaFilesTable.ownerId inList ids) and
                        (MediaFilesTable.ownerType eq OwnerType.SERVICE)
            }
            .map { it.toMediaFile() }
            .groupBy { it.ownerId }

        return services.map { service ->
            val files = mediaByOwner[service.id] ?: emptyList()
            service.copy(
                image = files.firstOrNull { it.mediaType == MediaType.IMAGE },
                bannerImage = files.firstOrNull { it.mediaType == MediaType.BANNER }
            )
        }
    }

    override fun insertService(insertNewService: InsertNewService): Service? {
        val id = ServicesTable.insertAndGetId {
            it[title] = insertNewService.title
            it[description] = insertNewService.description
            it[bannerDescription] = insertNewService.bannerDescription
            it[shortDescription] = insertNewService.shortDescription
            it[extraInfo] = insertNewService.extraInfo
            it[parentServiceId] = insertNewService.parentServiceId?.let { id -> UUID.fromString(id) }
            it[tagId] = insertNewService.tagId?.let { id -> UUID.fromString(id) }
        }.value

        return getServiceById(id)
    }

    override fun updateService(id: UUID, updateService: UpdateService): Service? {
        ServicesTable.update({ ServicesTable.id eq id }) {
            it[title] = updateService.title
            it[description] = updateService.description
            it[bannerDescription] = updateService.bannerDescription
            it[shortDescription] = updateService.shortDescription
            it[extraInfo] = updateService.extraInfo
            it[parentServiceId] = updateService.parentServiceId?.let { parentId -> UUID.fromString(parentId) }
            it[tagId] = updateService.tagId?.let { id -> UUID.fromString(id) }

            it[updatedAt] = LocalDateTime.now()
        }

        return getServiceById(id)
    }

    override fun deleteService(id: UUID): Boolean =
        ServicesTable.deleteWhere { ServicesTable.id eq id } > 0

    override fun getListOfExistingServiceIds(serviceIds: List<UUID>): List<UUID> =
        ServicesTable.selectAll().where { ServicesTable.id inList serviceIds }.map { it[ServicesTable.id].value }
}
