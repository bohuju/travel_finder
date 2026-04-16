package com.travelfinder.trip

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

class InMemoryTripRepository(
    initialTrips: List<Trip> = sampleTrips()
) : TripRepository {

    private val idSequence = AtomicInteger(initialTrips.size)
    private val trips = ConcurrentHashMap<String, Trip>(
        initialTrips.associateBy(Trip::id)
    )

    override fun getAll(): List<Trip> = trips.values.sortedBy(Trip::id)

    override fun create(request: CreateTripRequest): Trip {
        val id = "trip-${idSequence.incrementAndGet()}"
        val trip = Trip(
            id = id,
            name = request.name,
            days = request.days,
            note = request.note,
            poiIds = request.poiIds
        )
        trips[id] = trip
        return trip
    }

    companion object {
        fun sampleTrips(): List<Trip> = listOf(
            Trip(
                id = "trip-1",
                name = "杭州周末慢游",
                days = 2,
                note = "以西湖周边 citywalk 为主",
                poiIds = listOf("poi-west-lake")
            )
        )
    }
}
