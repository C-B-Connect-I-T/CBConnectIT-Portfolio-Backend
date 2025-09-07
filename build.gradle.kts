plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ktor)
    alias(libs.plugins.detekt)
}

group = "com.cbconnectit"
version = "0.0.1"

application {
    mainClass.set("com.cbconnectit.ApplicationKt")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.serialization.gson)
    implementation(libs.ktor.server.auth)
    implementation(libs.ktor.server.auth.jwt)
    implementation(libs.ktor.server.netty)
    implementation(libs.logback)
    implementation(libs.ktor.server.cors)
    implementation(libs.ktor.server.status.pages)
    implementation(libs.ktor.server.call.logging)
    implementation(libs.ktor.server.request.validation)
    implementation(libs.ktor.server.swagger)

    detektPlugins(libs.detekt.formatting)

    // Database
    implementation(libs.exposed.core)
    implementation(libs.exposed.jdbc)
//    implementation(libs.exposed.dao) // TODO: not really using this at the moment I think...
    implementation(libs.exposed.java.time)
    implementation(libs.mysql)
    implementation(libs.jbcrypt)

    // Firebase Admin SDK
//    implementation(libs.firebase.admin)

    // Koin for Ktor; make sure you go to File...Project Structure
    // and switch to Java 11
    implementation(libs.koin.ktor)
//    implementation(libs.koin.logger.slf4j)

    implementation(libs.slugify)

    testImplementation(libs.sqlite)
    testImplementation(libs.koin.test)
    testImplementation(libs.ktor.server.test.host)
    testImplementation(libs.assertj.core)
    testImplementation(libs.mockk)
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.platform.launcher)
    testImplementation(libs.junit.jupiter.engine)
}

tasks {
    test {
        useJUnitPlatform()
        testLogging {
            events("passed", "skipped", "failed")
        }
    }
}
