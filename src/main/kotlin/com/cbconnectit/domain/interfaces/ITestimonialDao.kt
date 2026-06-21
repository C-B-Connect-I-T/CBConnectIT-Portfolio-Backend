package com.cbconnectit.domain.interfaces

import com.cbconnectit.data.dto.requests.testimonial.InsertTestimonial
import com.cbconnectit.data.dto.requests.testimonial.UpdateTestimonial
import com.cbconnectit.domain.models.testimonial.Testimonial
import java.util.*

interface ITestimonialDao {

    fun readById(id: UUID): Testimonial?
    fun readAll(): List<Testimonial>
    fun create(id: UUID, insertTestimonial: InsertTestimonial): UUID
    fun updateById(id: UUID, updateTestimonial: UpdateTestimonial): Boolean
    fun deleteById(id: UUID): Boolean
}
