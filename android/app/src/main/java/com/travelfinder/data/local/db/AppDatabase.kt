package com.travelfinder.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.travelfinder.data.local.dao.PostDao
import com.travelfinder.data.local.dao.TripDao
import com.travelfinder.data.local.entity.PostEntity
import com.travelfinder.data.local.entity.TripPlanEntity
import com.travelfinder.data.local.entity.TripPOIEntity
import com.travelfinder.data.local.entity.POIEntity

/**
 * 应用数据库
 */
@Database(
    entities = [
        PostEntity::class,
        TripPlanEntity::class,
        TripPOIEntity::class,
        POIEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun postDao(): PostDao
    abstract fun tripDao(): TripDao

    companion object {
        const val DATABASE_NAME = "travel_finder_db"
    }
}
