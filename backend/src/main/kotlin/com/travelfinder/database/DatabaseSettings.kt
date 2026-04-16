package com.travelfinder.database

data class DatabaseSettings(
    val jdbcUrl: String,
    val username: String,
    val password: String
) {
    companion object {
        fun fromEnvironment(): DatabaseSettings {
            val jdbcUrl = System.getenv("DATABASE_URL")
                ?: "jdbc:h2:file:./backend/data/travel-finder;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH;AUTO_SERVER=TRUE"
            val username = System.getenv("DATABASE_USER") ?: "sa"
            val password = System.getenv("DATABASE_PASSWORD") ?: ""
            return DatabaseSettings(
                jdbcUrl = jdbcUrl,
                username = username,
                password = password
            )
        }
    }
}
