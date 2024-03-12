package com.cbconnectit.routing.services

import com.cbconnectit.data.dto.requests.service.InsertNewService
import com.cbconnectit.data.dto.requests.service.ServiceDto
import com.cbconnectit.data.dto.requests.service.UpdateService
import com.cbconnectit.utils.toDatabaseString
import java.time.LocalDateTime
import java.util.*

object ServiceInstrumentation {
    fun givenAValidInsertService() = InsertNewService("New Parent Service", tagId = "00000000-0000-0000-0000-000000000001")
    fun givenAValidUpdateServiceBody() = UpdateService("Updated Parent service", tagId = "00000000-0000-0000-0000-000000000001")

    fun givenAnEmptyInsertServiceBody() = InsertNewService("    ", tagId = "00000000-0000-0000-0000-000000000001")

    fun givenServiceList() = listOf(
        givenAService("First parent service"),
        givenAService("Second parent service"),
        givenAService("Third parent service"),
        givenAService("Fourth parent service"),
    )

    fun givenAService(name: String = "First Parent service") = run {
        val time = LocalDateTime.now().toDatabaseString()
        ServiceDto(
            id = UUID.randomUUID().toString(),
            title = name,
            createdAt = time,
            updatedAt = time
        )
    }
}