package com.travelfinder.poi

import kotlinx.serialization.Serializable

@Serializable
data class Poi(
    val id: String,
    val name: String,
    val city: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val tags: List<String> = emptyList(),
    val source: String
)
