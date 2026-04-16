package com.travelfinder.domain.usecase

import com.travelfinder.domain.repository.TripRepository
import javax.inject.Inject

/**
 * 获取行程详情用例
 */
class GetTripPlanDetailUseCase @Inject constructor(
    private val tripRepository: TripRepository
) {
    suspend operator fun invoke(tripId: String) = tripRepository.getTripPlanById(tripId)
}
