package com.cbconnectit.utils

import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.Temporal

private const val DATE_FORMAT = "yyyy-MM-dd HH:mm:ss z"

fun Temporal.toDatabaseString(): String = DateTimeFormatter.ISO_DATE_TIME.withZone(ZoneId.of("UTC")).format(this)
fun String.toLocalDateTime(): LocalDateTime = LocalDateTime.parse(this, DateTimeFormatter.ISO_DATE_TIME.withZone(ZoneId.of("UTC")))
