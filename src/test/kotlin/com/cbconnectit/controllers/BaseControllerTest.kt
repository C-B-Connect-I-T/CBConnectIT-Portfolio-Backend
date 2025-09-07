package com.cbconnectit.controllers

import org.jetbrains.exposed.sql.Database

abstract class BaseControllerTest {

    open fun before() {
        Database.connect(url = "jdbc:sqlite::memory:")
    }
}
