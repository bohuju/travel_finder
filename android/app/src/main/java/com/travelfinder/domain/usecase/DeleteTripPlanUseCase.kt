package com.travelfinder.domain.usecase

import com.travelfinder.domain.repository.TripRepository
import javax.inject.Inject

/**
 * 删除行程用例
 */
class DeleteTripPlanUseCase @Inject constructor(
    private val tripRepository: TripRepository
) {
    suspend operator fun invoke(tripId: String) = tripRepository.deleteTripPlan(tripId)
}
