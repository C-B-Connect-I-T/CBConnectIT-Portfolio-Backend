package com.cbconnectit.routing.users

import com.cbconnectit.data.dto.requests.user.InsertNewUser
import com.cbconnectit.data.dto.requests.user.UpdatePassword
import com.cbconnectit.data.dto.requests.user.UserDto
import com.cbconnectit.domain.models.user.User
import com.cbconnectit.domain.models.user.UserRoles
import com.cbconnectit.domain.models.user.toDto
import com.cbconnectit.modules.auth.ADMIN_ONLY
import com.cbconnectit.modules.users.UserController
import com.cbconnectit.modules.users.userRouting
import com.cbconnectit.routing.AuthenticationInstrumentation
import com.cbconnectit.routing.BaseRoutingTest
import com.cbconnectit.utils.toDatabaseString
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.mockk.coEvery
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import org.koin.dsl.module
import java.time.LocalDateTime
import java.util.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UserRoutingTest : BaseRoutingTest() {

    private val userController: UserController = mockk()

    @BeforeAll
    fun setup() {
        koinModules = module {
            single { userController }
        }

        moduleList = {
            install(Routing) {
                userRouting()
            }
        }
    }

    @BeforeEach
    fun clearMocks() {
        io.mockk.clearMocks(userController)
    }

    @Test
    fun `when creating user without any body, returns error`() = withBaseTestApplication(
        AuthenticationInstrumentation(ADMIN_ONLY, UserRoles.Admin),
        AuthenticationInstrumentation()
    ) {
        coEvery { userController.postUser(any()) } throws Exception()

        val exception = assertThrows<Exception> {
            doCall(HttpMethod.Post, "/users")
        }
        assertThat(exception.message).isEqualTo(null)
    }

    @Test
    fun `when creating user with correct data, user not admin, we return error`() = withBaseTestApplication(
        AuthenticationInstrumentation(ADMIN_ONLY),
        AuthenticationInstrumentation()
    ) {
        val call = doCall(HttpMethod.Post, "/users")

        assertThat(call.response.status()).isEqualTo(HttpStatusCode.Unauthorized)
    }

    @Test
    fun `when creating user with successful insertion, we return response user body`() = withBaseTestApplication(
        AuthenticationInstrumentation(ADMIN_ONLY, UserRoles.Admin),
        AuthenticationInstrumentation()
    ) {
        val time = LocalDateTime.now().toDatabaseString()
        val userDto = UserDto(UUID.randomUUID().toString(), "Chri Bol", "chri.bol@example.com", time, time, UserRoles.User)
        coEvery { userController.postUser(any()) } returns userDto

        val body = toJsonBody(InsertNewUser("", "", "", ""))
        val call = doCall(HttpMethod.Post, "/users", body)

        call.also {
            assertThat(HttpStatusCode.Created).isEqualTo(it.response.status())
            val responseBody = it.response.parseBody(UserDto::class.java)
            assertThat(userDto).isEqualTo(responseBody)
        }
    }

    @Test
    fun `when creating user with any error, we return error`() = withBaseTestApplication(
        AuthenticationInstrumentation(ADMIN_ONLY, UserRoles.Admin),
        AuthenticationInstrumentation()
    ) {
        coEvery { userController.postUser(any()) } throws Exception()

        val body = toJsonBody(InsertNewUser("", "", "", ""))
        val exception = assertThrows<Exception> {
            doCall(HttpMethod.Post, "/users", body)
        }
        assertThat(exception.message).isEqualTo(null)
    }

    @Test
    fun `when fetching current user, we return user`() = withBaseTestApplication(
        AuthenticationInstrumentation(ADMIN_ONLY),
        AuthenticationInstrumentation()
    ) {
        val time = LocalDateTime.now()
        val userResponse = User(UUID.fromString("00000000-0000-0000-0000-000000000001"), "Chris Bol", "chris.bol@example.com", time, time).toDto()

        val call = doCall(HttpMethod.Get, "/users/me")

        call.also {
            assertThat(HttpStatusCode.OK).isEqualTo(it.response.status())
            val responseBody = it.response.parseBody(UserDto::class.java)
            assertThat(userResponse).matches { user ->
                user.id == responseBody.id &&
                        user.role == responseBody.role &&
                        user.username == responseBody.username &&
                        user.fullName == responseBody.fullName
            }
        }
    }

    @Test
    fun `when updating user with successful insertion, we return response user body`() = withBaseTestApplication(
        AuthenticationInstrumentation(ADMIN_ONLY),
        AuthenticationInstrumentation()
    ) {
        val time = LocalDateTime.now().toDatabaseString()
        val userDto = UserDto(UUID.randomUUID().toString(), "Chri Bol", "chri.bol@example.com", time, time, UserRoles.User)
        coEvery { userController.updateUserById(any(), any()) } returns userDto

        val body = toJsonBody(InsertNewUser("", "", "", ""))
        val call = doCall(HttpMethod.Put, "/users/me", body)

        call.also {
            assertThat(HttpStatusCode.OK).isEqualTo(it.response.status())
            val responseBody = it.response.parseBody(UserDto::class.java)
            assertThat(userDto).isEqualTo(responseBody)
        }
    }

    @Test
    fun `when updating user with any error, we return error`() = withBaseTestApplication(
        AuthenticationInstrumentation(ADMIN_ONLY),
        AuthenticationInstrumentation()
    ) {
        coEvery { userController.updateUserById(any(), any()) } throws Exception()

        val body = toJsonBody(InsertNewUser("", "", "", ""))
        val exception = assertThrows<Exception> {
            doCall(HttpMethod.Put, "/users/me", body)
        }
        assertThat(exception.message).isEqualTo(null)
    }

    @Test
    fun `when updating user password with successful insertion, we return response user body`() = withBaseTestApplication(
        AuthenticationInstrumentation(ADMIN_ONLY),
        AuthenticationInstrumentation()
    ) {
        val time = LocalDateTime.now().toDatabaseString()
        val userDto = UserDto(UUID.randomUUID().toString(), "Chri Bol", "chri.bol@example.com", time, time, UserRoles.User)
        coEvery { userController.updateUserPasswordById(any(), any()) } returns userDto

        val body = toJsonBody(UpdatePassword("", "", ""))
        val call = doCall(HttpMethod.Put, "/users/me/password", body)

        call.also {
            assertThat(HttpStatusCode.OK).isEqualTo(it.response.status())
            val responseBody = it.response.parseBody(UserDto::class.java)
            assertThat(userDto).isEqualTo(responseBody)
        }
    }

    @Test
    fun `when updating user password with any error, we return error`() = withBaseTestApplication(
        AuthenticationInstrumentation(ADMIN_ONLY),
        AuthenticationInstrumentation()
    ) {
        coEvery { userController.updateUserPasswordById(any(), any()) } throws Exception()

        val body = toJsonBody(InsertNewUser("", "", "", ""))
        val exception = assertThrows<Exception> {
            doCall(HttpMethod.Put, "/users/me/password", body)
        }
        assertThat(exception.message).isEqualTo(null)
    }

    @Test
    fun `when fetching a specific user that exists, we return that user`() = withBaseTestApplication(
        AuthenticationInstrumentation(ADMIN_ONLY, UserRoles.Admin),
        AuthenticationInstrumentation()
    ) {
        val time = LocalDateTime.now().toDatabaseString()
        val userResponse = UserDto(UUID.randomUUID().toString(), "", "", time, time)
        coEvery { userController.getUserById(any()) } returns userResponse

        val call = doCall(HttpMethod.Get, "/users/a63a20c4-14dd-4e11-9e87-5ab361a51f65")

        call.also {
            assertThat(HttpStatusCode.OK).isEqualTo(it.response.status())
            val responseBody = it.response.parseBody(UserDto::class.java)
            assertThat(userResponse).isEqualTo(responseBody)
        }
    }

    @Test
    fun `when fetching a specific user that does not exist, we return error`() = withBaseTestApplication(
        AuthenticationInstrumentation(ADMIN_ONLY, UserRoles.Admin),
        AuthenticationInstrumentation()
    ) {
        coEvery { userController.getUserById(any()) } throws Exception()

        val exception = assertThrows<Exception> {
            doCall(HttpMethod.Get, "/users/a63a20c4-14dd-4e11-9e87-5ab361a51f65")
        }

        assertThat(exception.message).isEqualTo(null)
    }

    @Test
    fun `when fetching a specific user, user not admin, we return error`() = withBaseTestApplication(
        AuthenticationInstrumentation(ADMIN_ONLY),
        AuthenticationInstrumentation()
    ) {

        val call = doCall(HttpMethod.Get, "/users/a63a20c4-14dd-4e11-9e87-5ab361a51f65")

        assertThat(call.response.status()).isEqualTo(HttpStatusCode.Unauthorized)
    }

    @Test
    fun `when updating user by id with successful insertion, we return response user body`() = withBaseTestApplication(
        AuthenticationInstrumentation(ADMIN_ONLY, UserRoles.Admin),
        AuthenticationInstrumentation()
    ) {
        val time = LocalDateTime.now().toDatabaseString()
        val userDto = UserDto(UUID.randomUUID().toString(), "Chri Bol", "chri.bol@example.com", time, time, UserRoles.User)
        coEvery { userController.updateUserById(any(), any()) } returns userDto

        val body = toJsonBody(InsertNewUser("", "", "", ""))
        val call = doCall(HttpMethod.Put, "/users/a63a20c4-14dd-4e11-9e87-5ab361a51f65", body)

        call.also {
            assertThat(HttpStatusCode.OK).isEqualTo(it.response.status())
            val responseBody = it.response.parseBody(UserDto::class.java)
            assertThat(userDto).isEqualTo(responseBody)
        }
    }

    @Test
    fun `when updating user by id with any error, we return error`() = withBaseTestApplication(
        AuthenticationInstrumentation(ADMIN_ONLY, UserRoles.Admin),
        AuthenticationInstrumentation()
    ) {
        coEvery { userController.updateUserById(any(), any()) } throws Exception()

        val body = toJsonBody(InsertNewUser("", "", "", ""))
        val exception = assertThrows<Exception> {
            doCall(HttpMethod.Put, "/users/a63a20c4-14dd-4e11-9e87-5ab361a51f65", body)
        }
        assertThat(exception.message).isEqualTo(null)
    }

    @Test
    fun `when updating user by id, user not admin, we return error`() = withBaseTestApplication(
        AuthenticationInstrumentation(ADMIN_ONLY),
        AuthenticationInstrumentation()
    ) {

        val call = doCall(HttpMethod.Put, "/users/a63a20c4-14dd-4e11-9e87-5ab361a51f65")

        assertThat(call.response.status()).isEqualTo(HttpStatusCode.Unauthorized)
    }

    @Test
    fun `when updating user password by id, with successful insertion, we return response user body`() = withBaseTestApplication(
        AuthenticationInstrumentation(ADMIN_ONLY, UserRoles.Admin),
        AuthenticationInstrumentation()
    ) {
        val time = LocalDateTime.now().toDatabaseString()
        val userDto = UserDto(UUID.randomUUID().toString(), "Chri Bol", "chri.bol@example.com", time, time, UserRoles.User)
        coEvery { userController.updateUserPasswordById(any(), any()) } returns userDto

        val body = toJsonBody(UpdatePassword("", "", ""))
        val call = doCall(HttpMethod.Put, "/users/a63a20c4-14dd-4e11-9e87-5ab361a51f65/password", body)

        call.also {
            assertThat(HttpStatusCode.OK).isEqualTo(it.response.status())
            val responseBody = it.response.parseBody(UserDto::class.java)
            assertThat(userDto).isEqualTo(responseBody)
        }
    }

    @Test
    fun `when updating user password by id, with any error, we return error`() = withBaseTestApplication(
        AuthenticationInstrumentation(ADMIN_ONLY, UserRoles.Admin),
        AuthenticationInstrumentation()
    ) {
        coEvery { userController.updateUserPasswordById(any(), any()) } throws Exception()

        val body = toJsonBody(InsertNewUser("", "", "", ""))
        val exception = assertThrows<Exception> {
            doCall(HttpMethod.Put, "/users/a63a20c4-14dd-4e11-9e87-5ab361a51f65/password", body)
        }
        assertThat(exception.message).isEqualTo(null)
    }

    @Test
    fun `when updating user password by id, user not admin, we return error`() = withBaseTestApplication(
        AuthenticationInstrumentation(ADMIN_ONLY),
        AuthenticationInstrumentation()
    ) {

        val call = doCall(HttpMethod.Put, "/users/a63a20c4-14dd-4e11-9e87-5ab361a51f65/password")

        assertThat(call.response.status()).isEqualTo(HttpStatusCode.Unauthorized)
    }

    @Test
    fun `when deleting user successful by id, we return Ok response`() = withBaseTestApplication(
        AuthenticationInstrumentation(ADMIN_ONLY, UserRoles.Admin),
        AuthenticationInstrumentation()
    ) {
        coEvery { userController.deleteUserById(any()) } returns Unit

        val call = doCall(HttpMethod.Delete, "/users/a63a20c4-14dd-4e11-9e87-5ab361a51f65")

        call.also {
            assertThat(HttpStatusCode.OK).isEqualTo(it.response.status())
        }
    }

    @Test
    fun `when deleting user by id with any error, we return error`() = withBaseTestApplication(
        AuthenticationInstrumentation(ADMIN_ONLY, UserRoles.Admin),
        AuthenticationInstrumentation()
    ) {
        coEvery { userController.deleteUserById(any()) } throws Exception()

        val exception = assertThrows<Exception> {
            doCall(HttpMethod.Delete, "/users/a63a20c4-14dd-4e11-9e87-5ab361a51f65")
        }
        assertThat(exception.message).isEqualTo(null)
    }

    @Test
    fun `when deleting user by id, user not admin, we return error`() = withBaseTestApplication(
        AuthenticationInstrumentation(ADMIN_ONLY),
        AuthenticationInstrumentation()
    ) {

        val call = doCall(HttpMethod.Put, "/users/a63a20c4-14dd-4e11-9e87-5ab361a51f65/password")

        assertThat(call.response.status()).isEqualTo(HttpStatusCode.Unauthorized)
    }
}
