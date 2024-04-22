package com.cbconnectit.domain.interfaces

import com.cbconnectit.data.dto.requests.user.InsertNewUser
import com.cbconnectit.data.dto.requests.user.UpdateUser
import com.cbconnectit.domain.models.user.User
import java.util.*

interface IUserDao {

    fun getUser(id: UUID): User?
    fun getUserHashableById(id: UUID): User?
    fun getUserHashableByUsername(username: String): User?
    fun getUsers(): List<User>
    fun insertUser(user: InsertNewUser): User?

    fun updateUser(id: UUID, user: UpdateUser): User?
    fun deleteUser(id: UUID): Boolean
    fun userUnique(username: String): Boolean
    fun isUserRoleAdmin(userId: UUID): Boolean
    fun updateUserPassword(userId: UUID, updatePassword: String): User?
}
