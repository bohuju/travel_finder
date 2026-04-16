package com.travelfinder.domain.model

/**
 * 兴趣点（Point of Interest）
 * 用户可以访问的具体景点或地点
 */
data class POI(
    override val id: String,
    override val name: String,
    override val location: Location,
    override val rating: Float,
    val description: String,
    val images: List<String>,
    val tags: List<String>,
    val reviews: List<Review>,
    val visitOrder: Int? = null,
    val stayDuration: Int? = null,
    val likes: Int = 0,
    val source: String = ""
) : Locatable(id, name, location, rating) {

    fun withVisitOrder(order: Int): POI = copy(visitOrder = order)

    fun withStayDuration(minutes: Int): POI = copy(stayDuration = minutes)
}
