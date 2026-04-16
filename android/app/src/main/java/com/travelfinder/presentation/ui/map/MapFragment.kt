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
import com.baidu.location.BDAbstractLocationListener
import com.baidu.location.BDLocation
import com.baidu.location.LocationClient
import com.baidu.location.LocationClientOption
import com.baidu.mapapi.map.BaiduMap
import com.baidu.mapapi.map.BitmapDescriptorFactory
import com.baidu.mapapi.map.InfoWindow
import com.baidu.mapapi.map.MapStatusUpdateFactory
import com.baidu.mapapi.map.MapPoi
import com.baidu.mapapi.map.MarkerOptions
import com.baidu.mapapi.map.MyLocationConfiguration
import com.baidu.mapapi.map.MyLocationData
import com.baidu.mapapi.model.LatLng
import com.travelfinder.R
import com.travelfinder.databinding.FragmentMapBinding
import com.travelfinder.databinding.ViewMapInfoWindowBinding
import com.travelfinder.domain.model.Location
import com.travelfinder.domain.model.POI
import com.travelfinder.presentation.adapter.POIAdapter
import com.travelfinder.presentation.navigation.ExternalMapRouteNavigator
import com.travelfinder.presentation.navigation.NavigationResult
import com.travelfinder.presentation.state.MapUiState
import com.travelfinder.presentation.viewmodel.MapViewModel
import com.travelfinder.presentation.viewmodel.TripViewModel
import com.travelfinder.util.LocationFormatter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * 百度地图页面 Fragment
 * 展示 POI 标记、当前位置、搜索结果与详情联动
 */
@AndroidEntryPoint
class MapFragment : Fragment() {

    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!

    private val mapViewModel: MapViewModel by activityViewModels()
    private val tripViewModel: TripViewModel by activityViewModels()

    private lateinit var poiAdapter: POIAdapter
    private lateinit var baiduMap: BaiduMap

    private val routeNavigator by lazy { ExternalMapRouteNavigator(requireContext().applicationContext) }
    private var locationClient: LocationClient? = null
    private var latestPois: List<POI> = emptyList()
    private var activeInfoWindow: InfoWindow? = null

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocation = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseLocation = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

        if (fineLocation || coarseLocation) {
            enableMyLocation()
        } else {
            Toast.makeText(
                requireContext(),
                getString(R.string.map_permission_needed),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private val locationListener = object : BDAbstractLocationListener() {
        override fun onReceiveLocation(location: BDLocation?) {
            if (location == null || _binding == null) return

            val currentLocation = Location(
                latitude = location.latitude,
                longitude = location.longitude,
                address = location.addrStr.orEmpty()
            )
            mapViewModel.updateCurrentLocation(currentLocation)
            updateBaiduLocation(currentLocation)

            if (latestPois.isEmpty()) {
                mapViewModel.searchPOIsNearby(currentLocation.latitude, currentLocation.longitude, 5000)
            }
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

        setupMap()
        setupRecyclerView()
        setupSearch()
        setupMapActions()
        observeViewModel()
    }

    private fun setupMap() {
        baiduMap = binding.mapView.map
        binding.mapView.showScaleControl(false)
        binding.mapView.showZoomControls(false)
        baiduMap.isMyLocationEnabled = true
        baiduMap.setMyLocationConfiguration(MyLocationConfiguration(MyLocationConfiguration.LocationMode.NORMAL, true, null))
        baiduMap.setOnMarkerClickListener { marker ->
            (marker.extraInfo?.getString(KEY_POI_ID))?.let { poiId ->
                latestPois.firstOrNull { it.id == poiId }?.let(mapViewModel::selectPOI)
            }
            true
        }
        baiduMap.setOnMapClickListener(object : BaiduMap.OnMapClickListener {
            override fun onMapClick(point: LatLng?) {
                mapViewModel.clearSelectedPOI()
            }

            override fun onMapPoiClick(mapPoi: MapPoi?) {
            }
        })
        baiduMap.animateMapStatus(MapStatusUpdateFactory.zoomTo(DEFAULT_ZOOM))
    }

    private fun setupRecyclerView() {
        poiAdapter = POIAdapter(
            onPOIClick = { poi ->
                mapViewModel.selectPOI(poi)
                focusOnPoi(poi)
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

    private fun setupMapActions() {
        binding.searchNearbyButton.setOnClickListener {
            checkLocationPermissionAndSearch()
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
                        poi?.let {
                            focusOnPoi(it)
                            showPOIDetails(it)
                        } ?: hidePOIDetails()
                        updateStatusCard(mapViewModel.uiState.value)
                    }
                }

                launch {
                    mapViewModel.currentLocation.collect { location ->
                        updateStatusCard(mapViewModel.uiState.value)
                        updateBaiduLocation(location)
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
        latestPois = pois
        mapViewModel.clearSelectedPOI()
        poiAdapter.submitList(pois)
        binding.poiResultsRecycler.visibility = if (pois.isEmpty()) View.GONE else View.VISIBLE
        renderPoiMarkers(pois)
        updateStatusCard(MapUiState.Success(pois))
        if (pois.isNotEmpty()) {
            focusOnPoi(pois.first())
        }
    }

    private fun showError(message: String) {
        binding.progressBar.visibility = View.GONE
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
        updateStatusCard(MapUiState.Error(message))
    }

    private fun clearMarkers() {
        latestPois = emptyList()
        if (::poiAdapter.isInitialized) {
            poiAdapter.submitList(emptyList())
        }
        baiduMap.clear()
        updateBaiduLocation(mapViewModel.currentLocation.value)
        binding.poiResultsRecycler.visibility = View.GONE
        updateStatusCard(MapUiState.Idle)
    }

    private fun showPOIDetails(poi: POI) {
        if (!poi.location.isValid()) return

        val infoBinding = ViewMapInfoWindowBinding.inflate(layoutInflater)
        infoBinding.infoPoiName.text = poi.name
        infoBinding.infoPoiAddress.text = poi.location.address
        infoBinding.infoPoiRating.text = getString(R.string.map_rating, poi.rating)
        infoBinding.infoPoiMeta.text = buildMetaLine(poi)

        val distanceText = LocationFormatter.formatDistance(
            LocationFormatter.distanceMeters(mapViewModel.currentLocation.value, poi.location)
        )
        infoBinding.infoPoiDistance.text = distanceText?.let { getString(R.string.map_selected_distance, it) }
        infoBinding.infoPoiDistance.visibility = if (distanceText == null) View.GONE else View.VISIBLE

        infoBinding.infoNavigateButton.setOnClickListener {
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
        infoBinding.infoAddToTripButton.setOnClickListener {
            tripViewModel.addPOIToCurrentTrip(poi)
            Toast.makeText(requireContext(), getString(R.string.map_added_to_trip), Toast.LENGTH_SHORT).show()
        }

        activeInfoWindow = InfoWindow(
            infoBinding.root,
            LatLng(poi.location.latitude, poi.location.longitude),
            -80
        )
        baiduMap.showInfoWindow(activeInfoWindow)
    }

    private fun hidePOIDetails() {
        baiduMap.hideInfoWindow()
        activeInfoWindow = null
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
        if (locationClient == null) {
            locationClient = LocationClient(requireContext().applicationContext).apply {
                registerLocationListener(locationListener)
                locOption = LocationClientOption().apply {
                    setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy)
                    setCoorType("bd09ll")
                    setScanSpan(0)
                    setOpenGps(true)
                    setIsNeedAddress(true)
                    setIgnoreKillProcess(false)
                }
            }
        }

        if (locationClient?.isStarted != true) {
            locationClient?.start()
        } else {
            locationClient?.requestLocation()
        }
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
                binding.mapStatusSubtitle.text = mapViewModel.selectedPOI.value?.let {
                    getString(R.string.map_info_window_selected)
                } ?: getString(R.string.map_status_result_subtitle, locationLabel)
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

    private fun updateBaiduLocation(location: Location?) {
        if (location == null) return
        val locData = MyLocationData.Builder()
            .latitude(location.latitude)
            .longitude(location.longitude)
            .build()
        baiduMap.setMyLocationData(locData)
    }

    private fun renderPoiMarkers(pois: List<POI>) {
        baiduMap.clear()
        updateBaiduLocation(mapViewModel.currentLocation.value)
        activeInfoWindow = null

        val markerIcon = BitmapDescriptorFactory.fromResource(R.mipmap.ic_launcher)
        pois.forEach { poi ->
            if (!poi.location.isValid()) return@forEach

            val bundle = Bundle().apply {
                putString(KEY_POI_ID, poi.id)
            }
            baiduMap.addOverlay(
                MarkerOptions()
                    .position(LatLng(poi.location.latitude, poi.location.longitude))
                    .icon(markerIcon)
                    .extraInfo(bundle)
            )
        }
    }

    private fun focusOnPoi(poi: POI) {
        if (!poi.location.isValid()) return
        baiduMap.animateMapStatus(
            MapStatusUpdateFactory.newLatLngZoom(
                LatLng(poi.location.latitude, poi.location.longitude),
                DEFAULT_ZOOM
            )
        )
    }

    private fun Location.isValid(): Boolean {
        return latitude in -90.0..90.0 &&
            longitude in -180.0..180.0 &&
            !(latitude == 0.0 && longitude == 0.0)
    }

    override fun onResume() {
        super.onResume()
        if (_binding != null) {
            binding.mapView.onResume()
        }
    }

    override fun onPause() {
        if (_binding != null) {
            binding.mapView.onPause()
        }
        super.onPause()
    }

    override fun onDestroyView() {
        locationClient?.stop()
        locationClient?.unRegisterLocationListener(locationListener)
        locationClient = null
        activeInfoWindow = null
        if (::baiduMap.isInitialized) {
            baiduMap.isMyLocationEnabled = false
        }
        binding.mapView.onDestroy()
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val KEY_POI_ID = "poi_id"
        private const val DEFAULT_ZOOM = 14f
    }
}
