package com.travelfinder.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.travelfinder.domain.model.POI
import com.travelfinder.domain.model.TripPlan
import com.travelfinder.domain.usecase.AddPOIToTripUseCase
import com.travelfinder.domain.usecase.CreateTripPlanUseCase
import com.travelfinder.domain.usecase.DeleteTripPlanUseCase
import com.travelfinder.domain.usecase.GetAllTripPlansUseCase
import com.travelfinder.domain.usecase.GetTripPlanDetailUseCase
import com.travelfinder.domain.usecase.RemovePOIFromTripUseCase
import com.travelfinder.presentation.state.TripListUiState
import com.travelfinder.presentation.state.TripPlanningUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 行程规划 ViewModel
 */
@HiltViewModel
class TripViewModel @Inject constructor(
    private val createTripPlanUseCase: CreateTripPlanUseCase,
    private val getAllTripPlansUseCase: GetAllTripPlansUseCase,
    private val getTripPlanDetailUseCase: GetTripPlanDetailUseCase,
    private val addPOIToTripUseCase: AddPOIToTripUseCase,
    private val removePOIFromTripUseCase: RemovePOIFromTripUseCase,
    private val deleteTripPlanUseCase: DeleteTripPlanUseCase
) : ViewModel() {

    private val _tripListState = MutableStateFlow<TripListUiState>(TripListUiState.Loading)
    val tripListState: StateFlow<TripListUiState> = _tripListState.asStateFlow()

    private val _planningState = MutableStateFlow<TripPlanningUiState>(TripPlanningUiState.Idle)
    val planningState: StateFlow<TripPlanningUiState> = _planningState.asStateFlow()

    private val _currentTrip = MutableStateFlow<TripPlan?>(null)
    val currentTrip: StateFlow<TripPlan?> = _currentTrip.asStateFlow()

    private val _selectedPOIs = MutableStateFlow<List<POI>>(emptyList())
    val selectedPOIs: StateFlow<List<POI>> = _selectedPOIs.asStateFlow()

    private var tripListCollectionJob: Job? = null
    private var currentTripDetailJob: Job? = null

    init {
        loadAllTrips()
    }

    /**
     * 加载所有行程
     */
    fun loadAllTrips() {
        tripListCollectionJob?.cancel()
        tripListCollectionJob = viewModelScope.launch {
            _tripListState.value = TripListUiState.Loading

            getAllTripPlansUseCase()
                .catch { e ->
                    _tripListState.value = TripListUiState.Error(e.message ?: "加载失败")
                }
                .collect { trips ->
                    val tripSnapshots = trips.map { it.snapshot() }
                    _tripListState.value = if (tripSnapshots.isEmpty()) {
                        TripListUiState.Empty()
                    } else {
                        TripListUiState.Success(tripSnapshots)
                    }
                }
        }
    }

    /**
     * 创建新行程
     */
    fun createTripPlan(name: String, startDate: Long? = null, endDate: Long? = null) {
        val pois = _selectedPOIs.value
        if (pois.isEmpty()) {
            _planningState.value = TripPlanningUiState.Error("请先选择景点")
            return
        }

        viewModelScope.launch {
            _planningState.value = TripPlanningUiState.Loading

            createTripPlanUseCase(name, pois, startDate, endDate)
                .onSuccess { trip ->
                    _currentTrip.value = trip.snapshot()
                    _selectedPOIs.value = emptyList()
                    _planningState.value = TripPlanningUiState.Success(trip.name, trip.pois.map { it.copy() })
                    clearCurrentDetailJob()
                    loadAllTrips() // Refresh list
                }
                .onFailure { e ->
                    _planningState.value = TripPlanningUiState.Error(e.message ?: "创建失败")
                }
        }
    }

    /**
     * 加载行程详情
     */
    fun loadTripDetail(tripId: String) {
        clearCurrentDetailJob()
        currentTripDetailJob = viewModelScope.launch {
            _planningState.value = TripPlanningUiState.Loading

            getTripPlanDetailUseCase(tripId)
                .onSuccess { trip ->
                    val snapshot = trip.snapshot()
                    _currentTrip.value = snapshot
                    _selectedPOIs.value = snapshot.pois.toList()
                    _planningState.value = TripPlanningUiState.Idle
                }
                .onFailure { e ->
                    _planningState.value = TripPlanningUiState.Error(e.message ?: "加载失败")
                }

            currentTripDetailJob = null
        }
    }

    /**
     * 添加 POI 到当前行程
     */
    fun addPOIToCurrentTrip(poi: POI) {
        val currentTrip = _currentTrip.value
        if (currentTrip == null) {
            val currentList = _selectedPOIs.value.toMutableList()
            if (!currentList.any { it.id == poi.id }) {
                currentList.add(poi.withVisitOrder(currentList.size + 1))
                _selectedPOIs.value = currentList
            }
            return
        }

        viewModelScope.launch {
            val nextOrder = currentTrip.pois.size + 1
            addPOIToTripUseCase(currentTrip.id, poi, nextOrder)
                .onSuccess { trip ->
                    val snapshot = trip.snapshot()
                    _currentTrip.value = snapshot
                    _selectedPOIs.value = snapshot.pois.toList()
                    _planningState.value = TripPlanningUiState.Idle
                    loadAllTrips()
                }
                .onFailure { e ->
                    _planningState.value = TripPlanningUiState.Error(e.message ?: "添加失败")
                }
        }
    }

    /**
     * 从当前选择中移除 POI
     */
    fun removePOIFromSelection(poiId: String) {
        val currentTrip = _currentTrip.value
        if (currentTrip == null) {
            val currentList = _selectedPOIs.value.toMutableList()
            currentList.removeAll { it.id == poiId }
            _selectedPOIs.value = currentList.mapIndexed { index, poi ->
                poi.withVisitOrder(index + 1)
            }
            return
        }

        viewModelScope.launch {
            removePOIFromTripUseCase(currentTrip.id, poiId)
                .onSuccess { trip ->
                    val snapshot = trip.snapshot()
                    _currentTrip.value = snapshot
                    _selectedPOIs.value = snapshot.pois.toList()
                    _planningState.value = TripPlanningUiState.Idle
                    loadAllTrips()
                }
                .onFailure { e ->
                    _planningState.value = TripPlanningUiState.Error(e.message ?: "移除失败")
                }
        }
    }

    /**
     * 清空 POI 选择
     */
    fun clearPOISelection() {
        _selectedPOIs.value = emptyList()
        _currentTrip.value = null
        clearCurrentDetailJob()
    }

    /**
     * 更新行程中的 POI 顺序
     */
    fun reorderPOIs(orderedIds: List<String>) {
        val currentList = _selectedPOIs.value.toMutableList()
        val reordered = orderedIds.mapIndexedNotNull { index, id ->
            currentList.find { it.id == id }?.withVisitOrder(index + 1)
        }
        _selectedPOIs.value = reordered

        _currentTrip.value?.let { trip ->
            _currentTrip.value = trip.copy(pois = reordered.toMutableList())
        }
    }

    /**
     * 删除行程
     */
    fun deleteTrip(tripId: String) {
        viewModelScope.launch {
            deleteTripPlanUseCase(tripId)
                .onSuccess {
                    if (_currentTrip.value?.id == tripId) {
                        clearPOISelection()
                    }
                    loadAllTrips()
                }
                .onFailure { e ->
                    _tripListState.value = TripListUiState.Error(e.message ?: "删除失败")
                }
        }
    }

    private fun clearCurrentDetailJob() {
        currentTripDetailJob?.cancel()
        currentTripDetailJob = null
    }

    private fun TripPlan.snapshot(): TripPlan {
        return copy(pois = pois.map { it.copy() }.toMutableList())
    }
}
