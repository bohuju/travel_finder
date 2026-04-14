package com.travelfinder.domain.usecase

import com.travelfinder.domain.model.Location
import com.travelfinder.domain.model.POI
import com.travelfinder.domain.model.Post
import com.travelfinder.domain.model.Review
import com.travelfinder.domain.model.TripPlan
import com.travelfinder.domain.repository.PostRepository
import com.travelfinder.domain.repository.TripRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class UseCaseTest {

    @Test
    fun createTripPlanUseCase_createsTripWithGeneratedIdAndCenter() = runTest {
        val tripRepository = FakeTripRepository()
        val useCase = CreateTripPlanUseCase(tripRepository)

        val pois = listOf(
            poi(id = "p1", lat = 30.0, lng = 120.0, rating = 4.0f),
            poi(id = "p2", lat = 32.0, lng = 122.0, rating = 5.0f)
        )

        val result = useCase(
            name = "测试行程",
            pois = pois,
            startDate = 1000L,
            endDate = 2000L
        )

        assertTrue(result.isSuccess)
        val created = result.getOrThrow()
        assertTrue(created.id.isNotBlank())
        assertEquals("测试行程", created.name)
        assertEquals(31.0, created.location.latitude, 0.0001)
        assertEquals(121.0, created.location.longitude, 0.0001)
        assertEquals(2, created.pois.size)
        assertEquals(1000L, created.startDate)
        assertEquals(2000L, created.endDate)
    }

    @Test
    fun searchPOIsUseCase_mapsPostToPoi() = runTest {
        val postRepository = FakePostRepository()
        val useCase = SearchPOIsUseCase(postRepository)

        val post = Post(
            id = "post-1",
            name = "外滩",
            location = Location(31.23, 121.47, "上海"),
            rating = 4.5f,
            author = "alice",
            content = "夜景很美",
            images = listOf("img"),
            tags = listOf("夜景"),
            likes = 10,
            publishDate = 123L
        )
        postRepository.searchResult = Result.success(listOf(post))

        val result = useCase("外滩")
        assertTrue(result.isSuccess)
        val poi = result.getOrThrow().first()
        assertEquals(post.id, poi.id)
        assertEquals(post.name, poi.name)
        assertEquals(post.content, poi.description)
        assertEquals("小红书", poi.source)
    }

    private fun poi(id: String, lat: Double, lng: Double, rating: Float): POI {
        return POI(
            id = id,
            name = id,
            location = Location(lat, lng, "addr-$id"),
            rating = rating,
            description = "desc",
            images = emptyList(),
            tags = emptyList(),
            reviews = emptyList<Review>()
        )
    }
}

private class FakeTripRepository : TripRepository {
    override suspend fun createTripPlan(tripPlan: TripPlan): Result<TripPlan> = Result.success(tripPlan)
    override fun getAllTripPlans(): Flow<List<TripPlan>> = emptyFlow()
    override suspend fun getTripPlanById(id: String): Result<TripPlan> = Result.failure(Exception("not used"))
    override suspend fun updateTripPlan(tripPlan: TripPlan): Result<TripPlan> = Result.success(tripPlan)
    override suspend fun deleteTripPlan(id: String): Result<Unit> = Result.success(Unit)
}

private class FakePostRepository : PostRepository {
    var searchResult: Result<List<Post>> = Result.success(emptyList())

    override suspend fun searchPosts(keyword: String): Result<List<Post>> = searchResult
    override suspend fun getPostsByLocation(
        latitude: Double,
        longitude: Double,
        radiusMeters: Int
    ): Result<List<Post>> = Result.success(emptyList())

    override suspend fun getPostById(id: String): Result<Post> = Result.failure(Exception("not used"))
    override suspend fun savePosts(posts: List<Post>) = Unit
    override fun getSavedPosts(): Flow<List<Post>> = emptyFlow()
    override fun getSupportedSources(): List<String> = emptyList()
}
