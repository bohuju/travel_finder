package com.travelfinder.domain.model

/**
 * 用户评价
 */
data class Review(
    val id: String,
    val author: String,
    val content: String,
    val rating: Float,
    val images: List<String>,
    val date: Long
)
