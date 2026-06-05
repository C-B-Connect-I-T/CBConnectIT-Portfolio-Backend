package com.cbconnectit.controllers

import org.jetbrains.exposed.sql.Database
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class BaseControllerTest {

    open fun before() {
        Database.connect(url = "jdbc:sqlite::memory:")
    }
}
