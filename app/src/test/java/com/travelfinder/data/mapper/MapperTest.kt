package com.travelfinder.data.mapper

import com.travelfinder.domain.model.Location
import com.travelfinder.domain.model.POI
import com.travelfinder.domain.model.Post
import com.travelfinder.domain.model.Review
import com.travelfinder.domain.model.TripPlan
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MapperTest {

    @Test
    fun postMapper_roundTrip_preservesFields() {
        val post = Post(
            id = "post-1",
            name = "外滩",
            location = Location(31.23, 121.47, "上海"),
            rating = 4.8f,
            author = "tom",
            content = "值得去",
            images = listOf("a.jpg", "b.jpg"),
            tags = listOf("夜景", "地标"),
            likes = 12,
            publishDate = 1000L
        )

        val domain = post.toEntity().toDomain()
        assertEquals(post.id, domain.id)
        assertEquals(post.name, domain.name)
        assertEquals(post.location, domain.location)
        assertEquals(post.images, domain.images)
        assertEquals(post.tags, domain.tags)
        assertEquals(post.likes, domain.likes)
    }

    @Test
    fun tripMapper_toEntityAndBack_keepsKeyData() {
        val poi1 = poi("1", 30.0, 120.0, 4.0f).withVisitOrder(1).withStayDuration(60)
        val poi2 = poi("2", 32.0, 122.0, 5.0f).withVisitOrder(2).withStayDuration(90)
        val trip = TripPlan(
            id = "trip-1",
            name = "华东游",
            location = Location(31.0, 121.0, "中心"),
            pois = mutableListOf(poi1, poi2),
            startDate = 10L,
            endDate = 20L,
            createdAt = 30L
        )

        val entity = trip.toEntity()
        assertEquals(4.5f, entity.rating, 0.0001f)
        assertTrue(entity.poisJson.isNotBlank())

        val restored = entity.toDomain(listOf(poi1, poi2))
        assertEquals(trip.id, restored.id)
        assertEquals(2, restored.pois.size)
        assertEquals(trip.startDate, restored.startDate)
        assertEquals(trip.endDate, restored.endDate)
    }

    private fun poi(id: String, lat: Double, lng: Double, rating: Float): POI {
        return POI(
            id = id,
            name = "poi-$id",
            location = Location(lat, lng, "addr-$id"),
            rating = rating,
            description = "desc-$id",
            images = emptyList(),
            tags = emptyList(),
            reviews = emptyList<Review>()
        )
    }
}
