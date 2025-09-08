package com.cbconnectit.controllers.auth

import com.cbconnectit.controllers.BaseControllerTest
import com.cbconnectit.controllers.auth.AuthInstrumentation.givenAValidEmailCreateToken
import com.cbconnectit.controllers.auth.AuthInstrumentation.givenAValidUsernameCreateToken
import com.cbconnectit.controllers.auth.AuthInstrumentation.givenAnInvalidCreateToken
import com.cbconnectit.controllers.auth.AuthInstrumentation.givenAnInvalidCreateTokenWithoutPassword
import com.cbconnectit.data.dto.responses.CredentialsResponse
import com.cbconnectit.domain.interfaces.IUserDao
import com.cbconnectit.domain.models.user.User
import com.cbconnectit.modules.auth.AuthController
import com.cbconnectit.modules.auth.AuthControllerImpl
import com.cbconnectit.modules.auth.TokenProvider
import com.cbconnectit.statuspages.ErrorInvalidCredentials
import com.cbconnectit.statuspages.ErrorInvalidParameters
import com.cbconnectit.utils.PasswordManagerContract
import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AuthControllerTest : BaseControllerTest() {

    private val userDao: IUserDao = mockk()
    private val tokenProvider: TokenProvider = mockk()
    private val passwordEncryption: PasswordManagerContract = mockk()
    private val controller: AuthController by lazy { AuthControllerImpl(userDao, tokenProvider, passwordEncryption) }

    @BeforeEach
    override fun before() {
        super.before()
        clearMocks(userDao, tokenProvider, passwordEncryption)
    }

    @Test
    fun `when authorizing user with invalid data, we throw exception`() {
        coEvery { userDao.getUserHashableByUsername(any()) } returns null

        assertThrows<ErrorInvalidParameters> {
            runBlocking { controller.authorizeUser(givenAnInvalidCreateToken()) }
        }
    }

    @Test
    fun `when authorizing user with no password, we throw exception`() {
        coEvery { userDao.getUserHashableByUsername(any()) } returns null

        assertThrows<ErrorInvalidParameters> {
            runBlocking { controller.authorizeUser(givenAnInvalidCreateTokenWithoutPassword()) }
        }
    }

    @Test
    fun `when authorizing user with a valid email as a username which does not exist, we throw exception`() {
        coEvery { userDao.getUserHashableByUsername(any()) } returns null

        assertThrows<ErrorInvalidCredentials> {
            runBlocking { controller.authorizeUser(givenAValidEmailCreateToken()) }
        }
    }

    @Test
    fun `when authorizing user with a valid email as a username and password is not correct, we throw exception`() {
        coEvery { userDao.getUserHashableByUsername(any()) } returns User()
        coEvery { passwordEncryption.validatePassword(any(), any()) } returns false

        assertThrows<ErrorInvalidCredentials> {
            runBlocking { controller.authorizeUser(givenAValidEmailCreateToken()) }
        }
    }

    @Test
    fun `when authorizing user with a valid email as a username when everything is valid, we return an accessToken`() {
        val createdToken = CredentialsResponse("", "", 0)

        coEvery { userDao.getUserHashableByUsername(any()) } returns User()
        coEvery { passwordEncryption.validatePassword(any(), any()) } returns true
        coEvery { tokenProvider.createTokens(any()) } returns createdToken

        runBlocking {
            val response = controller.authorizeUser(givenAValidEmailCreateToken())
            assertThat(response).isEqualTo(createdToken)
        }
    }

    @Test
    fun `when authorizing user with valid username and everything is valid, we return an accessToken`() {
        val createdToken = CredentialsResponse("", "", 0)

        coEvery { userDao.getUserHashableByUsername(any()) } returns User()
        coEvery { passwordEncryption.validatePassword(any(), any()) } returns true
        coEvery { tokenProvider.createTokens(any()) } returns createdToken

        runBlocking {
            val response = controller.authorizeUser(givenAValidUsernameCreateToken())
            assertThat(response).isEqualTo(createdToken)
        }
    }
}
