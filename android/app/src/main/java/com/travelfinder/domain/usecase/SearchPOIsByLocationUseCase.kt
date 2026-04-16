package com.travelfinder.domain.usecase

import com.travelfinder.domain.model.POI
import com.travelfinder.domain.repository.PostRepository
import javax.inject.Inject

/**
 * 按位置搜索兴趣点用例
 */
class SearchPOIsByLocationUseCase @Inject constructor(
    private val postRepository: PostRepository
) {
    suspend operator fun invoke(
        latitude: Double,
        longitude: Double,
        radiusMeters: Int = 5000
    ): Result<List<POI>> {
        return postRepository.getPostsByLocation(latitude, longitude, radiusMeters)
            .map { posts -> posts.map { it.toPOI() } }
    }
}
