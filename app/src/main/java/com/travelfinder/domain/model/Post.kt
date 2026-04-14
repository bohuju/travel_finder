package com.travelfinder.domain.model

/**
 * 小红书帖子
 * 从爬虫获取的原始数据
 */
data class Post(
    override val id: String,
    override val name: String,
    override val location: Location,
    override val rating: Float,
    val author: String,
    val content: String,
    val images: List<String>,
    val tags: List<String>,
    val likes: Int,
    val publishDate: Long
) : Locatable(id, name, location, rating) {

    /**
     * 将 Post 转换为 POI
     */
    fun toPOI(): POI = POI(
        id = id,
        name = name,
        location = location,
        rating = rating,
        description = content,
        images = images,
        tags = tags,
        reviews = emptyList(),
        source = "小红书"
    )
}
