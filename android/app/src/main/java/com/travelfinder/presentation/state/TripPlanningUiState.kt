package com.travelfinder.presentation.state

import com.travelfinder.domain.model.POI

/**
 * 行程规划页 UI 状态
 *
 * 这个状态只负责表达“规划操作”的过程与结果，不承载页面数据本身。
 * 页面数据通过 `selectedPOIs` 和 `currentTrip` 的 StateFlow 单独输出。
 */
sealed class TripPlanningUiState {
    object Idle : TripPlanningUiState()
    object Loading : TripPlanningUiState()
    data class Success(val tripName: String, val pois: List<POI>) : TripPlanningUiState()
    data class Error(val message: String) : TripPlanningUiState()
}
