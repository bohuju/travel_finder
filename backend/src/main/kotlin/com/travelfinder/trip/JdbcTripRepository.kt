package com.travelfinder.trip

import com.travelfinder.database.DatabaseFactory
import java.util.UUID

class JdbcTripRepository(
    private val databaseFactory: DatabaseFactory
) : TripRepository {

    override fun getAll(): List<Trip> = databaseFactory.openConnection().use { connection ->
        val tripRows = connection.prepareStatement(
            """
            select id, name, days, note
            from trips
            order by id
            """.trimIndent()
        ).use { statement ->
            statement.executeQuery().use { resultSet ->
                buildList {
                    while (resultSet.next()) {
                        add(
                            TripRow(
                                id = resultSet.getString("id"),
                                name = resultSet.getString("name"),
                                days = resultSet.getInt("days"),
                                note = resultSet.getString("note")
                            )
                        )
                    }
                }
            }
        }

        val poiIdsByTripId = loadTripPoiIds(connection)
        tripRows.map { row ->
            Trip(
                id = row.id,
                name = row.name,
                days = row.days,
                note = row.note,
                poiIds = poiIdsByTripId[row.id].orEmpty()
            )
        }
    }

    override fun create(request: CreateTripRequest): Trip = databaseFactory.openConnection().use { connection ->
        connection.autoCommit = false
        try {
            val id = "trip-${UUID.randomUUID().toString().take(8)}"

            connection.prepareStatement(
                """
                insert into trips (id, name, days, note)
                values (?, ?, ?, ?)
                """.trimIndent()
            ).use { statement ->
                statement.setString(1, id)
                statement.setString(2, request.name)
                statement.setInt(3, request.days)
                statement.setString(4, request.note)
                statement.executeUpdate()
            }

            request.poiIds.forEachIndexed { index, poiId ->
                connection.prepareStatement(
                    """
                    insert into trip_pois (trip_id, poi_id, position)
                    values (?, ?, ?)
                    """.trimIndent()
                ).use { statement ->
                    statement.setString(1, id)
                    statement.setString(2, poiId)
                    statement.setInt(3, index)
                    statement.executeUpdate()
                }
            }

            connection.commit()
            Trip(
                id = id,
                name = request.name,
                days = request.days,
                note = request.note,
                poiIds = request.poiIds
            )
        } catch (exception: Exception) {
            connection.rollback()
            throw exception
        }
    }

    private fun loadTripPoiIds(connection: java.sql.Connection): Map<String, List<String>> =
        connection.prepareStatement(
            """
            select trip_id, poi_id
            from trip_pois
            order by trip_id, position
            """.trimIndent()
        ).use { statement ->
            statement.executeQuery().use { resultSet ->
                buildMap {
                    while (resultSet.next()) {
                        val tripId = resultSet.getString("trip_id")
                        val poiId = resultSet.getString("poi_id")
                        put(tripId, getOrDefault(tripId, emptyList()) + poiId)
                    }
                }
            }
        }
}

private data class TripRow(
    val id: String,
    val name: String,
    val days: Int,
    val note: String?
)
