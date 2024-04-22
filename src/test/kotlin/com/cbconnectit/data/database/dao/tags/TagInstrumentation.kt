package com.cbconnectit.data.database.dao.tags

import com.cbconnectit.data.dto.requests.tag.InsertNewTag
import com.cbconnectit.data.dto.requests.tag.UpdateTag

object TagInstrumentation {

    fun givenAValidInsertTagBody() = InsertNewTag("First tag")
    fun givenAValidSecondInsertTagBody() = InsertNewTag("Second tag")

    fun givenAValidUpdateTagBody() = UpdateTag("christiano bolla")

    fun givenAnEmptyUpdateTagBody() = UpdateTag("   ")
    fun givenAnUnknownTag() = InsertNewTag("Unknown")
}
