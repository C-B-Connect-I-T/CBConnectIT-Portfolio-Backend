package com.cbconnectit.dao

import com.cbconnectit.data.database.dao.UserDaoImpl
import com.cbconnectit.data.database.tables.UsersTable
import com.cbconnectit.domain.models.user.User
import com.cbconnectit.instrumentation.UserInstrumentation
import com.cbconnectit.instrumentation.UserInstrumentation.givenAValidInsertUser
import com.cbconnectit.instrumentation.UserInstrumentation.givenAValidUpdateUser
import kotlinx.coroutines.delay
import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.insert
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.*
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class UserDaoImplTest : BaseDaoTest() {

    private val dao = UserDaoImpl()

    override suspend fun seedData() {
        UserInstrumentation.givenUserList().forEach { data ->
            UsersTable.insert {
                it[id] = data.id
                it[username] = data.username
                it[password] = data.password ?: "hash"
                it[fullName] = data.fullName
                it[role] = data.role
                it[createdAt] = data.createdAt
                it[updatedAt] = data.updatedAt
            }
        }
    }

    @Test
    fun `getUser where item exists, return correct user`() = runTest {
        val user = dao.getUser(UserInstrumentation.givenUserList()[0].id)

        assertThat(user).matches {
            it?.username == UserInstrumentation.givenUserList()[0].username &&
                    it.fullName == UserInstrumentation.givenUserList()[0].fullName &&
                    it.role == UserInstrumentation.givenUserList()[0].role
        }
    }

    @Test
    fun `getUser where item does not exist, return 'null'`() = runTest {
        val user = dao.getUser(UUID.randomUUID())

        assertNull(user)
    }

    @Test
    fun `insertUser where information is correct, database is storing user and returning correct content`() = runTest(shouldSeedData = false) {
        val validUser = givenAValidInsertUser()
        val user = dao.insertUser(validUser)

        assertThat(user).matches {
            it?.username == validUser.username &&
                    it.fullName == validUser.fullName &&
                    it.role == User.Role.User &&
                    it.createdAt == it.updatedAt
        }
    }

    @Test
    fun `insertUser where the same data exists, database will give error`() = runTest(shouldSeedData = false) {
        val validUser = givenAValidInsertUser()
        dao.insertUser(validUser)

        assertThrows<ExposedSQLException> {
            dao.insertUser(givenAValidInsertUser())
        }
    }

    @Test
    fun `updateUser where information is correct, database is storing information and returning the correct content`() = runTest {
        // adding a delay so there is a clear difference between `updatedAt` and `createdAt`
        delay(1000)

        val validUpdateUser = givenAValidUpdateUser()
        val user = dao.updateUser(UserInstrumentation.givenUserList()[0].id, validUpdateUser)

        assertThat(user).matches {
            it?.username == validUpdateUser.username &&
                    it?.fullName == validUpdateUser.fullName &&
                    it?.role == User.Role.User &&
                    it.createdAt != it.updatedAt
        }
    }

    @Test
    fun `updateUser where information is correct but user with id does not exist, database does nothing and returns 'null'`() = runTest {
        val validUser = givenAValidUpdateUser()
        val user = dao.updateUser(UUID.randomUUID(), validUser)

        assertNull(user)
    }

    @Test
    fun `updateUserPassword where information is correct, database is storing information`() = runTest {
        // adding a delay so there is a clear difference between `updatedAt` and `createdAt`
        delay(1000)

        val validUpdateUserPassword = "new-hash"
        val user = dao.updateUserPassword(UserInstrumentation.givenUserList()[0].id, validUpdateUserPassword)

        val hashedUser = dao.getUserHashableById(UserInstrumentation.givenUserList()[0].id)

        assertThat(user).matches { it?.createdAt != it?.updatedAt }
        assertThat(hashedUser).matches {
            it?.username == UserInstrumentation.givenUserList()[0].username &&
                    it.password == validUpdateUserPassword
        }
    }

    @Test
    fun `getUsers but none exist return empty list`() = runTest(shouldSeedData = false) {
        val list = dao.getUsers()
        assertThat(list).isEmpty()
    }

    @Test
    fun `getUsers return the list`() = runTest {
        val list = dao.getUsers()
        assertThat(list).hasSize(2)
    }

    @Test
    fun `deleteUser for id that exists, return true`() = runTest {
        val deleted = dao.deleteUser(UserInstrumentation.givenUserList()[0].id)
        assertTrue(deleted)
    }

    @Test
    fun `deleteUser for id that does not exists, return false`() = runTest {
        val deleted = dao.deleteUser(UUID.randomUUID())
        assertFalse(deleted)
    }

    @Test
    fun `userUnique where user does not exist, return true`() = runTest {
        val unique = dao.userUnique("hell@example")
        assertTrue(unique)
    }

    @Test
    fun `userUnique where user does exist, return false`() = runTest {
        val unique = dao.userUnique(UserInstrumentation.givenUserList()[0].username)
        assertFalse(unique)
    }

    @Test
    fun `isUserRoleAdmin where user does not exist, return false`() = runTest {
        val isUserAdmin = dao.isUserRoleAdmin(UUID.randomUUID())
        assertFalse(isUserAdmin)
    }

    @Test
    fun `isUserRoleAdmin where user does exist but is not admin, return false`() = runTest {
        val isUserAdmin = dao.isUserRoleAdmin(UserInstrumentation.givenUserList()[0].id)
        assertFalse(isUserAdmin)
    }

    @Test
    fun `getUserHashableByUsername where user does not exist, return null`() = runTest {
        val userHashable = dao.getUserHashableByUsername("hello@example.be")
        assertNull(userHashable)
    }

    @Test
    fun `getUserHashableByUsername where user does exist, return correct content`() = runTest {
        val userHashable = dao.getUserHashableByUsername(UserInstrumentation.givenUserList()[0].username)

        assertThat(userHashable).matches {
            it?.username == UserInstrumentation.givenUserList()[0].username &&
                    it.password == UserInstrumentation.givenUserList()[0].password
        }
    }
}
