package com.cbconnectit

import com.cbconnectit.plugins.configureDatabase
import com.cbconnectit.plugins.configureHTTP
import com.cbconnectit.plugins.configureKoin
import com.cbconnectit.plugins.configureMonitoring
import com.cbconnectit.plugins.configureRouting
import com.cbconnectit.plugins.configureSecurity
import com.cbconnectit.plugins.configureSerialization
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    configureHTTP()
    configureKoin()
    configureDatabase()
    configureMonitoring() // callLogging
    configureSerialization() // contentNegotiation
    configureSecurity()
    configureRouting() // routing + statusPages
}
