package com.cbconnectit.data.database.tables

import com.cbconnectit.domain.models.testimonial.Testimonial
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.javatime.CurrentDateTime
import org.jetbrains.exposed.sql.javatime.datetime

object TestimonialsTable : UUIDTable() {
    val imageUrl = varchar("image_url", 255)
    val fullName = varchar("full_name", 255)
    val jobPositionId = reference("job_position_id", JobPositionsTable, ReferenceOption.CASCADE)
    val review = text("review")
    val companyId = reference("company_id", CompaniesTable, ReferenceOption.CASCADE)
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)
    val updatedAt = datetime("updated_at").defaultExpression(CurrentDateTime)
}

fun ResultRow.toTestimonial() = Testimonial(
    id = this[TestimonialsTable.id].value,
    imageUrl = this[TestimonialsTable.imageUrl],
    fullName = this[TestimonialsTable.fullName],
    review = this[TestimonialsTable.review],
    jobPosition = this.toJobPosition(),
    company = this.toCompany(),
    createdAt = this[TestimonialsTable.createdAt],
    updatedAt = this[TestimonialsTable.updatedAt],
)

fun Iterable<ResultRow>.toTestimonials() = this.map { it.toTestimonial() }
fun Iterable<ResultRow>.toTestimonial() = this.firstOrNull()?.toTestimonial()