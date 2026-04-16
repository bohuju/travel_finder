package com.travelfinder.data.repository

import com.travelfinder.data.local.dao.PostDao
import com.travelfinder.data.mapper.toDomain
import com.travelfinder.data.mapper.toEntity
import com.travelfinder.data.remote.crawler.CrawlerFactory
import com.travelfinder.data.remote.crawler.DataSource
import com.travelfinder.domain.model.Post
import com.travelfinder.domain.repository.PostRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 帖子仓储实现
 * 优先从远程爬取，失败时从本地缓存获取
 */
@Singleton
class PostRepositoryImpl @Inject constructor(
    private val postDao: PostDao,
    private val crawlerFactory: CrawlerFactory
) : PostRepository {

    private val xiaohongshuCrawler = crawlerFactory.getCrawler(DataSource.XIAOHONGSHU.id)
    private val amapCrawler = crawlerFactory.getCrawler(DataSource.AMAP.id)

    override suspend fun searchPosts(keyword: String): Result<List<Post>> = withContext(Dispatchers.IO) {
        // 优先从高德地图搜索（API 稳定）
        amapCrawler?.crawl(keyword)?.let { result ->
            if (result.isSuccess) {
                val posts = result.getOrNull().orEmpty()
                savePosts(posts)
                return@withContext Result.success(posts)
            }
        }

        // 如果高德失败，尝试小红书
        xiaohongshuCrawler?.crawl(keyword)?.let { result ->
            if (result.isSuccess) {
                val posts = result.getOrNull().orEmpty()
                savePosts(posts)
                return@withContext Result.success(posts)
            }
        }

        // 都失败，尝试从本地缓存获取
        val cachedPosts = postDao.searchPosts(keyword)
        if (cachedPosts.isNotEmpty()) {
            Result.success(cachedPosts.map { it.toDomain() })
        } else {
            Result.failure(Exception("无法获取帖子数据"))
        }
    }

    override suspend fun getPostsByLocation(
        latitude: Double,
        longitude: Double,
        radiusMeters: Int
    ): Result<List<Post>> = withContext(Dispatchers.IO) {
        // 半径转换为经纬度范围（近似）
        val latDelta = radiusMeters / 111000.0
        val lngDelta = radiusMeters / (111000.0 * Math.cos(Math.toRadians(latitude)))

        val minLat = latitude - latDelta
        val maxLat = latitude + latDelta
        val minLng = longitude - lngDelta
        val maxLng = longitude + lngDelta

        // 优先尝试远程
        amapCrawler?.crawlByLocation(latitude, longitude, radiusMeters)?.let { result ->
            if (result.isSuccess) {
                val posts = result.getOrNull().orEmpty()
                savePosts(posts)
                return@withContext Result.success(posts)
            }
        }

        // 尝试本地缓存
        val cachedPosts = postDao.getPostsByLocation(minLat, maxLat, minLng, maxLng)
        if (cachedPosts.isNotEmpty()) {
            Result.success(cachedPosts.map { it.toDomain() })
        } else {
            Result.failure(Exception("指定范围内未找到帖子"))
        }
    }

    override suspend fun getPostById(id: String): Result<Post> = withContext(Dispatchers.IO) {
        val post = postDao.getPostById(id)
        if (post != null) {
            Result.success(post.toDomain())
        } else {
            Result.failure(Exception("帖子不存在"))
        }
    }

    override suspend fun savePosts(posts: List<Post>) = withContext(Dispatchers.IO) {
        val entities = posts.map { it.toEntity() }
        postDao.insertPosts(entities)
    }

    override fun getSavedPosts(): Flow<List<Post>> {
        return postDao.getAllPosts().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getSupportedSources(): List<String> {
        return crawlerFactory.getSupportedSources().map { it.second }
    }
}
