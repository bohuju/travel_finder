package com.travelfinder.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.travelfinder.domain.model.Location
import com.travelfinder.domain.model.POI
import com.travelfinder.domain.usecase.SearchPOIsUseCase
import com.travelfinder.domain.usecase.SearchPOIsByLocationUseCase
import com.travelfinder.presentation.state.MapUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 地图页面 ViewModel
 */
@HiltViewModel
class MapViewModel @Inject constructor(
    private val searchPOIsUseCase: SearchPOIsUseCase,
    private val searchPOIsByLocationUseCase: SearchPOIsByLocationUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<MapUiState>(MapUiState.Idle)
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    private val _selectedPOI = MutableStateFlow<POI?>(null)
    val selectedPOI: StateFlow<POI?> = _selectedPOI.asStateFlow()

    private val _currentLocation = MutableStateFlow<Location?>(null)
    val currentLocation: StateFlow<Location?> = _currentLocation.asStateFlow()

    /**
     * 搜索 POI
     */
    fun searchPOIs(keyword: String) {
        if (keyword.isBlank()) return

        viewModelScope.launch {
            _uiState.value = MapUiState.Loading

            searchPOIsUseCase(keyword)
                .onSuccess { pois ->
                    _uiState.value = MapUiState.Success(pois)
                }
                .onFailure { e ->
                    _uiState.value = MapUiState.Error(e.message ?: "搜索失败")
                }
        }
    }

    /**
     * 按位置搜索 POI
     */
    fun searchPOIsNearby(latitude: Double, longitude: Double, radiusMeters: Int = 5000) {
        viewModelScope.launch {
            _uiState.value = MapUiState.Loading
            val currentAddress = _currentLocation.value?.address.orEmpty()
            _currentLocation.value = Location(
                latitude = latitude,
                longitude = longitude,
                address = currentAddress
            )

            searchPOIsByLocationUseCase(latitude, longitude, radiusMeters)
                .onSuccess { pois ->
                    _uiState.value = MapUiState.Success(pois)
                }
                .onFailure { e ->
                    _uiState.value = MapUiState.Error(e.message ?: "搜索失败")
                }
        }
    }

    /**
     * 选择 POI
     */
    fun selectPOI(poi: POI) {
        _selectedPOI.value = poi
    }

    /**
     * 清除选择
     */
    fun clearSelectedPOI() {
        _selectedPOI.value = null
    }

    /**
     * 更新当前位置
     */
    fun updateCurrentLocation(location: Location) {
        _currentLocation.value = location
    }
}
