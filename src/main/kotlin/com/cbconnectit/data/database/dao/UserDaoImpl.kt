package com.cbconnectit.data.database.dao

import com.cbconnectit.data.database.tables.UsersTable
import com.cbconnectit.data.database.tables.toUser
import com.cbconnectit.data.database.tables.toUserHashable
import com.cbconnectit.data.database.tables.toUsers
import com.cbconnectit.data.dto.requests.user.InsertNewUser
import com.cbconnectit.data.dto.requests.user.UpdateUser
import com.cbconnectit.domain.interfaces.IUserDao
import com.cbconnectit.domain.models.user.User
import com.cbconnectit.domain.models.user.UserRoles
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.javatime.CurrentDateTime
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import java.util.*

class UserDaoImpl : IUserDao {

    override fun getUser(id: UUID): User? =
        UsersTable.select { UsersTable.id eq id }.toUser()

    override fun getUserHashableById(id: UUID): User? =
        UsersTable.select { UsersTable.id eq id }.toUserHashable()

    override fun getUserHashableByUsername(username: String): User? =
        UsersTable.select { UsersTable.username eq username }.toUserHashable()

    override fun getUsers(): List<User> =
        UsersTable.selectAll().toUsers()

    override fun insertUser(user: InsertNewUser): User? {
        val userId = UsersTable.insertAndGetId {
            it[fullName] = user.fullName
            it[username] = user.username
            it[password] = user.password
        }.value

        return getUser(userId)
    }

    override fun updateUser(id: UUID, user: UpdateUser): User? {
        UsersTable.update({ UsersTable.id eq id }) {
            user.fullName?.let { last -> it[fullName] = last }
            user.username?.let { mail -> it[username] = mail }

            it[updatedAt] = CurrentDateTime
        }

        return getUser(id)
    }

    override fun deleteUser(id: UUID): Boolean =
        UsersTable.deleteWhere { UsersTable.id eq id } > 0

    override fun userUnique(username: String): Boolean =
        UsersTable.select { UsersTable.username eq username }.empty()

    override fun isUserRoleAdmin(userId: UUID): Boolean =
        UsersTable.select { UsersTable.id eq userId }.firstOrNull()?.get(UsersTable.role) == UserRoles.Admin

    override fun updateUserPassword(userId: UUID, updatePassword: String): User? {
        UsersTable.update({ UsersTable.id eq userId }) {
            it[password] = updatePassword
            it[updatedAt] = CurrentDateTime
        }

        return getUser(userId)
    }
}
