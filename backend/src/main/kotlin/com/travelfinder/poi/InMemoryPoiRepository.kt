package com.travelfinder.poi

class InMemoryPoiRepository(
    initialPois: List<Poi> = samplePois()
) : PoiRepository {

    private val poisById = initialPois.associateBy(Poi::id)

    override fun getAll(): List<Poi> = poisById.values.toList()

    override fun findById(id: String): Poi? = poisById[id]

    companion object {
        fun samplePois(): List<Poi> = listOf(
            Poi(
                id = "poi-west-lake",
                name = "西湖断桥",
                city = "杭州",
                address = "浙江省杭州市西湖区北山街",
                latitude = 30.259,
                longitude = 120.148,
                tags = listOf("view", "lake", "classic"),
                source = "seed"
            ),
            Poi(
                id = "poi-the-bund",
                name = "外滩观景步道",
                city = "上海",
                address = "上海市黄浦区中山东一路",
                latitude = 31.240,
                longitude = 121.490,
                tags = listOf("citywalk", "night"),
                source = "seed"
            ),
            Poi(
                id = "poi-orange-island",
                name = "橘子洲头",
                city = "长沙",
                address = "湖南省长沙市岳麓区橘子洲景区",
                latitude = 28.189,
                longitude = 112.969,
                tags = listOf("park", "landmark"),
                source = "seed"
            )
        )
    }
}
