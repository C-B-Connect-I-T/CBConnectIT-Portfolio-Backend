package com.cbconnectit.data.database.dao

import com.cbconnectit.data.database.tables.ServicesTable
import com.cbconnectit.data.database.tables.TagsTable
import com.cbconnectit.data.database.tables.toService
import com.cbconnectit.data.dto.requests.service.InsertNewService
import com.cbconnectit.data.dto.requests.service.UpdateService
import com.cbconnectit.domain.interfaces.IServiceDao
import com.cbconnectit.domain.models.service.Service
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.time.LocalDateTime
import java.util.*

class ServiceDaoImpl : IServiceDao {

    override fun getServiceById(id: UUID): Service? =
        fetchServicesWithSubServices(id)

    override fun getServices(): List<Service> =
        fetchServicesRecursive(null)

    private fun fetchServicesWithSubServices(id: UUID): Service? {
        var service: Service? = null

        transaction {
            service = (ServicesTable leftJoin TagsTable).select { ServicesTable.id eq id }
                .toService()

            val subServices = fetchServicesRecursive(id)
            service = service?.copy(subServices = subServices)
        }

        return service
    }

    private fun fetchServicesRecursive(parentId: UUID? = null): List<Service> {
        val subService = mutableListOf<Service>()

        transaction {
            val services = (ServicesTable leftJoin TagsTable).select { ServicesTable.parentServiceId eq parentId }
                .map { it.toService() }

            services.forEach { childService ->
                val grandSubServices = fetchServicesRecursive(childService.id)
                subService.add(childService.copy(subServices = grandSubServices.ifEmpty { null }))
            }
        }

        return subService
    }

    override fun insertService(insertNewService: InsertNewService): Service? {
        val id = ServicesTable.insertAndGetId {
            it[title] = insertNewService.title
            it[imageUrl] = insertNewService.imageUrl
            it[bannerImageUrl] = insertNewService.bannerImageUrl
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
            it[imageUrl] = updateService.imageUrl
            it[bannerImageUrl] = updateService.bannerImageUrl
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
        ServicesTable.select { ServicesTable.id inList serviceIds }.map { it[ServicesTable.id].value }
}
