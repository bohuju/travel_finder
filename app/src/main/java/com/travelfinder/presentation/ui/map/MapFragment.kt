package com.travelfinder.presentation.ui.map

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.travelfinder.R
import com.travelfinder.databinding.FragmentMapBinding
import com.travelfinder.domain.model.Location
import com.travelfinder.domain.model.POI
import com.travelfinder.presentation.navigation.ExternalMapRouteNavigator
import com.travelfinder.presentation.navigation.NavigationResult
import com.travelfinder.presentation.adapter.POIAdapter
import com.travelfinder.presentation.state.MapUiState
import com.travelfinder.presentation.viewmodel.MapViewModel
import com.travelfinder.presentation.viewmodel.TripViewModel
import com.travelfinder.util.LocationFormatter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * 地图页面 Fragment
 * 显示 POI 标记，支持搜索和选择
 */
@AndroidEntryPoint
class MapFragment : Fragment() {

    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!

    private val mapViewModel: MapViewModel by activityViewModels()
    private val tripViewModel: TripViewModel by activityViewModels()

    private lateinit var poiAdapter: POIAdapter
    private val routeNavigator by lazy { ExternalMapRouteNavigator(requireContext().applicationContext) }

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocation = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseLocation = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

        if (fineLocation || coarseLocation) {
            enableMyLocation()
        } else {
            Toast.makeText(requireContext(), getString(R.string.map_permission_needed), Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupSearch()
        setupBottomSheet()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        poiAdapter = POIAdapter(
            onPOIClick = { poi ->
                mapViewModel.selectPOI(poi)
            },
            distanceProvider = { poi ->
                LocationFormatter.formatDistance(
                    LocationFormatter.distanceMeters(mapViewModel.currentLocation.value, poi.location)
                )
            }
        )

        binding.poiResultsRecycler.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = poiAdapter
        }
    }

    private fun setupSearch() {
        binding.searchButton.setOnClickListener { submitKeywordSearch() }
        binding.searchInput.setOnEditorActionListener { _, actionId, event ->
            val isSearchAction = actionId == EditorInfo.IME_ACTION_SEARCH
            val isEnterKey = event?.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN
            if (isSearchAction || isEnterKey) {
                submitKeywordSearch()
                true
            } else {
                false
            }
        }
    }

    private fun setupBottomSheet() {
        binding.searchNearbyButton.setOnClickListener {
            checkLocationPermissionAndSearch()
        }

        binding.addToTripButton.setOnClickListener {
            mapViewModel.selectedPOI.value?.let { poi ->
                tripViewModel.addPOIToCurrentTrip(poi)
                Toast.makeText(requireContext(), getString(R.string.map_added_to_trip), Toast.LENGTH_SHORT).show()
            }
        }

        binding.navigateButton.setOnClickListener {
            val poi = mapViewModel.selectedPOI.value ?: return@setOnClickListener
            when (val result = routeNavigator.navigateTo(poi)) {
                NavigationResult.Launched -> Unit
                is NavigationResult.Unavailable -> {
                    Toast.makeText(
                        requireContext(),
                        result.reason.ifBlank { getString(R.string.map_navigation_unavailable) },
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    mapViewModel.uiState.collect { state ->
                        when (state) {
                            is MapUiState.Loading -> showLoading()
                            is MapUiState.Success -> showPOIs(state.pois)
                            is MapUiState.Error -> showError(state.message)
                            is MapUiState.Idle -> clearMarkers()
                        }
                    }
                }

                launch {
                    mapViewModel.selectedPOI.collect { poi ->
                        poiAdapter.setSelectedPoi(poi?.id)
                        poi?.let { showPOIDetails(it) } ?: hidePOIDetails()
                    }
                }

                launch {
                    mapViewModel.currentLocation.collect {
                        updateStatusCard(mapViewModel.uiState.value)
                        mapViewModel.selectedPOI.value?.let(::showPOIDetails)
                        if (::poiAdapter.isInitialized) {
                            poiAdapter.notifyDataSetChanged()
                        }
                    }
                }
            }
        }
    }

    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
        updateStatusCard(MapUiState.Loading)
    }

    private fun showPOIs(pois: List<POI>) {
        binding.progressBar.visibility = View.GONE
        mapViewModel.clearSelectedPOI()
        poiAdapter.submitList(pois)
        binding.poiResultsRecycler.visibility = if (pois.isEmpty()) View.GONE else View.VISIBLE
        updateStatusCard(MapUiState.Success(pois))
    }

    private fun showError(message: String) {
        binding.progressBar.visibility = View.GONE
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
        updateStatusCard(MapUiState.Error(message))
    }

    private fun clearMarkers() {
        if (::poiAdapter.isInitialized) {
            poiAdapter.submitList(emptyList())
        }
        binding.poiResultsRecycler.visibility = View.GONE
        updateStatusCard(MapUiState.Idle)
    }

    private fun showPOIDetails(poi: POI) {
        binding.poiDetailSheet.visibility = View.VISIBLE
        binding.poiName.text = poi.name
        binding.poiAddress.text = poi.location.address
        binding.poiRating.text = getString(R.string.map_rating, poi.rating)
        binding.poiDescription.text = poi.description
        binding.poiMeta.text = buildMetaLine(poi)

        val distanceText = LocationFormatter.formatDistance(
            LocationFormatter.distanceMeters(mapViewModel.currentLocation.value, poi.location)
        )
        binding.poiDistance.text = distanceText?.let { getString(R.string.map_selected_distance, it) }
        binding.poiDistance.visibility = if (distanceText == null) View.GONE else View.VISIBLE

        binding.addToTripButton.visibility = View.VISIBLE
        binding.navigateButton.visibility = View.VISIBLE
    }

    private fun hidePOIDetails() {
        binding.poiDetailSheet.visibility = View.GONE
        binding.addToTripButton.visibility = View.GONE
        binding.navigateButton.visibility = View.GONE
    }

    private fun checkLocationPermissionAndSearch() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                enableMyLocation()
            }
            else -> {
                locationPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
        }
    }

    private fun enableMyLocation() {
        val defaultLocation = Location(
            latitude = 31.2304,
            longitude = 121.4737,
            address = getString(R.string.map_default_location)
        )
        mapViewModel.updateCurrentLocation(defaultLocation)
        mapViewModel.searchPOIsNearby(defaultLocation.latitude, defaultLocation.longitude, 5000)
    }

    private fun submitKeywordSearch() {
        val keyword = binding.searchInput.text.toString().trim()
        if (keyword.isNotBlank()) {
            mapViewModel.searchPOIs(keyword)
        }
    }

    private fun updateStatusCard(state: MapUiState) {
        val locationLabel = LocationFormatter.formatLocationLabel(mapViewModel.currentLocation.value)
        when (state) {
            is MapUiState.Idle -> {
                binding.mapStatusTitle.text = getString(R.string.map_status_idle_title)
                binding.mapStatusSubtitle.text = getString(R.string.map_status_idle_subtitle)
            }
            is MapUiState.Loading -> {
                binding.mapStatusTitle.text = getString(R.string.map_status_loading_title)
                binding.mapStatusSubtitle.text = getString(R.string.map_status_loading_subtitle)
            }
            is MapUiState.Error -> {
                binding.mapStatusTitle.text = getString(R.string.map_status_error_title)
                binding.mapStatusSubtitle.text = getString(R.string.map_status_error_subtitle)
            }
            is MapUiState.Success -> {
                binding.mapStatusTitle.text = getString(R.string.map_status_result_title, state.pois.size)
                binding.mapStatusSubtitle.text = getString(R.string.map_status_result_subtitle, locationLabel)
            }
        }
    }

    private fun buildMetaLine(poi: POI): String {
        val tags = if (poi.tags.isEmpty()) {
            getString(R.string.map_no_tags)
        } else {
            poi.tags.joinToString(" / ")
        }
        val source = poi.source.ifBlank { getString(R.string.map_source_local) }
        return listOf(
            getString(R.string.map_tags, tags),
            getString(R.string.map_source, source)
        ).joinToString("  ·  ")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
