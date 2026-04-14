package com.travelfinder.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * 帖子数据库实体
 */
@Entity(tableName = "posts")
data class PostEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val address: String,
    val rating: Float,
    val author: String,
    val content: String,
    val images: String,         // JSON string
    val tags: String,           // JSON string
    val likes: Int,
    val publishDate: Long,
    val cachedAt: Long = System.currentTimeMillis()
) {
    fun getImagesList(): List<String> {
        return try {
            Json.decodeFromString<List<String>>(images)
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun getTagsList(): List<String> {
        return try {
            Json.decodeFromString<List<String>>(tags)
        } catch (e: Exception) {
            emptyList()
        }
    }

    companion object {
        fun fromJson(
            id: String,
            name: String,
            lat: Double,
            lng: Double,
            address: String,
            rating: Float,
            author: String,
            content: String,
            images: List<String>,
            tags: List<String>,
            likes: Int,
            publishDate: Long
        ): PostEntity {
            return PostEntity(
                id = id,
                name = name,
                latitude = lat,
                longitude = lng,
                address = address,
                rating = rating,
                author = author,
                content = content,
                images = Json.encodeToString(images),
                tags = Json.encodeToString(tags),
                likes = likes,
                publishDate = publishDate
            )
        }
    }
}
