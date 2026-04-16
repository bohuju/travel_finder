package com.travelfinder.domain.usecase

import com.travelfinder.domain.model.POI
import com.travelfinder.domain.repository.PostRepository
import javax.inject.Inject

/**
 * 搜索兴趣点用例
 */
class SearchPOIsUseCase @Inject constructor(
    private val postRepository: PostRepository
) {
    suspend operator fun invoke(keyword: String): Result<List<POI>> {
        return postRepository.searchPosts(keyword).map { posts ->
            posts.map { it.toPOI() }
        }
    }
}
