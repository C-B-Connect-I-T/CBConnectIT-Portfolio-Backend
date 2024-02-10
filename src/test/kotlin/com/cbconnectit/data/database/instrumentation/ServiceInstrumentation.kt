package com.cbconnectit.data.database.instrumentation

import com.cbconnectit.data.dto.requests.service.InsertNewService
import com.cbconnectit.data.dto.requests.service.UpdateService

object ServiceInstrumentation {

    fun givenAValidInsertServiceBody() = InsertNewService("First Service", tagId = "00000000-0000-0000-0000-000000000001")
    fun givenAValidSecondInsertServiceBody() = InsertNewService("Second Service", tagId = "00000000-0000-0000-0000-000000000001")

    fun givenAValidUpdateServiceBody() = UpdateService("Updated Service", tagId = "00000000-0000-0000-0000-000000000001")

    fun givenAnEmptyUpdateServiceBody() = UpdateService("   ", tagId = "00000000-0000-0000-0000-000000000001")
    fun givenAnUnknownService() = InsertNewService("Unknown", tagId = "00000000-0000-0000-0000-000000000001")
}