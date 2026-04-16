package com.travelfinder.database

import java.sql.Connection
import java.sql.DriverManager

class DatabaseFactory(
    private val settings: DatabaseSettings
) {
    init {
        when {
            settings.jdbcUrl.startsWith("jdbc:postgresql:") -> Class.forName("org.postgresql.Driver")
            settings.jdbcUrl.startsWith("jdbc:h2:") -> Class.forName("org.h2.Driver")
        }
    }

    fun openConnection(): Connection = DriverManager.getConnection(
        settings.jdbcUrl,
        settings.username,
        settings.password
    )
}
