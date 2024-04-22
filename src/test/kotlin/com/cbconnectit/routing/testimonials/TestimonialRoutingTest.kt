package com.cbconnectit.routing.testimonials

import com.cbconnectit.data.dto.requests.testimonial.TestimonialDto
import com.cbconnectit.modules.testimonials.TestimonialController
import com.cbconnectit.modules.testimonials.testimonialRouting
import com.cbconnectit.routing.AuthenticationInstrumentation
import com.cbconnectit.routing.BaseRoutingTest
import com.cbconnectit.routing.testimonials.TestimonialInstrumentation.givenATestimonial
import com.cbconnectit.routing.testimonials.TestimonialInstrumentation.givenAValidInsertTestimonial
import com.cbconnectit.routing.testimonials.TestimonialInstrumentation.givenAValidUpdateTestimonialBody
import com.cbconnectit.routing.testimonials.TestimonialInstrumentation.givenTestimonialList
import com.cbconnectit.statuspages.ErrorDuplicateEntity
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import org.koin.dsl.module

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestimonialRoutingTest: BaseRoutingTest() {

    private val testimonialController: TestimonialController = mockk()

    @BeforeAll
    fun setup() {
        koinModules = module {
            single { testimonialController }
        }
        moduleList = {
            install(Routing) {
                testimonialRouting()
            }
        }
    }

    @BeforeEach
    fun clearMocks() {
        clearMocks(testimonialController)
    }

    // <editor-fold desc="Get all testimonials">
    @Test
    fun `when fetching all testimonials, we return a list`() = withBaseTestApplication(
        AuthenticationInstrumentation()
    ) {
        coEvery { testimonialController.getTestimonials() } returns givenTestimonialList()

        val call = doCall(HttpMethod.Get, "/testimonials")

        call.also {
            assertThat(HttpStatusCode.OK).isEqualTo(it.response.status())
            val responseBody = it.response.parseBody(List::class.java)
            assertThat(responseBody).hasSize(4)
        }
    }
    // </editor-fold>

    // <editor-fold desc="Get specific testimonial">
    @Test
    fun `when fetching a specific testimonial that exists by id, we return that testimonial`() = withBaseTestApplication(
        AuthenticationInstrumentation()
    ) {
        val testimonialResponse = givenATestimonial()
        coEvery { testimonialController.getTestimonialById(any()) } returns testimonialResponse

        val call = doCall(HttpMethod.Get, "/testimonials/a63a20c4-14dd-4e11-9e87-5ab361a51f65")

        call.also {
            assertThat(HttpStatusCode.OK).isEqualTo(it.response.status())
            val responseBody = it.response.parseBody(TestimonialDto::class.java)
            assertThat(testimonialResponse).isEqualTo(responseBody)
        }
    }

    @Test
    fun `when fetching a specific testimonial by id that does not exists, we return error`() = withBaseTestApplication(
        AuthenticationInstrumentation()
    ) {
        coEvery { testimonialController.getTestimonialById(any()) } throws Exception()

        val exception = assertThrows<Exception>{
            doCall(HttpMethod.Get, "/testimonials/a63a20c4-14dd-4e11-9e87-5ab361a51f65")
        }

        assertThat(exception.message).isEqualTo(null)
    }
    // </editor-fold>

    // <editor-fold desc="Create new testimonial">
    @Test
    fun `when creating testimonial with successful insertion, we return response testimonial body`() = withBaseTestApplication(
        AuthenticationInstrumentation()
    ) {
        val testimonialResponse = givenATestimonial()
        coEvery { testimonialController.postTestimonial(any()) } returns testimonialResponse

        val body = toJsonBody(givenAValidInsertTestimonial())
        val call = doCall(HttpMethod.Post, "/testimonials", body)

        call.also {
            assertThat(HttpStatusCode.Created).isEqualTo(it.response.status())
            val responseBody = it.response.parseBody(TestimonialDto::class.java)
            assertThat(testimonialResponse).isEqualTo(responseBody)
        }
    }

    @Test
    fun `when creating testimonial already created, we return 409 error`() = withBaseTestApplication(
        AuthenticationInstrumentation()
    ) {
        coEvery { testimonialController.postTestimonial(any()) } throws ErrorDuplicateEntity

        val body = toJsonBody(givenAValidInsertTestimonial())
        val exception = assertThrows<ErrorDuplicateEntity> {
            doCall(HttpMethod.Post, "/testimonials", body)
        }
        assertThat(exception.message).isEqualTo(null)
    }
    // </editor-fold>

    // <editor-fold desc="Update testimonial">
    @Test
    fun `when updating testimonial with successful insertion, we return response testimonial body`() = withBaseTestApplication(
        AuthenticationInstrumentation()
    ) {
        val testimonialResponse = givenATestimonial()
        coEvery { testimonialController.updateTestimonialById(any(), any()) } returns testimonialResponse

        val body = toJsonBody(givenAValidUpdateTestimonialBody())
        val call = doCall(HttpMethod.Put, "/testimonials/a63a20c4-14dd-4e11-9e87-5ab361a51f65", body)

        call.also {
            assertThat(HttpStatusCode.OK).isEqualTo(it.response.status())
            val responseBody = it.response.parseBody(TestimonialDto::class.java)
            assertThat(testimonialResponse).isEqualTo(responseBody)
        }
    }

    @Test
    fun `when updating testimonial with wrong testimonialId, we return error`() = withBaseTestApplication(
        AuthenticationInstrumentation()
    ) {
        coEvery { testimonialController.updateTestimonialById(any(), any()) } throws Exception()

        val body = toJsonBody(givenAValidUpdateTestimonialBody())
        val exception = assertThrows<Exception> {
            doCall(HttpMethod.Put, "/testimonials/a63a20c4-14dd-4e11-9e87-5ab361a51f65", body)
        }
        assertThat(exception.message).isEqualTo(null)
    }
    // </editor-fold>

    // <editor-fold desc="Delete testimonial">
    @Test
    fun `when deleting testimonial successful, we return Ok response`() = withBaseTestApplication(
        AuthenticationInstrumentation()
    ) {
        coEvery { testimonialController.deleteTestimonialById(any()) } returns Unit

        val call = doCall(HttpMethod.Delete, "/testimonials/a63a20c4-14dd-4e11-9e87-5ab361a51f65")

        call.also {
            assertThat(HttpStatusCode.OK).isEqualTo(it.response.status())
        }
    }

    @Test
    fun `when deleting testimonial with wrong testimonialId, we return error`() = withBaseTestApplication(
        AuthenticationInstrumentation()
    ) {
        coEvery { testimonialController.deleteTestimonialById(any()) } throws Exception()

        val exception = assertThrows<Exception> {
            doCall(HttpMethod.Delete, "/testimonials/a63a20c4-14dd-4e11-9e87-5ab361a51f65")
        }
        assertThat(exception.message).isEqualTo(null)
    }
    // </editor-fold>
}
