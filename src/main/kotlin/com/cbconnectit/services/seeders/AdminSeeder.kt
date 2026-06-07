package com.cbconnectit.services.seeders

import com.cbconnectit.data.database.tables.UsersTable
import com.cbconnectit.domain.models.Environment
import com.cbconnectit.domain.models.user.User
import com.cbconnectit.utils.PasswordManagerContract
import org.jetbrains.exposed.sql.insertIgnore
import java.util.*

/**
 * Seeds the admin user required for system functionality.
 * Runs automatically on application startup after Flyway migrations.
 */
class AdminSeeder(
    private val passwordEncryption: PasswordManagerContract,
    private val environment: Environment
) {

    companion object {
        val ADMIN_USER_ID: UUID = UUID.fromString("00000000-0000-0000-0000-000000000001")
    }

    /**
     * Inserts the admin user if it doesn't already exist.
     * Uses `insertIgnore` to safely skip if the user already exists.
     */
    fun seed() {
        UsersTable.insertIgnore {
            it[id] = ADMIN_USER_ID
            it[fullName] = "Admin"
            it[username] = environment.adminSeedEmail
            it[password] = passwordEncryption.encryptPassword(environment.adminSeedPassword)
            it[role] = User.Role.Admin
        }
    }
}
