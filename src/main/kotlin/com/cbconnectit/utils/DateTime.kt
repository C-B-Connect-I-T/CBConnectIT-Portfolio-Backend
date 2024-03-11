package com.cbconnectit.utils

import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.Temporal

private const val DATE_FORMAT = "yyyy-MM-dd HH:mm:ss z"

fun Temporal.toDatabaseString(): String = DateTimeFormatter.ofPattern(DATE_FORMAT).withZone(ZoneId.of("UTC")).format(this)
fun String.toLocalDateTime(): LocalDateTime = LocalDateTime.parse(this, DateTimeFormatter.ofPattern(DATE_FORMAT).withZone(ZoneId.of("UTC")))
