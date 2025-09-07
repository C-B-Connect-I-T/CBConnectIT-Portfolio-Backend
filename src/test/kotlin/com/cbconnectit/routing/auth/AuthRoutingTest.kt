package com.cbconnectit.routing.auth

import com.cbconnectit.data.dto.requests.CreateTokenDto
import com.cbconnectit.data.dto.responses.CredentialsResponse
import com.cbconnectit.modules.auth.AuthController
import com.cbconnectit.modules.auth.authRouting
import com.cbconnectit.routing.BaseRoutingTest
import io.ktor.http.*
import io.ktor.server.routing.*
import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.mockk
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.koin.dsl.module

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AuthRoutingTest : BaseRoutingTest() {

    private val authController: AuthController = mockk()

    @BeforeAll
    fun setup() {
        koinModules = module {
            single { authController }
        }

        moduleList = {
            routing {
                authRouting(authController)
            }
        }
    }

    @BeforeEach
    fun clearMocks() {
        clearMocks(authController)
    }

    @Test
    fun `when fetching all categories, we return a list`() = withBaseTestApplication {
        val authResponse = CredentialsResponse("", "", 0)
        coEvery { authController.authorizeUser(any()) } returns authResponse

        val body = toJsonBody(CreateTokenDto("", ""))
        val response = doCall(HttpMethod.Post, "/oauth/token", body)

        Assertions.assertThat(response.status).isEqualTo(HttpStatusCode.OK)
        Assertions.assertThat(response.parseBody<CredentialsResponse>()).isEqualTo(authResponse)
    }
}
