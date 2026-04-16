package com.travelfinder.trip

import com.travelfinder.poi.Poi
import kotlinx.serialization.Serializable

@Serializable
data class Trip(
    val id: String,
    val name: String,
    val days: Int,
    val note: String? = null,
    val poiIds: List<String> = emptyList()
)

@Serializable
data class TripResponse(
    val id: String,
    val name: String,
    val days: Int,
    val note: String? = null,
    val pois: List<Poi> = emptyList()
)

@Serializable
data class CreateTripRequest(
    val name: String,
    val days: Int,
    val note: String? = null,
    val poiIds: List<String> = emptyList()
)
