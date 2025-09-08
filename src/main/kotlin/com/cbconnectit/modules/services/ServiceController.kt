package com.cbconnectit.modules.services

import com.cbconnectit.data.dto.requests.service.InsertNewService
import com.cbconnectit.data.dto.requests.service.ServiceDto
import com.cbconnectit.data.dto.requests.service.UpdateService
import com.cbconnectit.domain.interfaces.IServiceDao
import com.cbconnectit.domain.interfaces.ITagDao
import com.cbconnectit.domain.models.service.toDto
import com.cbconnectit.plugins.dbQuery
import com.cbconnectit.statuspages.ErrorFailedCreate
import com.cbconnectit.statuspages.ErrorFailedDelete
import com.cbconnectit.statuspages.ErrorFailedUpdate
import com.cbconnectit.statuspages.ErrorInvalidParameters
import com.cbconnectit.statuspages.ErrorNotFound
import com.cbconnectit.statuspages.ErrorUnknownServiceIdsForCreate
import com.cbconnectit.statuspages.ErrorUnknownServiceIdsForUpdate
import com.cbconnectit.statuspages.ErrorUnknownTagIdsForCreate
import com.cbconnectit.statuspages.ErrorUnknownTagIdsForUpdate
import java.util.*

class ServiceControllerImpl(
    private val serviceDao: IServiceDao,
    private val tagDao: ITagDao
) : ServiceController {

    override suspend fun getServices(): List<ServiceDto> = dbQuery {
        serviceDao.getServices().map { it.toDto() }
    }

    override suspend fun getServiceById(serviceId: UUID): ServiceDto = dbQuery {
        serviceDao.getServiceById(serviceId)?.toDto() ?: throw ErrorNotFound
    }

    override suspend fun postService(insertNewService: InsertNewService): ServiceDto = dbQuery {
        if (!insertNewService.isValid) throw ErrorInvalidParameters

        val parentServiceIds = insertNewService.parentServiceUuid?.let { serviceDao.getListOfExistingServiceIds(listOf(it)) }
        if (insertNewService.parentServiceUuid != null && parentServiceIds?.count() != 1) {
            throw ErrorUnknownServiceIdsForCreate(listOfNotNull(insertNewService.parentServiceUuid))
        }

        val tagIds = insertNewService.tagUuid?.let { tagDao.getListOfExistingTagIds(listOf(it)) }
        if (tagIds != null && tagIds.count() != 1) {
            throw ErrorUnknownTagIdsForCreate(listOfNotNull(insertNewService.tagUuid))
        }

        serviceDao.insertService(insertNewService)?.toDto() ?: throw ErrorFailedCreate
    }

    override suspend fun updateServiceById(serviceId: UUID, updateService: UpdateService): ServiceDto = dbQuery {
        if (!updateService.isValid) throw ErrorInvalidParameters

        val parentServiceIds = updateService.parentServiceUuid?.let { serviceDao.getListOfExistingServiceIds(listOf(it)) }
        if (updateService.parentServiceUuid != null && parentServiceIds?.count() != 1) {
            throw ErrorUnknownServiceIdsForUpdate(listOfNotNull(updateService.parentServiceUuid))
        }

        val tagIds = updateService.tagUuid?.let { tagDao.getListOfExistingTagIds(listOf(it)) }
        if (tagIds != null && tagIds.count() != 1) {
            throw ErrorUnknownTagIdsForUpdate(listOfNotNull(updateService.tagUuid))
        }

        serviceDao.updateService(serviceId, updateService)?.toDto() ?: throw ErrorFailedUpdate
    }

    override suspend fun deleteServiceById(serviceId: UUID) = dbQuery {
        val deleted = serviceDao.deleteService(serviceId)
        if (!deleted) throw ErrorFailedDelete
    }
}

interface ServiceController {
    suspend fun getServices(): List<ServiceDto>
    suspend fun getServiceById(serviceId: UUID): ServiceDto
    suspend fun postService(insertNewService: InsertNewService): ServiceDto
    suspend fun updateServiceById(serviceId: UUID, updateService: UpdateService): ServiceDto
    suspend fun deleteServiceById(serviceId: UUID)
}
