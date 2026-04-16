package com.travelfinder.database

import java.time.Instant

class MigrationRunner(
    private val databaseFactory: DatabaseFactory
) {
    fun migrate() {
        databaseFactory.openConnection().use { connection ->
            connection.autoCommit = false
            ensureMigrationTable(connection)

            val appliedVersions = connection.prepareStatement(
                "select version from schema_migrations"
            ).use { statement ->
                statement.executeQuery().use { resultSet ->
                    buildSet {
                        while (resultSet.next()) {
                            add(resultSet.getString("version"))
                        }
                    }
                }
            }

            val migrations = listOf(
                SqlMigration(
                    version = "V1",
                    description = "init_schema",
                    resourcePath = "/db/migration/V1__init_schema.sql"
                )
            )

            migrations
                .filterNot { it.version in appliedVersions }
                .forEach { migration ->
                    runMigration(connection, migration)
                }

            connection.commit()
        }
    }

    private fun ensureMigrationTable(connection: java.sql.Connection) {
        connection.createStatement().use { statement ->
            statement.execute(
                """
                create table if not exists schema_migrations (
                    version varchar(32) primary key,
                    description varchar(255) not null,
                    applied_at varchar(64) not null
                )
                """.trimIndent()
            )
        }
    }

    private fun runMigration(connection: java.sql.Connection, migration: SqlMigration) {
        val sql = requireNotNull(javaClass.getResource(migration.resourcePath)) {
            "Migration file not found: ${migration.resourcePath}"
        }.readText()

        sql.split(";")
            .map(String::trim)
            .filter(String::isNotEmpty)
            .forEach { statementSql ->
                connection.createStatement().use { statement ->
                    statement.execute(statementSql)
                }
            }

        connection.prepareStatement(
            """
            insert into schema_migrations (version, description, applied_at)
            values (?, ?, ?)
            """.trimIndent()
        ).use { statement ->
            statement.setString(1, migration.version)
            statement.setString(2, migration.description)
            statement.setString(3, Instant.now().toString())
            statement.executeUpdate()
        }
    }
}

private data class SqlMigration(
    val version: String,
    val description: String,
    val resourcePath: String
)
