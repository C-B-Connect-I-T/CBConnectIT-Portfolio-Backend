package com.cbconnectit.controllers.auth

import com.cbconnectit.data.dto.requests.CreateTokenDto

object AuthInstrumentation {
    fun givenAnInvalidCreateToken() = CreateTokenDto("", "")
    fun givenAnInvalidCreateTokenWithoutPassword() = CreateTokenDto("john", "")
    fun givenAValidEmailCreateToken() = CreateTokenDto("john@example.be", "Test1234")
    fun givenAValidUsernameCreateToken() = CreateTokenDto("john", "Test1234")
}