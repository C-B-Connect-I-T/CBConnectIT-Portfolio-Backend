package com.cbconnectit.modules.users

import com.cbconnectit.data.dto.requests.user.InsertNewUser
import com.cbconnectit.data.dto.requests.user.UpdatePassword
import com.cbconnectit.data.dto.requests.user.UpdateUser
import com.cbconnectit.data.dto.requests.user.UserDto
import com.cbconnectit.domain.interfaces.IUserDao
import com.cbconnectit.domain.models.user.toDto
import com.cbconnectit.plugins.dbTransactionalQuery
import com.cbconnectit.plugins.statuspages.ErrorFailedCreate
import com.cbconnectit.plugins.statuspages.ErrorFailedDelete
import com.cbconnectit.plugins.statuspages.ErrorFailedUpdate
import com.cbconnectit.plugins.statuspages.ErrorInvalidCredentials
import com.cbconnectit.plugins.statuspages.ErrorInvalidParameters
import com.cbconnectit.plugins.statuspages.ErrorNotFound
import com.cbconnectit.plugins.statuspages.ErrorPasswordsDoNotMatch
import com.cbconnectit.plugins.statuspages.ErrorSameAsOldPassword
import com.cbconnectit.plugins.statuspages.ErrorUsernameExists
import com.cbconnectit.plugins.statuspages.ErrorWeakPassword
import com.cbconnectit.utils.PasswordManagerContract
import java.util.*

class UserControllerImpl(
    private val userDao: IUserDao,
    private val passwordEncryption: PasswordManagerContract
) : UserController {

    override suspend fun postUser(insertNewUser: InsertNewUser): UserDto = dbTransactionalQuery {
        if (!insertNewUser.isValid) throw ErrorInvalidParameters

        val userUnique = userDao.userUnique(insertNewUser.username)
        if (!userUnique) throw ErrorUsernameExists

        if (!insertNewUser.isPasswordSame) throw ErrorPasswordsDoNotMatch

        if (!insertNewUser.isPasswordStrong) throw ErrorWeakPassword

        val encryptedPassword = passwordEncryption.encryptPassword(insertNewUser.password)

        userDao.insertUser(insertNewUser.copy(password = encryptedPassword, repeatPassword = null))?.toDto() ?: throw ErrorFailedCreate
    }

    override suspend fun getUserById(userId: UUID): UserDto = dbTransactionalQuery {
        userDao.getUser(userId)?.toDto() ?: throw ErrorNotFound
    }

    override suspend fun updateUserById(userId: UUID, updateUser: UpdateUser): UserDto = dbTransactionalQuery {
        // TODO: should we check for Admin or logged in user here as well?

        if (!updateUser.isValid) throw ErrorInvalidParameters

        userDao.updateUser(userId, updateUser)?.toDto() ?: throw ErrorFailedUpdate
    }

    override suspend fun updateUserPasswordById(userId: UUID, updatePassword: UpdatePassword): UserDto = dbTransactionalQuery {
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

        return dbTransactionalQuery {
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
