package com.travelfinder.application

import com.travelfinder.database.DatabaseFactory
import com.travelfinder.database.DatabaseSettings
import com.travelfinder.database.MigrationRunner
import com.travelfinder.poi.JdbcPoiRepository
import com.travelfinder.poi.PoiRepository
import com.travelfinder.trip.JdbcTripRepository
import com.travelfinder.trip.TripRepository
import java.util.UUID

data class AppDependencies(
    val poiRepository: PoiRepository,
    val tripRepository: TripRepository
) {
    companion object {
        fun default(): AppDependencies = fromDatabase(DatabaseSettings.fromEnvironment())

        fun forTests(): AppDependencies = fromDatabase(
            DatabaseSettings(
                jdbcUrl = "jdbc:h2:mem:travel-finder-${UUID.randomUUID()};MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH;DB_CLOSE_DELAY=-1",
                username = "sa",
                password = ""
            )
        )

        private fun fromDatabase(settings: DatabaseSettings): AppDependencies {
            val databaseFactory = DatabaseFactory(settings)
            MigrationRunner(databaseFactory).migrate()
            return AppDependencies(
                poiRepository = JdbcPoiRepository(databaseFactory),
                tripRepository = JdbcTripRepository(databaseFactory)
            )
        }
    }
}
