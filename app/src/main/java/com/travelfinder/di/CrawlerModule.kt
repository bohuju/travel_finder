package com.travelfinder.di

import com.travelfinder.data.remote.crawler.AmapPoiCrawler
import com.travelfinder.data.remote.crawler.CrawlerStrategy
import com.travelfinder.data.remote.crawler.XiaohongshuCrawler
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import dagger.multibindings.StringKey
import javax.inject.Singleton

/**
 * 爬虫模块
 * 将 CrawlerStrategy 实现绑定到具体类
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class CrawlerModule {

    @Binds
    @IntoMap
    @StringKey(XiaohongshuCrawler.SOURCE_ID)
    @Singleton
    abstract fun bindXiaohongshuCrawler(
        xiaohongshuCrawler: XiaohongshuCrawler
    ): CrawlerStrategy

    @Binds
    @IntoMap
    @StringKey(AmapPoiCrawler.SOURCE_ID)
    @Singleton
    abstract fun bindAmapPoiCrawler(
        amapPoiCrawler: AmapPoiCrawler
    ): CrawlerStrategy
}
