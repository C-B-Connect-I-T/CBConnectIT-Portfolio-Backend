package com.cbconnectit.data.database.tables

object Constants {
    const val CLIENT_TYPE_HEADER = "X-Client-Type"
    const val AUTH_METHOD_HEADER = "X-Auth-Method"
    const val smallerTextSize = 100
    const val normalTextSize = 255
    const val mediumTextSize = 500
    const val bigTextSize = 1000

    val trustedWebOrigins = setOf(
        "localhost",
        "0.0.0.0",
        "www.dev.cb-connect-it.com",
        "dev.cb-connect-it.com",
        "www.stag.cb-connect-it.com",
        "stag.cb-connect-it.com",
        "www.cb-connect-it.com",
        "cb-connect-it.com"
    )
}
