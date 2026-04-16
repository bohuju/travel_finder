package com.travelfinder.domain.usecase

import com.travelfinder.domain.model.POI
import com.travelfinder.domain.model.TripPlan
import com.travelfinder.domain.repository.TripRepository
import javax.inject.Inject

/**
 * 添加 POI 到行程用例
 */
class AddPOIToTripUseCase @Inject constructor(
    private val tripRepository: TripRepository
) {
    suspend operator fun invoke(tripId: String, poi: POI, order: Int? = null): Result<TripPlan> {
        return tripRepository.getTripPlanById(tripId).map { trip ->
            val updatedTrip = trip.addPOI(poi, order)
            tripRepository.updateTripPlan(updatedTrip).getOrThrow()
        }
    }
}
