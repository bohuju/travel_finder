package com.travelfinder.application

import com.travelfinder.common.ErrorResponse
import com.travelfinder.trip.CreateTripRequest
import com.travelfinder.trip.TripResponse
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.routing.routing

fun Application.configureRouting(dependencies: AppDependencies) {
    routing {
        get("/health") {
            call.respond(mapOf("status" to "ok"))
        }

        route("/api") {
            get("/pois") {
                call.respond(dependencies.poiRepository.getAll())
            }

            get("/pois/{id}") {
                val id = call.parameters["id"]
                    ?: throw IllegalArgumentException("POI id is required")
                val poi = dependencies.poiRepository.findById(id)
                if (poi == null) {
                    call.respond(HttpStatusCode.NotFound, ErrorResponse("POI not found: $id"))
                } else {
                    call.respond(poi)
                }
            }

            get("/trips") {
                val trips = dependencies.tripRepository.getAll().map { trip ->
                    TripResponse(
                        id = trip.id,
                        name = trip.name,
                        days = trip.days,
                        note = trip.note,
                        pois = trip.poiIds.mapNotNull(dependencies.poiRepository::findById)
                    )
                }
                call.respond(trips)
            }

            post("/trips") {
                val request = call.receive<CreateTripRequest>().also(::validateCreateTripRequest)
                val unknownPoiIds = request.poiIds.filter { dependencies.poiRepository.findById(it) == null }
                if (unknownPoiIds.isNotEmpty()) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse("Unknown poi ids: ${unknownPoiIds.joinToString(", ")}")
                    )
                    return@post
                }

                val trip = dependencies.tripRepository.create(request)
                val response = TripResponse(
                    id = trip.id,
                    name = trip.name,
                    days = trip.days,
                    note = trip.note,
                    pois = trip.poiIds.mapNotNull(dependencies.poiRepository::findById)
                )
                call.respond(HttpStatusCode.Created, response)
            }
        }
    }
}

private fun validateCreateTripRequest(request: CreateTripRequest) {
    require(request.name.isNotBlank()) { "Trip name must not be blank" }
    require(request.days > 0) { "Trip days must be greater than 0" }
}
