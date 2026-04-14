package com.travelfinder.data.remote.crawler

import com.travelfinder.domain.model.Post

/**
 * 爬虫策略接口
 * 定义爬虫的抽象行为，支持不同平台实现
 */
interface CrawlerStrategy {

    /**
     * 按关键词搜索帖子
     */
    suspend fun crawl(keyword: String): Result<List<Post>>

    /**
     * 按位置搜索帖子
     * @param latitude 纬度
     * @param longitude 经度
     * @param radiusMeters 搜索半径（米）
     */
    suspend fun crawlByLocation(
        latitude: Double,
        longitude: Double,
        radiusMeters: Int = 5000
    ): Result<List<Post>>

    /**
     * 获取数据源名称
     */
    fun getSourceName(): String

    /**
     * 获取数据源标识
     */
    fun getSourceId(): String
}
