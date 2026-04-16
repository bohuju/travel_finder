package com.travelfinder.poi

import com.travelfinder.database.DatabaseFactory

class JdbcPoiRepository(
    private val databaseFactory: DatabaseFactory
) : PoiRepository {

    override fun getAll(): List<Poi> = databaseFactory.openConnection().use { connection ->
        val poiRows = connection.prepareStatement(
            """
            select id, name, city, address, latitude, longitude, source
            from pois
            order by id
            """.trimIndent()
        ).use { statement ->
            statement.executeQuery().use { resultSet ->
                buildList {
                    while (resultSet.next()) {
                        add(
                            PoiRow(
                                id = resultSet.getString("id"),
                                name = resultSet.getString("name"),
                                city = resultSet.getString("city"),
                                address = resultSet.getString("address"),
                                latitude = resultSet.getDouble("latitude"),
                                longitude = resultSet.getDouble("longitude"),
                                source = resultSet.getString("source")
                            )
                        )
                    }
                }
            }
        }

        val tagsByPoiId = loadTags(connection)
        poiRows.map { row -> row.toPoi(tagsByPoiId[row.id].orEmpty()) }
    }

    override fun findById(id: String): Poi? = databaseFactory.openConnection().use { connection ->
        val poiRow = connection.prepareStatement(
            """
            select id, name, city, address, latitude, longitude, source
            from pois
            where id = ?
            """.trimIndent()
        ).use { statement ->
            statement.setString(1, id)
            statement.executeQuery().use { resultSet ->
                if (!resultSet.next()) {
                    null
                } else {
                    PoiRow(
                        id = resultSet.getString("id"),
                        name = resultSet.getString("name"),
                        city = resultSet.getString("city"),
                        address = resultSet.getString("address"),
                        latitude = resultSet.getDouble("latitude"),
                        longitude = resultSet.getDouble("longitude"),
                        source = resultSet.getString("source")
                    )
                }
            }
        } ?: return@use null

        val tags = connection.prepareStatement(
            """
            select tag
            from poi_tags
            where poi_id = ?
            order by position
            """.trimIndent()
        ).use { statement ->
            statement.setString(1, id)
            statement.executeQuery().use { resultSet ->
                buildList {
                    while (resultSet.next()) {
                        add(resultSet.getString("tag"))
                    }
                }
            }
        }

        poiRow.toPoi(tags)
    }

    private fun loadTags(connection: java.sql.Connection): Map<String, List<String>> =
        connection.prepareStatement(
            """
            select poi_id, tag
            from poi_tags
            order by poi_id, position
            """.trimIndent()
        ).use { statement ->
            statement.executeQuery().use { resultSet ->
                buildMap {
                    while (resultSet.next()) {
                        val poiId = resultSet.getString("poi_id")
                        val tag = resultSet.getString("tag")
                        put(poiId, getOrDefault(poiId, emptyList()) + tag)
                    }
                }
            }
        }
}

private data class PoiRow(
    val id: String,
    val name: String,
    val city: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val source: String
) {
    fun toPoi(tags: List<String>): Poi = Poi(
        id = id,
        name = name,
        city = city,
        address = address,
        latitude = latitude,
        longitude = longitude,
        tags = tags,
        source = source
    )
}
