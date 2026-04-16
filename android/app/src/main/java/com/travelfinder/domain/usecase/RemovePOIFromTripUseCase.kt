package com.travelfinder.domain.usecase

import com.travelfinder.domain.model.TripPlan
import com.travelfinder.domain.repository.TripRepository
import javax.inject.Inject

/**
 * 从行程移除 POI 用例
 */
class RemovePOIFromTripUseCase @Inject constructor(
    private val tripRepository: TripRepository
) {
    suspend operator fun invoke(tripId: String, poiId: String): Result<TripPlan> {
        return tripRepository.getTripPlanById(tripId).map { trip ->
            val updatedTrip = trip.removePOI(poiId)
            tripRepository.updateTripPlan(updatedTrip).getOrThrow()
        }
    }
}
