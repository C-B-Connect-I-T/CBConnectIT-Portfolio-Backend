val ktor_version: String by project
val kotlin_version: String by project
val logback_version: String by project

val exposed_version: String by project
val h2_version: String by project
val postgres_version: String by project
val koin_version: String by project

plugins {
    application
    kotlin("jvm") version "1.9.22"
    id("io.ktor.plugin") version "2.3.7"
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.22"
    id("io.gitlab.arturbosch.detekt") version("1.23.3")
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
    jcenter()
    maven {
        url = uri("https://dl.bintray.com/kotlin/ktor")
    }
    maven {
        url = uri("https://dl.bintray.com/kotlin/kotlinx")
    }
}

dependencies {
    implementation("io.ktor:ktor-server-content-negotiation-jvm")
    implementation("io.ktor:ktor-server-core-jvm")
    implementation("io.ktor:ktor-serialization-gson-jvm")
    implementation("io.ktor:ktor-server-auth-jvm")
    implementation("io.ktor:ktor-server-auth-jwt-jvm")
    implementation("io.ktor:ktor-server-netty-jvm")
    implementation("ch.qos.logback:logback-classic:$logback_version")
    implementation("io.ktor:ktor-server-cors-jvm")
    implementation("io.ktor:ktor-server-status-pages-jvm")
    implementation("io.ktor:ktor-server-call-logging-jvm")

//    detektPlugins("io.gitlab.arturbosch.detekt:detekt:1.23.3")
    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:1.23.3")

    // Unsure what this does!!
    implementation("io.ktor:ktor-server-host-common-jvm")

    implementation("io.ktor:ktor-server-swagger-jvm")


    // Database
    implementation("org.jetbrains.exposed:exposed-core:$exposed_version")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposed_version")
    implementation("org.jetbrains.exposed:exposed-dao:$exposed_version")
    implementation("org.jetbrains.exposed:exposed-java-time:$exposed_version")
    implementation("mysql:mysql-connector-java:8.0.33")
    implementation("org.postgresql:postgresql:$postgres_version")

    // Encrypt/Decrypt password
    implementation("org.mindrot:jbcrypt:0.4")

    // Koin for Ktor; make sure you go to File...Project Structure
    // and switch to Java 11
    implementation("io.insert-koin:koin-ktor:$koin_version")
    // SLF4J Logger
    implementation("io.insert-koin:koin-logger-slf4j:$koin_version")

    testImplementation("io.insert-koin:koin-test:3.3.3")
    testImplementation("io.ktor:ktor-server-test-host:2.2.4")
    testImplementation("org.assertj:assertj-core:3.24.2")
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.2")
    testImplementation("io.mockk:mockk:1.13.4")
    testImplementation("org.xerial:sqlite-jdbc:3.41.2.2")

    testImplementation("io.ktor:ktor-server-tests-jvm")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlin_version")

    implementation("com.github.slugify:slugify:3.0.6")
}

tasks {
    test {
        useJUnitPlatform()
        testLogging {
            events("passed", "skipped", "failed")
        }
    }

    // config JVM target to 1.8 for kotlin compilation tasks
    compileKotlin {
        kotlinOptions.jvmTarget = "17"
    }
}
