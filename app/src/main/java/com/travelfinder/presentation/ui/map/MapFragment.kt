package com.travelfinder.presentation.ui.map

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.travelfinder.databinding.FragmentMapBinding
import com.travelfinder.domain.model.POI
import com.travelfinder.presentation.adapter.POIAdapter
import com.travelfinder.presentation.state.MapUiState
import com.travelfinder.presentation.viewmodel.MapViewModel
import com.travelfinder.presentation.viewmodel.TripViewModel
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

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocation = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseLocation = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

        if (fineLocation || coarseLocation) {
            enableMyLocation()
        } else {
            Toast.makeText(requireContext(), "需要位置权限才能使用附近功能", Toast.LENGTH_SHORT).show()
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
            }
        )

        binding.poiResultsRecycler.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = poiAdapter
        }
    }

    private fun setupSearch() {
        binding.searchButton.setOnClickListener {
            val keyword = binding.searchInput.text.toString()
            if (keyword.isNotBlank()) {
                mapViewModel.searchPOIs(keyword)
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
                Toast.makeText(requireContext(), "已添加到行程", Toast.LENGTH_SHORT).show()
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
                        poi?.let { showPOIDetails(it) } ?: hidePOIDetails()
                    }
                }
            }
        }
    }

    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
    }

    private fun showPOIs(pois: List<POI>) {
        binding.progressBar.visibility = View.GONE
        mapViewModel.clearSelectedPOI()
        poiAdapter.submitList(pois)
        binding.poiResultsRecycler.visibility = if (pois.isEmpty()) View.GONE else View.VISIBLE
    }

    private fun showError(message: String) {
        binding.progressBar.visibility = View.GONE
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }

    private fun clearMarkers() {
        if (::poiAdapter.isInitialized) {
            poiAdapter.submitList(emptyList())
        }
        binding.poiResultsRecycler.visibility = View.GONE
    }

    private fun showPOIDetails(poi: POI) {
        binding.poiDetailSheet.visibility = View.VISIBLE
        binding.poiName.text = poi.name
        binding.poiAddress.text = poi.location.address
        binding.poiRating.text = "评分: ${poi.rating}"
        binding.poiDescription.text = poi.description

        binding.addToTripButton.visibility = View.VISIBLE
    }

    private fun hidePOIDetails() {
        binding.poiDetailSheet.visibility = View.GONE
        binding.addToTripButton.visibility = View.GONE
    }

    private fun checkLocationPermissionAndSearch() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                // Permission already granted
                // In a real app, we would use AMap's location client here
                // For now, use a default location
                mapViewModel.searchPOIsNearby(31.2304, 121.4737, 5000)
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
        // Placeholder map implementation: location behavior is delegated to search nearby.
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
