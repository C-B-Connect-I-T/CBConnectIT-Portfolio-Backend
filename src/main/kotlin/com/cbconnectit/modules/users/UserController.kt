package com.cbconnectit.modules.users

import com.cbconnectit.data.dto.requests.user.InsertNewUser
import com.cbconnectit.data.dto.requests.user.UpdatePassword
import com.cbconnectit.data.dto.requests.user.UpdateUser
import com.cbconnectit.data.dto.requests.user.UserDto
import com.cbconnectit.domain.interfaces.IUserDao
import com.cbconnectit.domain.models.user.toDto
import com.cbconnectit.plugins.dbQuery
import com.cbconnectit.statuspages.ErrorFailedCreate
import com.cbconnectit.statuspages.ErrorFailedDelete
import com.cbconnectit.statuspages.ErrorFailedUpdate
import com.cbconnectit.statuspages.ErrorInvalidCredentials
import com.cbconnectit.statuspages.ErrorInvalidParameters
import com.cbconnectit.statuspages.ErrorNotFound
import com.cbconnectit.statuspages.ErrorPasswordsDoNotMatch
import com.cbconnectit.statuspages.ErrorSameAsOldPassword
import com.cbconnectit.statuspages.ErrorUsernameExists
import com.cbconnectit.statuspages.ErrorWeakPassword
import com.cbconnectit.utils.PasswordManagerContract
import java.util.*

class UserControllerImpl(
    private val userDao: IUserDao,
    private val passwordEncryption: PasswordManagerContract
) : UserController {

    override suspend fun postUser(insertNewUser: InsertNewUser): UserDto = dbQuery {
        if (!insertNewUser.isValid) throw ErrorInvalidParameters

        val userUnique = userDao.userUnique(insertNewUser.username)
        if (!userUnique) throw ErrorUsernameExists

        if (!insertNewUser.isPasswordSame) throw ErrorPasswordsDoNotMatch

        if (!insertNewUser.isPasswordStrong) throw ErrorWeakPassword

        val encryptedPassword = passwordEncryption.encryptPassword(insertNewUser.password)

        userDao.insertUser(insertNewUser.copy(password = encryptedPassword, repeatPassword = null))?.toDto() ?: throw ErrorFailedCreate
    }

    override suspend fun getUserById(userId: UUID): UserDto = dbQuery {
        userDao.getUser(userId)?.toDto() ?: throw ErrorNotFound
    }

    override suspend fun updateUserById(userId: UUID, updateUser: UpdateUser): UserDto = dbQuery {
        // TODO: should we check for Admin or logged in user here as well?

        if (!updateUser.isValid) throw ErrorInvalidParameters

        userDao.updateUser(userId, updateUser)?.toDto() ?: throw ErrorFailedUpdate
    }

    override suspend fun updateUserPasswordById(userId: UUID, updatePassword: UpdatePassword): UserDto = dbQuery {
        // TODO: should we check for Admin or logged in user here as well?

        val userHashable = userDao.getUserHashableById(userId) ?: throw ErrorNotFound

        if (listOf(updatePassword.password, updatePassword.repeatPassword).any { it == updatePassword.oldPassword }) throw ErrorSameAsOldPassword

        val isValidPassword = passwordEncryption.validatePassword(updatePassword.oldPassword, userHashable.password ?: "")
        if (!isValidPassword) throw ErrorInvalidCredentials

        if (!updatePassword.isPasswordSame) throw ErrorPasswordsDoNotMatch

        if (!updatePassword.isPasswordStrong) throw ErrorWeakPassword

        val encryptedPassword = passwordEncryption.encryptPassword(updatePassword.password)

        userDao.updateUserPassword(userId, encryptedPassword)?.toDto() ?: throw ErrorFailedUpdate
    }

    override suspend fun deleteUserById(userId: UUID) {
        // TODO: should we check for Admin or logged in user here as well?

        return dbQuery {
            val deleted = userDao.deleteUser(userId)
            if (!deleted) throw ErrorFailedDelete
        }
    }
}

interface UserController {
    suspend fun postUser(insertNewUser: InsertNewUser): UserDto
    suspend fun getUserById(userId: UUID): UserDto
    suspend fun updateUserById(userId: UUID, updateUser: UpdateUser): UserDto
    suspend fun updateUserPasswordById(userId: UUID, updatePassword: UpdatePassword): UserDto
    suspend fun deleteUserById(userId: UUID)
}
