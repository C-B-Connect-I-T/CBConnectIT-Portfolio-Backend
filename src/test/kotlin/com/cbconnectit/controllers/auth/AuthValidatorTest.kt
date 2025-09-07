package com.cbconnectit.controllers.auth

import com.auth0.jwt.interfaces.Payload
import com.cbconnectit.controllers.BaseControllerTest
import com.cbconnectit.domain.interfaces.IUserDao
import com.cbconnectit.domain.models.user.User
import com.cbconnectit.domain.models.user.UserRoles
import com.cbconnectit.modules.auth.validateUser
import com.cbconnectit.modules.auth.validateUserIsAdmin
import io.ktor.server.auth.jwt.*
import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.util.*
import kotlin.test.assertNull

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AuthValidatorTest : BaseControllerTest() {

    private val userDao: IUserDao = mockk()
    private val payload: Payload = mockk()

    @BeforeEach
    override fun before() {
        super.before()
        clearMocks(userDao)
    }

    @Test
    fun `when validating user where payload does not contain UserId, return null -- unauthorized`() {
        coEvery { payload.claims[any()]?.asString() } returns null

        val jwt = JWTCredential(payload)

        runBlocking {
            val response = jwt.validateUser(userDao)
            assertNull(response)
        }
    }

    @Test
    fun `when validating user where payload does not contain correct audience, return null -- unauthorized`() {
        coEvery { payload.claims[any()]?.asString() } returns "00000000-0000-0000-0000-000000000001"
        coEvery { userDao.getUser(any()) } returns User()
        coEvery { payload.audience.contains(any()) } returns false

        val jwt = JWTCredential(payload)

        runBlocking {
            val response = jwt.validateUser(userDao)
            assertNull(response)
        }
    }

    @Test
    fun `when validating user where every thing is correct, return user as principal`() {
        coEvery { payload.claims[any()]?.asString() } returns "00000000-0000-0000-0000-000000000001"
        coEvery { userDao.getUser(any()) } returns User(UUID.fromString("00000000-0000-0000-0000-000000000001"))
        coEvery { payload.audience.contains(any()) } returns true

        val jwt = JWTCredential(payload)

        runBlocking {
            val response = jwt.validateUser(userDao)
            assertThat(response).matches {
                it is User &&
                        it.id == UUID.fromString("00000000-0000-0000-0000-000000000001") &&
                        it.role == UserRoles.User
            }
        }
    }

    @Test
    fun `when validating user as admin where payload does not contain UserId, return null -- unauthorized`() {
        coEvery { payload.claims[any()]?.asString() } returns null

        val jwt = JWTCredential(payload)

        runBlocking {
            val response = jwt.validateUserIsAdmin(userDao)
            assertNull(response)
        }
    }

    @Test
    fun `when validating user as admin where payload does not contain correct audience, return null -- unauthorized`() {
        coEvery { payload.claims[any()]?.asString() } returns "00000000-0000-0000-0000-000000000001"
        coEvery { userDao.getUser(any()) } returns User()
        coEvery { userDao.isUserRoleAdmin(any()) } returns true
        coEvery { payload.audience.contains(any()) } returns false

        val jwt = JWTCredential(payload)

        runBlocking {
            val response = jwt.validateUserIsAdmin(userDao)
            assertNull(response)
        }
    }

    @Test
    fun `when validating user as admin where user is not admin, return null -- unauthorized`() {
        coEvery { payload.claims[any()]?.asString() } returns "00000000-0000-0000-0000-000000000001"
        coEvery { userDao.getUser(any()) } returns User()
        coEvery { userDao.isUserRoleAdmin(any()) } returns false
        coEvery { payload.audience.contains(any()) } returns true

        val jwt = JWTCredential(payload)

        runBlocking {
            val response = jwt.validateUserIsAdmin(userDao)
            assertNull(response)
        }
    }

    @Test
    fun `when validating user as admin where every thing is correct, return user as principal`() {
        coEvery { payload.claims[any()]?.asString() } returns "00000000-0000-0000-0000-000000000001"
        coEvery { userDao.getUser(any()) } returns User(UUID.fromString("00000000-0000-0000-0000-000000000001"))
        coEvery { userDao.isUserRoleAdmin(any()) } returns true
        coEvery { payload.audience.contains(any()) } returns true

        val jwt = JWTCredential(payload)

        runBlocking {
            val response = jwt.validateUserIsAdmin(userDao)
            assertThat(response).matches {
                it is User &&
                        it.id == UUID.fromString("00000000-0000-0000-0000-000000000001") &&
                        it.role == UserRoles.User
            }
        }
    }
}
