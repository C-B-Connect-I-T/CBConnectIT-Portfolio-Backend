package com.cbconnectit.data.database.dao.services

import com.cbconnectit.data.dto.requests.service.InsertNewService
import com.cbconnectit.data.dto.requests.service.UpdateService

object ServiceInstrumentation {

    fun givenAValidInsertServiceBody() = InsertNewService("First Service", imageUrl = "https://www.google.be/image", description =  "Description", tagId = "00000000-0000-0000-0000-000000000001")
    fun givenAValidSecondInsertServiceBody() = InsertNewService("Second Service", imageUrl = "https://www.google.be/image", description =  "Description", tagId = "00000000-0000-0000-0000-000000000001")

    fun givenAValidUpdateServiceBody() = UpdateService("Updated Service", imageUrl = "https://www.google.be/image", description =  "Description", tagId = "00000000-0000-0000-0000-000000000001")

    fun givenAnEmptyUpdateServiceBody() = UpdateService("   ", imageUrl = "https://www.google.be/image", description =  "Description", tagId = "00000000-0000-0000-0000-000000000001")
    fun givenAnUnknownService() = InsertNewService("Unknown", imageUrl = "https://www.google.be/image", description =  "Description", tagId = "00000000-0000-0000-0000-000000000001")
}