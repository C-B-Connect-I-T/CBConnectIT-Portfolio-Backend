package com.cbconnectit.data.database.dao.links

import com.cbconnectit.data.dto.requests.link.InsertNewLink
import com.cbconnectit.data.dto.requests.link.UpdateLink

object LinkInstrumentation {

    fun givenAValidInsertLinkBody() = InsertNewLink("First link")
    fun givenAValidSecondInsertLinkBody() = InsertNewLink("Second link")

    fun givenAValidUpdateLinkBody() = UpdateLink("christiano bolla")

    fun givenAnEmptyUpdateLinkBody() = UpdateLink("   ")
    fun givenAnUnknownLink() = InsertNewLink("Unknown")
}
