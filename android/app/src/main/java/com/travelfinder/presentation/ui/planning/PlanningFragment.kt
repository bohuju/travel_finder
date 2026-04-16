package com.travelfinder.presentation.ui.planning

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
import com.travelfinder.databinding.FragmentPlanningBinding
import com.travelfinder.domain.model.POI
import com.travelfinder.presentation.adapter.POIAdapter
import com.travelfinder.presentation.state.TripPlanningUiState
import com.travelfinder.presentation.viewmodel.TripViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * 行程规划页面 Fragment
 */
@AndroidEntryPoint
class PlanningFragment : Fragment() {

    private var _binding: FragmentPlanningBinding? = null
    private val binding get() = _binding!!

    private val tripViewModel: TripViewModel by activityViewModels()
    private lateinit var poiAdapter: POIAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlanningBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupCreateButton()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        poiAdapter = POIAdapter(
            onPOIClick = { /* Navigate to detail */ },
            onRemoveClick = { poi ->
                tripViewModel.removePOIFromSelection(poi.id)
            }
        )

        binding.poiRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = poiAdapter
        }
    }

    private fun setupCreateButton() {
        binding.createTripButton.setOnClickListener {
            val tripName = binding.tripNameInput.text.toString()
            if (tripName.isBlank()) {
                Toast.makeText(requireContext(), "请输入行程名称", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            tripViewModel.createTripPlan(tripName)
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    tripViewModel.selectedPOIs.collect { pois ->
                        poiAdapter.submitList(pois)
                        updateUI(pois)
                    }
                }

                launch {
                    tripViewModel.planningState.collect { state ->
                        when (state) {
                            is TripPlanningUiState.Loading -> showLoading()
                            is TripPlanningUiState.Success -> showSuccess(state)
                            is TripPlanningUiState.Error -> showError(state.message)
                            is TripPlanningUiState.Idle -> hideLoading()
                        }
                    }
                }
            }
        }
    }

    private fun updateUI(pois: List<POI>) {
        binding.emptyState.visibility = if (pois.isEmpty()) View.VISIBLE else View.GONE
        binding.poiRecyclerView.visibility = if (pois.isEmpty()) View.GONE else View.VISIBLE
        binding.selectedCount.text = "已选择 ${pois.size} 个景点"
    }

    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
    }

    private fun hideLoading() {
        binding.progressBar.visibility = View.GONE
    }

    private fun showSuccess(state: TripPlanningUiState.Success) {
        binding.progressBar.visibility = View.GONE
        Toast.makeText(requireContext(), "行程「${state.tripName}」创建成功！", Toast.LENGTH_SHORT).show()
        binding.tripNameInput.text?.clear()
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
