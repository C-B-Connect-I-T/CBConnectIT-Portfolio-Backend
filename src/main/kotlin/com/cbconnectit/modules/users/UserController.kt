package com.cbconnectit.modules.users

import com.cbconnectit.data.dto.requests.user.*
import com.cbconnectit.domain.interfaces.IUserDao
import com.cbconnectit.domain.models.user.toDto
import com.cbconnectit.modules.BaseController
import com.cbconnectit.plugins.dbQuery
import com.cbconnectit.statuspages.*
import com.cbconnectit.utils.PasswordManagerContract
import org.koin.core.component.inject

class UserControllerImpl : BaseController(), UserController {

    private val userDao by inject<IUserDao>()
    private val passwordEncryption by inject<PasswordManagerContract>()

    override suspend fun postUser(insertNewUser: InsertNewUser): UserDto = dbQuery {
        if (!insertNewUser.isValid) throw ErrorInvalidParameters

        val userUnique = userDao.userUnique(insertNewUser.username)
        if (!userUnique) throw ErrorUsernameExists

        if (!insertNewUser.isPasswordSame) throw ErrorPasswordsDoNotMatch

        if (!insertNewUser.isPasswordStrong) throw ErrorWeakPassword

        val encryptedPassword = passwordEncryption.encryptPassword(insertNewUser.password)

        userDao.insertUser(insertNewUser.copy(password = encryptedPassword, repeatPassword = null))?.toDto() ?: throw ErrorFailedCreate
    }

    override suspend fun getUserById(userId: Int): UserDto = dbQuery {
        userDao.getUser(userId)?.toDto() ?: throw ErrorNotFound
    }

    override suspend fun updateUserById(userId: Int, updateUser: UpdateUser): UserDto = dbQuery {
        //TODO: should we check for Admin or logged in user here as well?

        if (!updateUser.isValid) throw ErrorInvalidParameters

        userDao.updateUser(userId, updateUser)?.toDto() ?: throw ErrorFailedUpdate
    }

    override suspend fun updateUserPasswordById(userId: Int, updatePassword: UpdatePassword): UserDto = dbQuery {
        //TODO: should we check for Admin or logged in user here as well?

        val userHashable = userDao.getUserHashableById(userId) ?: throw ErrorNotFound

        if (listOf(updatePassword.password, updatePassword.repeatPassword).any { it == updatePassword.oldPassword }) throw ErrorSameAsOldPassword

        val isValidPassword = passwordEncryption.validatePassword(updatePassword.oldPassword, userHashable.password ?: "")
        if (!isValidPassword) throw ErrorInvalidCredentials

        if (!updatePassword.isPasswordSame) throw ErrorPasswordsDoNotMatch

        if (!updatePassword.isPasswordStrong) throw ErrorWeakPassword

        val encryptedPassword = passwordEncryption.encryptPassword(updatePassword.password)

        userDao.updateUserPassword(userId, encryptedPassword)?.toDto() ?: throw ErrorFailedUpdate
    }

    override suspend fun deleteUserById(userId: Int) {
        //TODO: should we check for Admin or logged in user here as well?

        return dbQuery {
            val deleted = userDao.deleteUser(userId)
            if (!deleted) throw ErrorFailedDelete
        }
    }
}

interface UserController {
    suspend fun postUser(insertNewUser: InsertNewUser): UserDto
    suspend fun getUserById(userId: Int): UserDto
    suspend fun updateUserById(userId: Int, updateUser: UpdateUser): UserDto
    suspend fun updateUserPasswordById(userId: Int, updatePassword: UpdatePassword): UserDto
    suspend fun deleteUserById(userId: Int)
}
