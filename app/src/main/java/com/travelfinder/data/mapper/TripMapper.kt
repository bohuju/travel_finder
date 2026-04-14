package com.travelfinder.data.mapper

import com.travelfinder.data.local.entity.POIEntity
import com.travelfinder.data.local.entity.TripPlanEntity
import com.travelfinder.domain.model.Location
import com.travelfinder.domain.model.POI
import com.travelfinder.domain.model.TripPlan
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

fun TripPlan.toEntity(): TripPlanEntity {
    val poisJson = pois.map { poi ->
        TripPoiSnapshot(
            id = poi.id,
            order = poi.visitOrder ?: 0,
            duration = poi.stayDuration ?: 0
        )
    }

    return TripPlanEntity(
        id = id,
        name = name,
        centerLat = location.latitude,
        centerLng = location.longitude,
        centerAddress = location.address,
        rating = calculateOverallRating(),
        poisJson = Json.encodeToString(poisJson),
        startDate = startDate,
        endDate = endDate,
        createdAt = createdAt
    )
}

fun TripPlanEntity.toDomain(pois: List<POI>): TripPlan {
    return TripPlan(
        id = id,
        name = name,
        location = Location(centerLat, centerLng, centerAddress),
        rating = rating,
        pois = pois.toMutableList(),
        startDate = startDate,
        endDate = endDate,
        createdAt = createdAt
    )
}

fun POI.toEntity(): POIEntity {
    return POIEntity(
        id = id,
        name = name,
        latitude = location.latitude,
        longitude = location.longitude,
        address = location.address,
        rating = rating,
        description = description,
        images = Json.encodeToString(images),
        tags = Json.encodeToString(tags),
        likes = likes,
        source = source
    )
}

fun POIEntity.toDomain(): POI {
    return POI(
        id = id,
        name = name,
        location = Location(latitude, longitude, address),
        rating = rating,
        description = description,
        images = try {
            Json.decodeFromString<List<String>>(images)
        } catch (_: Exception) {
            emptyList()
        },
        tags = try {
            Json.decodeFromString<List<String>>(tags)
        } catch (_: Exception) {
            emptyList()
        },
        reviews = emptyList(),
        likes = likes,
        source = source
    )
}

@Serializable
private data class TripPoiSnapshot(
    val id: String,
    val order: Int,
    val duration: Int
)
