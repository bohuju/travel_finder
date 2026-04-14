package com.travelfinder.domain.model

/**
 * 地理位置值对象
 * 封装经纬度信息
 */
data class Location(
    val latitude: Double,
    val longitude: Double,
    val address: String = ""
) {
    companion object {
        val UNKNOWN = Location(0.0, 0.0, "")
    }
}
