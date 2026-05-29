package com.cbconnectit.domain.models

data class Environment(
    val clientUrl: String,

    val jwtSecret: String,
    val databaseUrl: String,
    val databaseUsername: String,
    val databasePassword: String,
    val adminSeedEmail: String,
    val adminSeedPassword: String
)

fun parseEnvironment(): Environment = Environment(
    clientUrl = System.getenv("CLIENT_URL"),
    jwtSecret = System.getenv("JWT_SECRET"),
    databaseUrl = System.getenv("DATABASE_URL"),
    databaseUsername = System.getenv("DATABASE_USERNAME"),
    databasePassword = System.getenv("DATABASE_PASSWORD"),
    adminSeedEmail = System.getenv("ADMIN_SEED_EMAIL"),
    adminSeedPassword = System.getenv("ADMIN_SEED_PASSWORD")
)
