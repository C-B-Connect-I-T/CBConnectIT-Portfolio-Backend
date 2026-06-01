package com.cbconnectit.data.database.dao.users

import com.cbconnectit.data.database.dao.BaseDaoTest
import com.cbconnectit.data.database.dao.UserDaoImpl
import com.cbconnectit.data.database.dao.users.UserInstrumentation.givenAValidInsertUserBody
import com.cbconnectit.data.database.dao.users.UserInstrumentation.givenAValidUpdateUserBody
import com.cbconnectit.domain.models.user.UserRoles
import kotlinx.coroutines.delay
import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.*
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class UserDaoImplTest : BaseDaoTest() {

    private val dao = UserDaoImpl()

    @Test
    fun `getUser where item exists, return correct user`() = runTest(shouldSeedData = false) {
        val validUser = givenAValidInsertUserBody()
        val userId = dao.insertUser(validUser)?.id
        val user = dao.getUser(userId!!)

        assertThat(user).matches {
            it?.username == validUser.username &&
                    it.fullName == validUser.fullName &&
                    it.role == UserRoles.User
        }
    }

    @Test
    fun `getUser where item does not exist, return 'null'`() = runTest(shouldSeedData = false) {
        val user = dao.getUser(UUID.randomUUID())

        assertNull(user)
    }

    @Test
    fun `insertUser where information is correct, database is storing user and returning correct content`() = runTest(shouldSeedData = false) {
        val validUser = givenAValidInsertUserBody()
        val user = dao.insertUser(validUser)

        assertThat(user).matches {
            it?.username == validUser.username &&
                    it.fullName == validUser.fullName &&
                    it.role == UserRoles.User &&
                    it.createdAt == it.updatedAt
        }
    }

    @Test
    fun `insertUser where the same data exists, database will give error`() = runTest(shouldSeedData = false) {
        val validUser = givenAValidInsertUserBody()
        dao.insertUser(validUser)

        assertThrows<ExposedSQLException> {
            dao.insertUser(givenAValidInsertUserBody())
        }
    }

    @Test
    fun `updateUser where information is correct, database is storing information and returning the correct content`() = runTest(shouldSeedData = false) {
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
                    it.createdAt != it.updatedAt
        }
    }

    @Test
    fun `updateUser where information is correct but user with id does not exist, database does nothing and returns 'null'`() = runTest(shouldSeedData = false) {
        val validUser = givenAValidUpdateUserBody()
        val user = dao.updateUser(UUID.randomUUID(), validUser)

        assertNull(user)
    }

    @Test
    fun `updateUserPassword where information is correct, database is storing information`() = runTest(shouldSeedData = false) {
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

    @Test
    fun `getUsers but none exist return empty list`() = runTest(shouldSeedData = false) {
        val list = dao.getUsers()
        assertThat(list).isEmpty()
    }

    @Test
    fun `getUsers return the list`() = runTest(shouldSeedData = false) {
        dao.insertUser(givenAValidInsertUserBody())
        val list = dao.getUsers()
        assertThat(list).hasSize(1)
    }

    @Test
    fun `deleteUser for id that exists, return true`() = runTest(shouldSeedData = false) {
        val id = dao.insertUser(givenAValidInsertUserBody())?.id
        val deleted = dao.deleteUser(id!!)
        assertTrue(deleted)
    }

    @Test
    fun `deleteUser for id that does not exists, return false`() = runTest(shouldSeedData = false) {
        val deleted = dao.deleteUser(UUID.randomUUID())
        assertFalse(deleted)
    }

    @Test
    fun `userUnique where user does not exist, return true`() = runTest(shouldSeedData = false) {
        val unique = dao.userUnique("hell@example")
        assertTrue(unique)
    }

    @Test
    fun `userUnique where user does exist, return false`() = runTest(shouldSeedData = false) {
        dao.insertUser(givenAValidInsertUserBody())
        val unique = dao.userUnique("christiano@example")
        assertFalse(unique)
    }

    @Test
    fun `isUserRoleAdmin where user does not exist, return false`() = runTest(shouldSeedData = false) {
        val isUserAdmin = dao.isUserRoleAdmin(UUID.randomUUID())
        assertFalse(isUserAdmin)
    }

    @Test
    fun `isUserRoleAdmin where user does exist but is not admin, return false`() = runTest(shouldSeedData = false) {
        val uuid = dao.insertUser(givenAValidInsertUserBody())?.id
        val isUserAdmin = dao.isUserRoleAdmin(uuid!!)
        assertFalse(isUserAdmin)
    }

    @Test
    fun `getUserHashableByUsername where user does not exist, return null`() = runTest(shouldSeedData = false) {
        val userHashable = dao.getUserHashableByUsername("hello@example.be")
        assertNull(userHashable)
    }

    @Test
    fun `getUserHashableByUsername where user does exist, return correct content`() = runTest(shouldSeedData = false) {
        val validInsertUser = givenAValidInsertUserBody()
        dao.insertUser(validInsertUser)
        val userHashable = dao.getUserHashableByUsername("christiano@example")

        assertThat(userHashable).matches {
            it?.username == validInsertUser.username &&
                    it.password == userHashable?.password
        }
    }
}
