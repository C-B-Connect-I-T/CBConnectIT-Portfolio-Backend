package com.cbconnectit.controllers

import org.jetbrains.exposed.sql.Database
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.module.Module
import org.koin.dsl.module

abstract class BaseControllerTest {

    init {
        stopKoin()
    }

    open fun before() {
        Database.connect(url = "jdbc:sqlite::memory:")
    }

    fun startInjection(module: Module) {
        startKoin {
            modules(
                module,
                module {}
            )
        }
    }
}
