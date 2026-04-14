package com.travelfinder.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.travelfinder.data.local.entity.PostEntity
import kotlinx.coroutines.flow.Flow

/**
 * 帖子数据访问对象
 */
@Dao
interface PostDao {

    @Query("SELECT * FROM posts ORDER BY cachedAt DESC")
    fun getAllPosts(): Flow<List<PostEntity>>

    @Query("SELECT * FROM posts WHERE id = :id")
    suspend fun getPostById(id: String): PostEntity?

    @Query("SELECT * FROM posts WHERE name LIKE '%' || :keyword || '%' OR content LIKE '%' || :keyword || '%'")
    suspend fun searchPosts(keyword: String): List<PostEntity>

    @Query("SELECT * FROM posts WHERE latitude BETWEEN :minLat AND :maxLat AND longitude BETWEEN :minLng AND :maxLng")
    suspend fun getPostsByLocation(
        minLat: Double,
        maxLat: Double,
        minLng: Double,
        maxLng: Double
    ): List<PostEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPosts(posts: List<PostEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPost(post: PostEntity)

    @Query("DELETE FROM posts WHERE id = :id")
    suspend fun deletePost(id: String)

    @Query("DELETE FROM posts")
    suspend fun deleteAllPosts()
}
