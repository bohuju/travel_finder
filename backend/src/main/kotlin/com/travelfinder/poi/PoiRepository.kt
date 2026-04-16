package com.travelfinder.poi

interface PoiRepository {
    fun getAll(): List<Poi>

    fun findById(id: String): Poi?
}
