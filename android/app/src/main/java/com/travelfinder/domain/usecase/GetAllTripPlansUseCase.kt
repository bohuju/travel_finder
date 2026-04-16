package com.travelfinder.domain.usecase

import com.travelfinder.domain.model.TripPlan
import com.travelfinder.domain.repository.TripRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * 获取所有行程用例
 */
class GetAllTripPlansUseCase @Inject constructor(
    private val tripRepository: TripRepository
) {
    operator fun invoke(): Flow<List<TripPlan>> = tripRepository.getAllTripPlans()
}
