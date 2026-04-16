package com.travelfinder.trip

interface TripRepository {
    fun getAll(): List<Trip>

    fun create(request: CreateTripRequest): Trip
}
