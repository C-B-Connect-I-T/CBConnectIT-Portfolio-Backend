package com.cbconnectit.controllers.users

import com.cbconnectit.data.dto.requests.user.InsertNewUser
import com.cbconnectit.data.dto.requests.user.UpdatePassword
import com.cbconnectit.data.dto.requests.user.UpdateUser
import com.cbconnectit.domain.models.user.User
import org.jetbrains.exposed.sql.javatime.CurrentDateTime
import java.time.LocalDateTime
import java.util.*

object UserInstrumentation {

    fun givenAnInvalidInsertUser() = InsertNewUser("", "John.Doe@example.be", "Test1234", "Test1234")

    fun givenAnAlreadyKnownInsertUser() = InsertNewUser("John Doe", "John.Doe@example.be", "Test1234", "Test1234")

    fun givenAnInvalidInsertUserWherePasswordsDontMatch() = InsertNewUser("John Doe", "John.Doe@example.be", "Test1234", "Test12345")

    fun givenAnInvalidInsertUserWherePasswordIsNotStrong() = InsertNewUser("John Doe", "John.Doe@example.be", "test", "test")

    fun givenAValidInsertUser() = InsertNewUser("John Doe", "John.Doe@example.be", "Test1234", "Test1234")

    fun givenAValidUser() = User(UUID.randomUUID(), "John Doe", "john.doe@example.be", LocalDateTime.now(), LocalDateTime.now())

    fun givenAValidUpdateUser() = UpdateUser("John Doe", "john.doe@example.be")

    fun givenOldPasswordIsSameAsNewPassword() = UpdatePassword("Test1234", "Test1234", "Test1234")

    fun givenPasswordsDontMatch() = UpdatePassword("Test1", "Test1234", "1234Test")

    fun givenPasswordNotStrong() = UpdatePassword("Test1", "test", "test")

    fun givenValidUpdatePassword() = UpdatePassword("Test1", "Test1234", "Test1234")
}