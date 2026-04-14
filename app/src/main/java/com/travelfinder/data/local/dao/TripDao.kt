package com.travelfinder.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.travelfinder.data.local.entity.TripPlanEntity
import com.travelfinder.data.local.entity.TripPOIEntity
import com.travelfinder.data.local.entity.POIEntity
import kotlinx.coroutines.flow.Flow

/**
 * 行程数据访问对象
 */
@Dao
interface TripDao {

    @Query("SELECT * FROM trip_plans ORDER BY createdAt DESC")
    fun getAllTripPlans(): Flow<List<TripPlanEntity>>

    @Query("SELECT * FROM trip_plans WHERE id = :id")
    suspend fun getTripPlanById(id: String): TripPlanEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTripPlan(tripPlan: TripPlanEntity)

    @Query("DELETE FROM trip_plans WHERE id = :id")
    suspend fun deleteTripPlan(id: String)

    // Trip POI relations
    @Query("SELECT * FROM trip_pois WHERE tripId = :tripId ORDER BY visitOrder")
    suspend fun getTripPOIs(tripId: String): List<TripPOIEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTripPOIs(tripPOIs: List<TripPOIEntity>)

    @Query("DELETE FROM trip_pois WHERE tripId = :tripId")
    suspend fun deleteTripPOIs(tripId: String)

    // POI storage
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPOI(poi: POIEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPOIs(pois: List<POIEntity>)

    @Query("SELECT * FROM pois WHERE id = :id")
    suspend fun getPOIById(id: String): POIEntity?

    @Query("SELECT * FROM pois WHERE id IN (:ids)")
    suspend fun getPOIsByIds(ids: List<String>): List<POIEntity>
}
