package com.cbconnectit.data.database.dao.testimonials

import com.cbconnectit.data.dto.requests.testimonial.InsertNewTestimonial
import com.cbconnectit.data.dto.requests.testimonial.UpdateTestimonial

object TestimonialInstrumentation {

    fun givenAValidInsertTestimonialBody() = InsertNewTestimonial(review = "First Testimonial", fullName = "", imageUrl = "", jobPositionId = "00000000-0000-0000-0000-000000000001", companyId = "00000000-0000-0000-0000-000000000001")
    fun givenAValidSecondInsertTestimonialBody() = InsertNewTestimonial(review = "Second Testimonial", fullName = "", imageUrl = "", jobPositionId = "00000000-0000-0000-0000-000000000001", companyId = "00000000-0000-0000-0000-000000000001")

    fun givenAValidUpdateTestimonialBody() = UpdateTestimonial(review = "Updated Testimonial", fullName = "", imageUrl = "", jobPositionId = "00000000-0000-0000-0000-000000000001", companyId = "00000000-0000-0000-0000-000000000001")

    fun givenAnEmptyUpdateTestimonialBody() = UpdateTestimonial(review = "   ", fullName = "", imageUrl = "", jobPositionId = "00000000-0000-0000-0000-000000000001", companyId = "00000000-0000-0000-0000-000000000001")
    fun givenAnUnknownTestimonial() = InsertNewTestimonial(review = "Unknown", fullName = "", imageUrl = "", jobPositionId = "00000000-0000-0000-0000-000000000001", companyId = "00000000-0000-0000-0000-000000000001")
}
