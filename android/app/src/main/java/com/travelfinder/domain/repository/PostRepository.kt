package com.travelfinder.domain.repository

import com.travelfinder.domain.model.Post
import kotlinx.coroutines.flow.Flow

/**
 * 帖子仓储接口
 * 抽象数据访问，支持不同数据源实现
 */
interface PostRepository {

    /**
     * 搜索帖子
     */
    suspend fun searchPosts(keyword: String): Result<List<Post>>

    /**
     * 按位置搜索帖子
     */
    suspend fun getPostsByLocation(
        latitude: Double,
        longitude: Double,
        radiusMeters: Int
    ): Result<List<Post>>

    /**
     * 获取帖子详情
     */
    suspend fun getPostById(id: String): Result<Post>

    /**
     * 保存帖子到本地缓存
     */
    suspend fun savePosts(posts: List<Post>)

    /**
     * 获取本地缓存的帖子
     */
    fun getSavedPosts(): Flow<List<Post>>

    /**
     * 获取所有支持的平台
     */
    fun getSupportedSources(): List<String>
}
