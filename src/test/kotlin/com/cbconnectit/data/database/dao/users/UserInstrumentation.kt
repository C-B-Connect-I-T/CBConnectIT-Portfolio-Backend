package com.cbconnectit.data.database.dao.users

import com.cbconnectit.data.dto.requests.user.InsertNewUser
import com.cbconnectit.data.dto.requests.user.UpdateUser

object UserInstrumentation {

    fun givenAValidInsertUserBody() = InsertNewUser("christiano bolla", "christiano@example", "hash", null)

    fun givenAValidUpdateUserBody() = UpdateUser("John Doe", "john.dao@example.be")

    fun givenAnEmptyUpdateUserBody() = UpdateUser()
}
