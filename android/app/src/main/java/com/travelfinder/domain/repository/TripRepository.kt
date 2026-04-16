package com.travelfinder.domain.repository

import com.travelfinder.domain.model.TripPlan
import kotlinx.coroutines.flow.Flow

/**
 * 行程仓储接口
 */
interface TripRepository {

    /**
     * 创建行程
     */
    suspend fun createTripPlan(tripPlan: TripPlan): Result<TripPlan>

    /**
     * 获取所有行程
     */
    fun getAllTripPlans(): Flow<List<TripPlan>>

    /**
     * 获取行程详情
     */
    suspend fun getTripPlanById(id: String): Result<TripPlan>

    /**
     * 更新行程
     */
    suspend fun updateTripPlan(tripPlan: TripPlan): Result<TripPlan>

    /**
     * 删除行程
     */
    suspend fun deleteTripPlan(id: String): Result<Unit>
}
