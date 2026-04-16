package com.travelfinder.data.remote.crawler

import com.travelfinder.domain.model.Location
import com.travelfinder.domain.model.Post
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 高德地图 POI 爬虫实现
 * 用于通过高德地图搜索兴趣点
 */
@Singleton
class AmapPoiCrawler @Inject constructor() : CrawlerStrategy {

    companion object {
        const val SOURCE_ID = "amap"
    }

    override suspend fun crawl(keyword: String): Result<List<Post>> = withContext(Dispatchers.IO) {
        try {
            // 实际实现需要调用高德地图 POI 搜索 API
            // 这里返回模拟数据
            val mockPosts = generateMockPosts(keyword)
            Result.success(mockPosts)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun crawlByLocation(
        latitude: Double,
        longitude: Double,
        radiusMeters: Int
    ): Result<List<Post>> = withContext(Dispatchers.IO) {
        try {
            val mockPosts = generateMockPostsByLocation(latitude, longitude, radiusMeters)
            Result.success(mockPosts)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getSourceName(): String = "高德地图"

    override fun getSourceId(): String = SOURCE_ID

    private fun generateMockPosts(keyword: String): List<Post> {
        val locations = listOf(
            Location(31.2304, 121.4737, "上海市黄浦区"),
            Location(39.9042, 116.4074, "北京市东城区"),
            Location(22.5431, 114.0579, "深圳市南山区"),
            Location(30.5728, 114.2792, "武汉市武昌区"),
            Location(29.5587, 106.5494, "重庆市渝中区")
        )

        return locations.mapIndexed { index, location ->
            Post(
                id = UUID.randomUUID().toString(),
                name = "${keyword}（高德推荐）",
                location = location,
                rating = (3.5f..5.0f).random(),
                author = "高德地图",
                content = "基于高德地图数据的${keyword}推荐，位置精准，信息丰富。",
                images = emptyList(),
                tags = listOf(keyword, "高德推荐"),
                likes = 0,
                publishDate = System.currentTimeMillis()
            )
        }
    }

    private fun generateMockPostsByLocation(
        lat: Double,
        lng: Double,
        radius: Int
    ): List<Post> {
        return (1..5).map { index ->
            Post(
                id = UUID.randomUUID().toString(),
                name = "高德POI-${index}",
                location = Location(
                    latitude = lat + (Math.random() - 0.5) * 0.02,
                    longitude = lng + (Math.random() - 0.5) * 0.02,
                    address = "高德地图定位地址"
                ),
                rating = (3.0f..5.0f).random(),
                author = "高德地图",
                content = "附近的热门地点",
                images = emptyList(),
                tags = listOf("附近", "热门"),
                likes = 0,
                publishDate = System.currentTimeMillis()
            )
        }
    }

    private fun ClosedRange<Float>.random(): Float {
        return start + (endInclusive - start) * Math.random().toFloat()
    }
}
