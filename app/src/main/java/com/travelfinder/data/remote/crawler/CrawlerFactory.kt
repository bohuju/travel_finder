package com.travelfinder.data.remote.crawler

import javax.inject.Inject
import javax.inject.Singleton

/**
 * 爬虫工厂
 * 根据数据源类型创建对应的爬虫实例
 */
@Singleton
class CrawlerFactory @Inject constructor(
    private val crawlers: Map<String, @JvmSuppressWildcards CrawlerStrategy>
) {

    fun getCrawler(sourceId: String): CrawlerStrategy? {
        return crawlers[sourceId]
    }

    fun getAllCrawlers(): Map<String, CrawlerStrategy> = crawlers

    fun getSupportedSources(): List<Pair<String, String>> {
        return crawlers.map { it.key to it.value.getSourceName() }
    }
}

/**
 * 支持的数据源枚举
 */
enum class DataSource(val id: String, val displayName: String) {
    XIAOHONGSHU("xiaohongshu", "小红书"),
    AMAP("amap", "高德地图")
}
