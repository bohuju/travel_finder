package com.travelfinder.data.repository

import com.travelfinder.data.local.dao.PostDao
import com.travelfinder.data.local.entity.PostEntity
import com.travelfinder.data.remote.crawler.CrawlerFactory
import com.travelfinder.data.remote.crawler.CrawlerStrategy
import com.travelfinder.data.remote.crawler.DataSource
import com.travelfinder.domain.model.Location
import com.travelfinder.domain.model.Post
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PostRepositoryImplTest {

    private val postDao = mockk<PostDao>()
    private val crawlerFactory = mockk<CrawlerFactory>()
    private val amapCrawler = mockk<CrawlerStrategy>()
    private val xhsCrawler = mockk<CrawlerStrategy>()

    @Test
    fun searchPosts_whenRemoteSuccess_returnsRemoteAndCaches() = runTest {
        val post = post("1")
        every { crawlerFactory.getCrawler(DataSource.AMAP.id) } returns amapCrawler
        every { crawlerFactory.getCrawler(DataSource.XIAOHONGSHU.id) } returns xhsCrawler
        coEvery { amapCrawler.crawl("外滩") } returns Result.success(listOf(post))
        coEvery { postDao.insertPosts(any()) } just runs

        val repository = PostRepositoryImpl(postDao, crawlerFactory)
        val result = repository.searchPosts("外滩")

        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrThrow().size)
        coVerify(exactly = 1) { postDao.insertPosts(any()) }
        coVerify(exactly = 0) { postDao.searchPosts(any()) }
    }

    @Test
    fun searchPosts_whenAllRemoteFailed_returnsLocalCache() = runTest {
        val entity = PostEntity.fromJson(
            id = "2",
            name = "本地景点",
            lat = 31.23,
            lng = 121.47,
            address = "上海",
            rating = 4.2f,
            author = "local",
            content = "缓存内容",
            images = listOf("img"),
            tags = listOf("tag"),
            likes = 2,
            publishDate = 100L
        )
        every { crawlerFactory.getCrawler(DataSource.AMAP.id) } returns amapCrawler
        every { crawlerFactory.getCrawler(DataSource.XIAOHONGSHU.id) } returns xhsCrawler
        coEvery { amapCrawler.crawl("缓存") } returns Result.failure(Exception("net"))
        coEvery { xhsCrawler.crawl("缓存") } returns Result.failure(Exception("net"))
        coEvery { postDao.searchPosts("缓存") } returns listOf(entity)

        val repository = PostRepositoryImpl(postDao, crawlerFactory)
        val result = repository.searchPosts("缓存")

        assertTrue(result.isSuccess)
        val post = result.getOrThrow().first()
        assertEquals("2", post.id)
        assertEquals("本地景点", post.name)
    }

    private fun post(id: String): Post {
        return Post(
            id = id,
            name = "name-$id",
            location = Location(31.0, 121.0, "上海"),
            rating = 4.5f,
            author = "author",
            content = "content",
            images = listOf("img"),
            tags = listOf("tag"),
            likes = 1,
            publishDate = 1L
        )
    }
}
