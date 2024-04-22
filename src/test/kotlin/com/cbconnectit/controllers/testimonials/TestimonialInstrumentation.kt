package com.cbconnectit.controllers.testimonials

import com.cbconnectit.data.dto.requests.testimonial.InsertNewTestimonial
import com.cbconnectit.data.dto.requests.testimonial.UpdateTestimonial
import com.cbconnectit.domain.models.testimonial.Testimonial
import java.util.*

object TestimonialInstrumentation {

    fun givenAnInvalidInsertTestimonial() = InsertNewTestimonial("  ", "", "", companyId = "00000000-0000-0000-0000-000000000001", jobPositionId = "00000000-0000-0000-0000-000000000001")
    fun givenAnInvalidUpdateTestimonial() = UpdateTestimonial("  ", "", "", companyId = "00000000-0000-0000-0000-000000000001", jobPositionId = "00000000-0000-0000-0000-000000000001")
    fun givenAValidInsertTestimonial() = InsertNewTestimonial("https://www.google.be", "First Testimonial", "First name", companyId = "00000000-0000-0000-0000-000000000001", jobPositionId = "00000000-0000-0000-0000-000000000001")
    fun givenAValidInsertNewTestimonialWithParent() = InsertNewTestimonial("Sub testimonial", "", "", companyId = "00000000-0000-0000-0000-000000000001", jobPositionId = "00000000-0000-0000-0000-000000000001")
    fun givenAValidUpdateTestimonial() = UpdateTestimonial("Updated testimonial", "Updated testimonial", "Updated name", companyId = "00000000-0000-0000-0000-000000000001", jobPositionId = "00000000-0000-0000-0000-000000000001")
    fun givenAValidUpdateTestimonialWithParent() = UpdateTestimonial("Sub testimonial", "", "", companyId = "00000000-0000-0000-0000-000000000001", jobPositionId = "00000000-0000-0000-0000-000000000001")

    fun givenTestimonialList() = listOf(
        givenATestimonial(id = UUID.fromString("00000000-0000-0000-0000-000000000001"), review = "First Testimonial"),
        givenATestimonial(id = UUID.fromString("00000000-0000-0000-0000-000000000002"), review = "Second Testimonial"),
        givenATestimonial(id = UUID.fromString("00000000-0000-0000-0000-000000000001"), review = "Third Testimonial"),
        givenATestimonial(id = UUID.fromString("00000000-0000-0000-0000-000000000001"), review = "Fourth Testimonial"),
    )

    fun givenATestimonial(id: UUID = UUID.randomUUID(), review: String = "Testimonial") = Testimonial(id = id, review = review)
}
