package com.cbconnectit.controllers.services

import com.cbconnectit.data.dto.requests.service.InsertNewService
import com.cbconnectit.data.dto.requests.service.UpdateService
import com.cbconnectit.domain.models.service.Service
import java.util.*

object ServiceInstrumentation {

    fun givenAnInvalidInsertService() = InsertNewService("  ", tagId = "00000000-0000-0000-0000-000000000001")
    fun givenAnInvalidUpdateService() = UpdateService("  ", tagId = "00000000-0000-0000-0000-000000000001")
    fun givenAValidInsertService() = InsertNewService("New service", tagId = "00000000-0000-0000-0000-000000000001")
    fun givenAValidInsertNewServiceWithParent() = InsertNewService("Sub service", tagId = "00000000-0000-0000-0000-000000000001", parentServiceId = "00000000-0000-0000-0000-000000000001")
    fun givenAValidUpdateService() = UpdateService("Updated service", tagId = "00000000-0000-0000-0000-000000000001")
    fun givenAValidUpdateServiceWithParent() = UpdateService("Sub service", tagId = "00000000-0000-0000-0000-000000000001", parentServiceId = "00000000-0000-0000-0000-000000000001")

    fun givenServiceList() = listOf(
        givenAService(id = UUID.fromString("00000000-0000-0000-0000-000000000001"), name = "First Parent Service"),
        givenAService(id = UUID.fromString("00000000-0000-0000-0000-000000000002"), name = "Second Parent Service"),
        givenAService(id = UUID.fromString("00000000-0000-0000-0000-000000000001"), name = "Third Parent Service"),
        givenAService(id = UUID.fromString("00000000-0000-0000-0000-000000000001"), name = "Fourth Parent Service"),
    )

    fun givenAService(id: UUID = UUID.randomUUID(), name: String = "Parent Service") = Service(id = id, name = name)
    fun givenAService(id: UUID = UUID.randomUUID(), name: String = "Parent Service", subService: Service) = Service(id = id, name = name, subServices = listOf(subService))
}