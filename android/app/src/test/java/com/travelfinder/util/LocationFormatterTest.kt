package com.travelfinder.util

import com.travelfinder.domain.model.Location
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LocationFormatterTest {

    @Test
    fun distanceMeters_returnsNullWhenLocationInvalid() {
        val distance = LocationFormatter.distanceMeters(
            from = null,
            to = Location(31.2304, 121.4737, "Shanghai")
        )

        assertNull(distance)
    }

    @Test
    fun distanceMeters_calculatesReadableDistance() {
        val distance = LocationFormatter.distanceMeters(
            from = Location(31.2304, 121.4737, "人民广场"),
            to = Location(31.2380, 121.4900, "外滩")
        )

        assertNotNull(distance)
        assertTrue(distance!! > 1500)
        assertTrue(distance < 2500)
    }

    @Test
    fun formatDistance_formatsMetersAndKilometers() {
        assertEquals("420 m", LocationFormatter.formatDistance(420.2))
        assertEquals("1.6 km", LocationFormatter.formatDistance(1560.0))
    }

    @Test
    fun formatLocationLabel_prefersAddress() {
        assertEquals(
            "上海市中心",
            LocationFormatter.formatLocationLabel(Location(31.2304, 121.4737, "上海市中心"))
        )
    }
}
