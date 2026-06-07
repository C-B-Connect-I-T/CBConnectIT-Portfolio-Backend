package com.cbconnectit.dao

import kotlinx.coroutines.runBlocking
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import java.sql.Connection

/**
 * Base class for DAO tests that sets up a file-based SQLite database using Flyway migrations
 * and provides rollback + seeding support.
 *
 * Uses a file-based SQLite database (in the build directory) so that both Flyway and Exposed
 * can independently connect and share the same schema. The file is cleaned up after each
 * test class via Flyway clean in @BeforeAll, guaranteeing a fresh schema.
 */
abstract class BaseDaoTest {

    companion object Companion {
        private lateinit var db: Database
        private const val DB_FILE = "build/test-portfolio.db"
        private const val DB_URL = "jdbc:sqlite:$DB_FILE?foreign_keys=on"

        @JvmStatic
        @BeforeAll
        fun setupDatabase() {
            // Run Flyway clean + migrate to guarantee a fresh schema for each test class
            Flyway.configure()
                .dataSource(DB_URL, null, null)
                .locations("classpath:db/test-migration")
                .cleanDisabled(false)
                .load()
                .also { it.clean() }
                .also { it.migrate() }

            // Connect Exposed to the same file-based database
            db = Database.connect(DB_URL, driver = "org.sqlite.JDBC")
            TransactionManager.manager.defaultIsolationLevel = Connection.TRANSACTION_REPEATABLE_READ
        }

        @JvmStatic
        @AfterAll
        fun tearDownDatabase() {
            // File-based DB in build/ directory is cleaned by ./gradlew clean
        }
    }

    /**
     * Override this if you want to insert default data before each test.
     * Runs inside a transaction before the test block.
     */
    open suspend fun seedData() {}

    /**
     * Runs a suspending test inside a rolled-back transaction.
     * Automatically runs `seedData()` before the test block.
     */
    protected fun runTest(shouldSeedData: Boolean = true, block: suspend () -> Unit) {
        runBlocking {
            newSuspendedTransaction(db = db) {
                try {
                    if (shouldSeedData) {
                        seedData()
                    }
                    block()
                } finally {
                    rollback()
                }
            }
        }
    }
}
