package com.travelfinder.data.repository

import com.travelfinder.data.local.dao.TripDao
import com.travelfinder.data.local.entity.TripPOIEntity
import com.travelfinder.data.mapper.toDomain
import com.travelfinder.data.mapper.toEntity
import com.travelfinder.domain.model.POI
import com.travelfinder.domain.model.TripPlan
import com.travelfinder.domain.repository.TripRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 行程仓储实现
 */
@Singleton
class TripRepositoryImpl @Inject constructor(
    private val tripDao: TripDao
) : TripRepository {

    override suspend fun createTripPlan(tripPlan: TripPlan): Result<TripPlan> = withContext(Dispatchers.IO) {
        try {
            // 保存 POI
            val poiEntities = tripPlan.pois.map { it.toEntity() }
            tripDao.insertPOIs(poiEntities)

            // 保存行程
            val tripEntity = tripPlan.toEntity()
            tripDao.insertTripPlan(tripEntity)

            // 保存关联
            val tripPOIEntities = tripPlan.pois.mapNotNull { poi ->
                poi.visitOrder?.let { order ->
                    TripPOIEntity(
                        tripId = tripPlan.id,
                        poiId = poi.id,
                        visitOrder = order,
                        stayDuration = poi.stayDuration
                    )
                }
            }
            tripDao.insertTripPOIs(tripPOIEntities)

            Result.success(tripPlan)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getAllTripPlans(): Flow<List<TripPlan>> {
        return tripDao.getAllTripPlans().map { entities ->
            entities.map { entity ->
                val pois = loadPOIsForTrip(entity.id)
                entity.toDomain(pois)
            }
        }
    }

    override suspend fun getTripPlanById(id: String): Result<TripPlan> = withContext(Dispatchers.IO) {
        try {
            val entity = tripDao.getTripPlanById(id)
            if (entity != null) {
                val pois = loadPOIsForTrip(id)
                Result.success(entity.toDomain(pois))
            } else {
                Result.failure(Exception("行程不存在"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateTripPlan(tripPlan: TripPlan): Result<TripPlan> = withContext(Dispatchers.IO) {
        try {
            // 删除旧的关联
            tripDao.deleteTripPOIs(tripPlan.id)

            // 保存新的 POI
            val poiEntities = tripPlan.pois.map { it.toEntity() }
            tripDao.insertPOIs(poiEntities)

            // 保存行程
            tripDao.insertTripPlan(tripPlan.toEntity())

            // 保存新的关联
            val tripPOIEntities = tripPlan.pois.mapNotNull { poi ->
                poi.visitOrder?.let { order ->
                    TripPOIEntity(
                        tripId = tripPlan.id,
                        poiId = poi.id,
                        visitOrder = order,
                        stayDuration = poi.stayDuration
                    )
                }
            }
            tripDao.insertTripPOIs(tripPOIEntities)

            Result.success(tripPlan)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteTripPlan(id: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            tripDao.deleteTripPOIs(id)
            tripDao.deleteTripPlan(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun loadPOIsForTrip(tripId: String): List<POI> {
        val tripPOIs = tripDao.getTripPOIs(tripId)
        if (tripPOIs.isEmpty()) return emptyList()

        val poiIds = tripPOIs.map { it.poiId }
        val poiEntities = tripDao.getPOIsByIds(poiIds)
        val poiMap = poiEntities.associateBy { it.id }

        return tripPOIs.mapNotNull { tripPOI ->
            poiMap[tripPOI.poiId]?.toDomain()?.copy(
                visitOrder = tripPOI.visitOrder,
                stayDuration = tripPOI.stayDuration
            )
        }.sortedBy { it.visitOrder }
    }
}
