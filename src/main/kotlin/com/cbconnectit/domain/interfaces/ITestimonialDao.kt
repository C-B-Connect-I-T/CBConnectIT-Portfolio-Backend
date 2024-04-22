package com.cbconnectit.domain.interfaces

import com.cbconnectit.data.dto.requests.testimonial.InsertNewTestimonial
import com.cbconnectit.data.dto.requests.testimonial.UpdateTestimonial
import com.cbconnectit.domain.models.testimonial.Testimonial
import java.util.*

interface ITestimonialDao {

    fun getTestimonialById(id: UUID): Testimonial?
    fun getTestimonials(): List<Testimonial>
    fun insertTestimonial(insertNewTestimonial: InsertNewTestimonial): Testimonial?
    fun updateTestimonial(id: UUID, updateTestimonial: UpdateTestimonial): Testimonial?
    fun deleteTestimonial(id: UUID): Boolean
}
