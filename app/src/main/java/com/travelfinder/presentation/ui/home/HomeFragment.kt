package com.travelfinder.presentation.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.travelfinder.databinding.FragmentHomeBinding
import com.travelfinder.domain.model.TripPlan
import com.travelfinder.presentation.adapter.TripPlanAdapter
import com.travelfinder.presentation.state.TripListUiState
import com.travelfinder.presentation.viewmodel.TripViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * 首页 Fragment
 * 显示行程列表
 */
@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val tripViewModel: TripViewModel by activityViewModels()
    private lateinit var tripAdapter: TripPlanAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupSwipeRefresh()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        tripAdapter = TripPlanAdapter(
            onTripClick = { trip ->
                // Navigate to trip detail
                Toast.makeText(requireContext(), "查看行程: ${trip.name}", Toast.LENGTH_SHORT).show()
            },
            onDeleteClick = { trip ->
                tripViewModel.deleteTrip(trip.id)
            }
        )

        binding.tripRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = tripAdapter
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            tripViewModel.loadAllTrips()
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                tripViewModel.tripListState.collect { state ->
                    binding.swipeRefresh.isRefreshing = false

                    when (state) {
                        is TripListUiState.Loading -> showLoading()
                        is TripListUiState.Success -> showTrips(state.trips)
                        is TripListUiState.Empty -> showEmpty(state.message)
                        is TripListUiState.Error -> showError(state.message)
                    }
                }
            }
        }
    }

    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
        binding.emptyState.visibility = View.GONE
    }

    private fun showTrips(trips: List<TripPlan>) {
        binding.progressBar.visibility = View.GONE
        binding.emptyState.visibility = View.GONE
        binding.tripRecyclerView.visibility = View.VISIBLE
        tripAdapter.submitList(trips)
    }

    private fun showEmpty(message: String) {
        binding.progressBar.visibility = View.GONE
        binding.tripRecyclerView.visibility = View.GONE
        binding.emptyState.visibility = View.VISIBLE
        binding.emptyMessage.text = message
    }

    private fun showError(message: String) {
        binding.progressBar.visibility = View.GONE
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
