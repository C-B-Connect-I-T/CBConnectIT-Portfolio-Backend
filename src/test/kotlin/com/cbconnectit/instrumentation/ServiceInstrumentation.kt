package com.cbconnectit.instrumentation

import com.cbconnectit.data.dto.requests.service.InsertNewService
import com.cbconnectit.data.dto.requests.service.ServiceAdminDto
import com.cbconnectit.data.dto.requests.service.UpdateService
import com.cbconnectit.domain.models.service.Service
import java.util.*

object ServiceInstrumentation {

    fun givenAnInvalidInsertService() = InsertNewService("  ", description = " ", tagId = "00000000-0000-0000-0000-000000000001")
    fun givenAnInvalidUpdateService() = UpdateService("  ", description = "  ", tagId = "00000000-0000-0000-0000-000000000001")
    fun givenAValidInsertService() = InsertNewService("New service", description = "New description", tagId = "00000000-0000-0000-0000-000000000001")
    fun givenAValidInsertServiceWithParent() = InsertNewService(
        "Sub service",
        description = "Sub description",
        tagId = "00000000-0000-0000-0000-000000000001",
        parentServiceId = "00000000-0000-0000-0000-000000000001"
    )

    fun givenAValidUpdateService() = UpdateService("Updated service", description = "Updated description", tagId = "00000000-0000-0000-0000-000000000001")
    fun givenAValidUpdateServiceWithParent() = UpdateService(
        "Sub service",
        description = "sub description",
        tagId = "00000000-0000-0000-0000-000000000001",
        parentServiceId = "00000000-0000-0000-0000-000000000001"
    )

    fun givenServiceList() = listOf(
        givenAService(id = UUID.fromString("00000000-0000-0000-0000-000000000001"), name = "First Parent Service"),
        givenAService(id = UUID.fromString("00000000-0000-0000-0000-000000000002"), name = "Second Parent Service"),
        givenAService(id = UUID.fromString("00000000-0000-0000-0000-000000000001"), name = "Third Parent Service"),
        givenAService(id = UUID.fromString("00000000-0000-0000-0000-000000000001"), name = "Fourth Parent Service"),
    )

    fun givenServiceAdminList() = listOf(
        ServiceAdminDto(id = "00000000-0000-0000-0000-000000000001", title = "Parent Service", updatedAt = "2024-01-01 00:00:00"),
        ServiceAdminDto(id = "00000000-0000-0000-0000-000000000002", title = "Sub Service", updatedAt = "2024-01-01 00:00:00"),
    )

    fun givenAService(
        id: UUID = UUID.randomUUID(),
        name: String = "Parent Service",
        subService: Service? = null
    ) = Service(
        id = id,
        title = name,
        description = "Parent description",
        subServices = if (subService != null) listOf(subService) else emptyList()
    )
}
