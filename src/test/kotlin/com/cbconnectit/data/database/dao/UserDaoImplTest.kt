package com.cbconnectit.data.database.dao

import com.cbconnectit.data.database.dao.UserDaoImpl
import com.cbconnectit.data.database.instrumentation.UserInstrumentation.givenAValidInsertUserBody
import com.cbconnectit.data.database.instrumentation.UserInstrumentation.givenAValidUpdateUserBody
import com.cbconnectit.data.database.tables.UsersTable
import com.cbconnectit.domain.models.user.UserRoles
import kotlinx.coroutines.delay
import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class UserDaoImplTest : BaseDaoTest() {

    private val dao = UserDaoImpl()

    @Test
    fun `getUser where item exists, return correct user`() {
        withTables(UsersTable) {
            val validUser = givenAValidInsertUserBody()
            val userId = dao.insertUser(validUser)?.id
            val user = dao.getUser(userId!!)

            assertThat(user).matches {
                it?.username == validUser.username &&
                        it.fullName == validUser.fullName &&
                        it.role == UserRoles.User
            }
        }
    }

    @Test
    fun `getUser where item does not exist, return 'null'`() {
        withTables(UsersTable) {
            val user = dao.getUser(803)

            assertNull(user)
        }
    }

    @Test
    fun `insertUser where information is correct, database is storing user and returning correct content`() {
        withTables(UsersTable) {
            val validUser = givenAValidInsertUserBody()
            val user = dao.insertUser(validUser)

            assertThat(user).matches {
                it?.username == validUser.username &&
                        it.fullName == validUser.fullName &&
                        it.role == UserRoles.User &&
                        it.id == 1 &&
                        it.createdAt == it.updatedAt
            }
        }
    }

    @Test
    fun `insertUser where the same data exists, database will give error`() {
        withTables(UsersTable) {
            val validUser = givenAValidInsertUserBody()
            dao.insertUser(validUser)

            assertThrows<ExposedSQLException> {
                dao.insertUser(givenAValidInsertUserBody())
            }
        }
    }

    @Test
    fun `updateUser where information is correct, database is storing information and returning the correct content`() {
        withTables(UsersTable) {
            val validUser = givenAValidInsertUserBody()
            val userId = dao.insertUser(validUser)?.id

            // adding a delay so there is a clear difference between `updatedAt` and `createdAt`
            delay(1000)

            val validUpdateUser = givenAValidUpdateUserBody()
            val user = dao.updateUser(userId!!, validUpdateUser)

            assertThat(user).matches {
                it?.username == validUpdateUser.username &&
                        it?.fullName == validUpdateUser.fullName &&
                        it?.role == UserRoles.User &&
                        it.id == 1 &&
                        it.createdAt != it.updatedAt
            }
        }
    }

    @Test
    fun `updateUser where information is correct but user with id does not exist, database does nothing and returns 'null'`() {
        withTables(UsersTable) {
            val validUser = givenAValidUpdateUserBody()
            val user = dao.updateUser(203, validUser)

            assertNull(user)
        }
    }

    @Test
    fun `updateUserPassword where information is correct, database is storing information`() {
        withTables(UsersTable) {
            val validUser = givenAValidInsertUserBody()
            val userId = dao.insertUser(validUser)?.id

            // adding a delay so there is a clear difference between `updatedAt` and `createdAt`
            delay(1000)

            val validUpdateUserPassword = "new-hash"
            val user = dao.updateUserPassword(userId!!, validUpdateUserPassword)

            val hashedUser = dao.getUserHashableById(userId)

            assertThat(user).matches { it?.createdAt != it?.updatedAt }
            assertThat(hashedUser).matches {
                it?.username == validUser.username &&
                        it.password == validUpdateUserPassword
            }
        }
    }

    @Test
    fun `getUsers but none exist return empty list`() {
        withTables(UsersTable) {
            val list = dao.getUsers()
            assertThat(list).isEmpty()
        }
    }

    @Test
    fun `getUsers return the list`() {
        withTables(UsersTable) {
            dao.insertUser(givenAValidInsertUserBody())
            val list = dao.getUsers()
            assertThat(list).hasSize(1)
        }
    }

    @Test
    fun `deleteUser for id that exists, return true`() {
        withTables(UsersTable) {
            val id = dao.insertUser(givenAValidInsertUserBody())?.id
            val deleted = dao.deleteUser(id!!)
            assertTrue(deleted)
        }
    }

    @Test
    fun `deleteUser for id that does not exists, return false`() {
        withTables(UsersTable) {
            val deleted = dao.deleteUser(203)
            assertFalse(deleted)
        }
    }

    @Test
    fun `userUnique where user does not exist, return true`() {
        withTables(UsersTable) {
            val unique = dao.userUnique("hell@example")
            assertTrue(unique)
        }
    }

    @Test
    fun `userUnique where user does exist, return false`() {
        withTables(UsersTable) {
            dao.insertUser(givenAValidInsertUserBody())
            val unique = dao.userUnique("christiano@example")
            assertFalse(unique)
        }
    }

    @Test
    fun `isUserRoleAdmin where user does not exist, return false`() {
        withTables(UsersTable) {
            val isUserAdmin = dao.isUserRoleAdmin(839)
            assertFalse(isUserAdmin)
        }
    }

    @Test
    fun `isUserRoleAdmin where user does exist but is not admin, return false`() {
        withTables(UsersTable) {
            dao.insertUser(givenAValidInsertUserBody())
            val isUserAdmin = dao.isUserRoleAdmin(1)
            assertFalse(isUserAdmin)
        }
    }

    @Test
    fun `getUserHashableByUsername where user does not exist, return null`() {
        withTables(UsersTable) {
            val userHashable = dao.getUserHashableByUsername("hello@example.be")
            assertNull(userHashable)
        }
    }

    @Test
    fun `getUserHashableByUsername where user does exist, return correct content`() {
        withTables(UsersTable) {
            val validInsertUser = givenAValidInsertUserBody()
            dao.insertUser(validInsertUser)
            val userHashable = dao.getUserHashableByUsername("christiano@example")

            assertThat(userHashable).matches {
                it?.username == validInsertUser.username &&
                        it.password == userHashable?.password
            }
        }
    }
}