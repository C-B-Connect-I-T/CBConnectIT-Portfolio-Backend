package com.cbconnectit.routing.testimonials

import com.cbconnectit.data.dto.requests.testimonial.InsertNewTestimonial
import com.cbconnectit.data.dto.requests.testimonial.TestimonialDto
import com.cbconnectit.data.dto.requests.testimonial.UpdateTestimonial
import com.cbconnectit.utils.toDatabaseString
import java.time.LocalDateTime
import java.util.*

object TestimonialInstrumentation {
    fun givenAValidInsertTestimonial() = InsertNewTestimonial("", "New Parent Testimonial", "", companyId = "00000000-0000-0000-0000-000000000001", jobPositionId = "00000000-0000-0000-0000-000000000001")
    fun givenAValidUpdateTestimonialBody() = UpdateTestimonial("", "Updated Parent testimonial", "", companyId = "00000000-0000-0000-0000-000000000001", jobPositionId = "00000000-0000-0000-0000-000000000001")

    fun givenAnEmptyInsertTestimonialBody() = InsertNewTestimonial("    ", "      ", "", companyId = "00000000-0000-0000-0000-000000000001", jobPositionId = "00000000-0000-0000-0000-000000000001")

    fun givenTestimonialList() = listOf(
        givenATestimonial("First testimonial"),
        givenATestimonial("Second testimonial"),
        givenATestimonial("Third testimonial"),
        givenATestimonial("Fourth testimonial"),
    )

    fun givenATestimonial(review: String = "First testimonial") = run {
        val time = LocalDateTime.now().toDatabaseString()
        TestimonialDto(
            id = UUID.randomUUID().toString(),
            review = review,
            createdAt = time,
            updatedAt = time
        )
    }
}
