package com.cbconnectit.plugins

import com.cbconnectit.data.database.tables.UsersTable
import com.cbconnectit.domain.models.user.UserRoles
import com.cbconnectit.utils.PasswordManagerContract
import com.cbconnectit.utils.toDatabaseString
import io.ktor.server.application.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.insertIgnore
import org.jetbrains.exposed.sql.transactions.transaction
import org.koin.ktor.ext.inject
import java.time.LocalDateTime

fun Application.configureDatabase() {
    val passwordEncryption by inject<PasswordManagerContract>()

    Database.connect(
        System.getenv("database-url"),
        user = System.getenv("database-username"),
        password = System.getenv("database-password")
    )

    transaction {
        SchemaUtils.createMissingTablesAndColumns(UsersTable)

        seedDatabase(passwordEncryption)
    }
}

private fun seedDatabase(passwordEncryption: PasswordManagerContract) {
    val time = LocalDateTime.now().toDatabaseString()

    UsersTable.insertIgnore {
        it[id] = 1
        it[fullName] = "Bolla"
        it[username] = "bollachristiano@gmail.com"
        it[password] = passwordEncryption.encryptPassword(System.getenv("ADMIN_SEED_PASSWORD"))
        it[createdAt] = time
        it[updatedAt] = time
        it[role] = UserRoles.Admin
    }
}

suspend fun <T> dbQuery(block: () -> T): T = withContext(Dispatchers.IO) {
    transaction { block() }
}