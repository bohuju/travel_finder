package com.travelfinder.util

import com.travelfinder.domain.model.Location
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

object LocationFormatter {

    private const val EARTH_RADIUS_METERS = 6371000.0

    fun distanceMeters(from: Location?, to: Location): Double? {
        if (from == null || !from.isValid() || !to.isValid()) return null

        val latDistance = Math.toRadians(to.latitude - from.latitude)
        val lngDistance = Math.toRadians(to.longitude - from.longitude)
        val startLat = Math.toRadians(from.latitude)
        val endLat = Math.toRadians(to.latitude)

        val haversine = sin(latDistance / 2).pow(2) +
            cos(startLat) * cos(endLat) * sin(lngDistance / 2).pow(2)

        return 2 * EARTH_RADIUS_METERS * asin(sqrt(haversine))
    }

    fun formatDistance(distanceMeters: Double?): String? {
        if (distanceMeters == null) return null
        if (distanceMeters < 1000) {
            return "${distanceMeters.roundToInt()} m"
        }

        val kilometers = distanceMeters / 1000
        return String.format("%.1f km", kilometers)
    }

    fun formatLocationLabel(location: Location?): String {
        if (location == null || !location.isValid()) return "未定位"
        return if (location.address.isNotBlank()) location.address else "${location.latitude}, ${location.longitude}"
    }

    private fun Location.isValid(): Boolean {
        return latitude in -90.0..90.0 &&
            longitude in -180.0..180.0 &&
            !(latitude == 0.0 && longitude == 0.0)
    }
}
