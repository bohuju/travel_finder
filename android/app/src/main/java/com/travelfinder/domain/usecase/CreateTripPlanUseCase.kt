package com.travelfinder.domain.usecase

import com.travelfinder.domain.model.POI
import com.travelfinder.domain.model.TripPlan
import com.travelfinder.domain.model.TripPlanBuilder
import com.travelfinder.domain.repository.TripRepository
import java.util.UUID
import javax.inject.Inject

/**
 * 创建行程用例
 */
class CreateTripPlanUseCase @Inject constructor(
    private val tripRepository: TripRepository
) {
    suspend operator fun invoke(
        name: String,
        pois: List<POI>,
        startDate: Long? = null,
        endDate: Long? = null
    ): Result<TripPlan> {
        if (pois.isEmpty()) {
            return Result.failure(IllegalArgumentException("POI list cannot be empty"))
        }

        val tripPlan = TripPlanBuilder()
            .id(UUID.randomUUID().toString())
            .name(name)
            .apply {
                pois.forEach { addPOI(it) }
                startDate?.let { this.startDate(it) }
                endDate?.let { this.endDate(it) }
            }
            .build()

        return tripRepository.createTripPlan(tripPlan)
    }
}
