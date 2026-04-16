package com.travelfinder.domain.model

/**
 * 行程规划
 * 包含多个 POI 的访问计划
 */
data class TripPlan(
    override val id: String,
    override val name: String,
    override val location: Location,
    override val rating: Float = 0f,
    val pois: MutableList<POI> = mutableListOf(),
    val startDate: Long? = null,
    val endDate: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
) : Locatable(id, name, location, rating) {

    /**
     * 计算行程总评分（所有 POI 的平均分）
     */
    fun calculateOverallRating(): Float {
        if (pois.isEmpty()) return 0f
        return pois.map { it.rating }.average().toFloat()
    }

    /**
     * 计算预计总停留时间（分钟）
     */
    fun totalStayDuration(): Int {
        return pois.mapNotNull { it.stayDuration }.sum()
    }

    /**
     * 添加 POI 到行程
     */
    fun addPOI(poi: POI, order: Int? = null): TripPlan {
        val newPOI = poi.withVisitOrder(order ?: pois.size + 1)
        pois.add(newPOI)
        return this
    }

    /**
     * 从行程中移除 POI
     */
    fun removePOI(poiId: String): TripPlan {
        pois.removeAll { it.id == poiId }
        return this
    }

    /**
     * 重新排序 POI
     */
    fun reorderPOIs(orderedIds: List<String>): TripPlan {
        val reordered = orderedIds.mapIndexedNotNull { index, id ->
            pois.find { it.id == id }?.withVisitOrder(index + 1)
        }
        pois.clear()
        pois.addAll(reordered)
        return this
    }
}

/**
 * 行程计划建造者
 */
class TripPlanBuilder {
    private var id: String = ""
    private var name: String = ""
    private var location: Location = Location.UNKNOWN
    private val pois: MutableList<POI> = mutableListOf()
    private var startDate: Long? = null
    private var endDate: Long? = null

    fun id(id: String) = apply { this.id = id }
    fun name(name: String) = apply { this.name = name }
    fun location(location: Location) = apply { this.location = location }
    fun addPOI(poi: POI) = apply { pois.add(poi) }
    fun startDate(date: Long) = apply { this.startDate = date }
    fun endDate(date: Long) = apply { this.endDate = date }

    fun build(): TripPlan {
        require(id.isNotBlank()) { "TripPlan id cannot be blank" }
        require(name.isNotBlank()) { "TripPlan name cannot be blank" }

        val centerLocation = if (pois.isNotEmpty()) {
            val avgLat = pois.map { it.location.latitude }.average()
            val avgLng = pois.map { it.location.longitude }.average()
            Location(avgLat, avgLng)
        } else {
            location
        }

        return TripPlan(
            id = id,
            name = name,
            location = centerLocation,
            pois = pois,
            startDate = startDate,
            endDate = endDate
        )
    }
}
