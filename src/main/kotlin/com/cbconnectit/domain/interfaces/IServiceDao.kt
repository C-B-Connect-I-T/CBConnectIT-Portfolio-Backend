package com.cbconnectit.domain.interfaces

import com.cbconnectit.data.dto.requests.service.InsertNewService
import com.cbconnectit.data.dto.requests.service.UpdateService
import com.cbconnectit.domain.models.service.Service
import java.util.*

interface IServiceDao {

    fun getServiceById(id: UUID): Service?
    fun getServices(): List<Service>
    fun insertService(insertNewService: InsertNewService): Service?
    fun updateService(id: UUID, updateService: UpdateService): Service?
    fun deleteService(id: UUID): Boolean
    fun getListOfExistingServiceIds(serviceIds: List<UUID>): List<UUID>
}