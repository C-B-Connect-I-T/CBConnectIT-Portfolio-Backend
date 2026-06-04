package com.cbconnectit.instrumentation

import com.cbconnectit.data.dto.requests.user.InsertNewUser
import com.cbconnectit.data.dto.requests.user.UpdatePassword
import com.cbconnectit.data.dto.requests.user.UpdateUser
import com.cbconnectit.domain.models.user.User
import java.time.LocalDateTime
import java.util.*

object UserInstrumentation {

    fun givenAnInvalidInsertUser() = InsertNewUser("", "John.Doe@example.be", "Test1234", "Test1234")

    fun givenAnAlreadyKnownInsertUser() = InsertNewUser("John Doe", "John.Doe@example.be", "Test1234", "Test1234")

    fun givenAnInvalidInsertUserWherePasswordsDontMatch() = InsertNewUser("John Doe", "John.Doe@example.be", "Test1234", "Test12345")

    fun givenAnInvalidInsertUserWherePasswordIsNotStrong() = InsertNewUser("John Doe", "John.Doe@example.be", "test", "test")

    fun givenAValidInsertUser(
        fullName: String = "christiano bolla",
        username: String = "christiano@example",
        password: String = "ValidPass123",
        confirmPassword: String? = password
    ) = InsertNewUser(fullName, username, password, confirmPassword)

    fun givenAValidUser(
        id: UUID = UUID.randomUUID(),
        fullName: String = "John Doe",
        username: String = "john.doe@example.be"
    ) = User(id, fullName, username, LocalDateTime.now(), LocalDateTime.now())

    fun givenAValidUpdateUser(
        fullName: String = "John Doe",
        username: String = "john.doe@example.be"
    ) = UpdateUser(fullName, username)

    fun givenAnEmptyUpdateUserBody() = UpdateUser()

    fun givenOldPasswordIsSameAsNewPassword() = UpdatePassword("Test1234", "Test1234", "Test1234")

    fun givenPasswordsDontMatch() = UpdatePassword("Test1", "Test1234", "1234Test")

    fun givenPasswordNotStrong() = UpdatePassword("Test1", "test", "test")

    fun givenValidUpdatePassword() = UpdatePassword("Test1", "Test1234", "Test1234")

    fun givenUserList() = listOf(
        givenAUser(UUID.fromString("00000000-0000-0000-0000-000000000001"), "User One", "user1@example", "hash1"),
        givenAUser(UUID.fromString("00000000-0000-0000-0000-000000000002"), "User Two", "user2@example", "hash2"),
    )

    fun givenAUser(
        id: UUID = UUID.randomUUID(),
        fullName: String = "John Doe",
        username: String = "john.doe@example.be",
        password: String = "ValidPass123",
        role: User.Role = User.Role.User
    ) = User(
        id = id,
        fullName = fullName,
        username = username,
        createdAt = LocalDateTime.now(),
        updatedAt = LocalDateTime.now(),
        role = role,
        password = password
    )
}
