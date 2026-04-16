package com.travelfinder.presentation.state

import com.travelfinder.domain.model.TripPlan

/**
 * 行程列表页 UI 状态
 */
sealed class TripListUiState {
    object Loading : TripListUiState()
    data class Success(val trips: List<TripPlan>) : TripListUiState()
    data class Empty(val message: String = "暂无行程规划") : TripListUiState()
    data class Error(val message: String) : TripListUiState()
}
