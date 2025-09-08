package com.cbconnectit.modules.auth

import com.cbconnectit.data.dto.requests.CreateTokenDto
import com.cbconnectit.data.dto.requests.hasData
import com.cbconnectit.data.dto.responses.CredentialsResponse
import com.cbconnectit.domain.interfaces.IUserDao
import com.cbconnectit.plugins.dbQuery
import com.cbconnectit.statuspages.ErrorInvalidCredentials
import com.cbconnectit.statuspages.ErrorInvalidParameters
import com.cbconnectit.utils.PasswordManagerContract

class AuthControllerImpl(
    private val userDao: IUserDao,
    private val tokenProvider: TokenProvider,
    private val passwordManager: PasswordManagerContract
) : AuthController {

    override suspend fun authorizeUser(tokenDto: CreateTokenDto): CredentialsResponse = dbQuery {
        if (!tokenDto.hasData()) throw ErrorInvalidParameters

        val userHashable = userDao.getUserHashableByUsername(tokenDto.username) ?: throw ErrorInvalidCredentials

        val isValidPassword = passwordManager.validatePassword(tokenDto.password, userHashable.password ?: "")

        if (!isValidPassword) throw ErrorInvalidCredentials

        tokenProvider.createTokens(userHashable.copy(password = null))
    }
}

interface AuthController {
    suspend fun authorizeUser(tokenDto: CreateTokenDto): CredentialsResponse
}
