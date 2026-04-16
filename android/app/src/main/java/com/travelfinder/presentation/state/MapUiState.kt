package com.travelfinder.presentation.state

import com.travelfinder.domain.model.POI

/**
 * 地图页面 UI 状态
 */
sealed class MapUiState {
    object Idle : MapUiState()
    object Loading : MapUiState()
    data class Success(val pois: List<POI>) : MapUiState()
    data class Error(val message: String) : MapUiState()
}

/**
 * POI 详情页 UI 状态
 */
sealed class POIDetailUiState {
    object Loading : POIDetailUiState()
    data class Success(val poi: POI) : POIDetailUiState()
    data class Error(val message: String) : POIDetailUiState()
}
