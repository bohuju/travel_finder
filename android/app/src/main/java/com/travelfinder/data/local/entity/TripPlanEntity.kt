package com.travelfinder.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 行程数据库实体
 */
@Entity(tableName = "trip_plans")
data class TripPlanEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val centerLat: Double,
    val centerLng: Double,
    val centerAddress: String,
    val rating: Float,
    val poisJson: String,       // JSON array of POI IDs with order
    val startDate: Long?,
    val endDate: Long?,
    val createdAt: Long
)

/**
 * 行程中的 POI 关联实体
 */
@Entity(
    tableName = "trip_pois",
    primaryKeys = ["tripId", "poiId"]
)
data class TripPOIEntity(
    val tripId: String,
    val poiId: String,
    val visitOrder: Int,
    val stayDuration: Int?
)

/**
 * POI 实体（独立存储）
 */
@Entity(tableName = "pois")
data class POIEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val address: String,
    val rating: Float,
    val description: String,
    val images: String,         // JSON
    val tags: String,           // JSON
    val likes: Int,
    val source: String
)
