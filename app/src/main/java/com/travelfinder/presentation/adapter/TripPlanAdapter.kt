package com.travelfinder.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.travelfinder.databinding.ItemTripPlanBinding
import com.travelfinder.domain.model.TripPlan
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 行程计划适配器
 */
class TripPlanAdapter(
    private val onTripClick: (TripPlan) -> Unit,
    private val onDeleteClick: (TripPlan) -> Unit
) : ListAdapter<TripPlan, TripPlanAdapter.TripViewHolder>(TripDiffCallback()) {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TripViewHolder {
        val binding = ItemTripPlanBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TripViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TripViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class TripViewHolder(
        private val binding: ItemTripPlanBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onTripClick(getItem(position))
                }
            }

            binding.deleteButton.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onDeleteClick(getItem(position))
                }
            }
        }

        fun bind(trip: TripPlan) {
            binding.tripName.text = trip.name
            binding.poiCount.text = "${trip.pois.size} 个景点"
            binding.tripRating.text = String.format("%.1f", trip.calculateOverallRating())

            // 日期显示
            val dateText = when {
                trip.startDate != null && trip.endDate != null -> {
                    "${dateFormat.format(Date(trip.startDate))} ~ ${dateFormat.format(Date(trip.endDate))}"
                }
                trip.startDate != null -> {
                    dateFormat.format(Date(trip.startDate))
                }
                else -> {
                    dateFormat.format(Date(trip.createdAt))
                }
            }
            binding.tripDate.text = dateText

            // 显示停留时间
            val totalMinutes = trip.totalStayDuration()
            if (totalMinutes > 0) {
                val hours = totalMinutes / 60
                val minutes = totalMinutes % 60
                binding.tripDuration.text = if (hours > 0) "${hours}h ${minutes}min" else "${minutes}min"
            } else {
                binding.tripDuration.text = ""
            }
        }
    }

    private class TripDiffCallback : DiffUtil.ItemCallback<TripPlan>() {
        override fun areItemsTheSame(oldItem: TripPlan, newItem: TripPlan): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: TripPlan, newItem: TripPlan): Boolean {
            return oldItem == newItem
        }
    }
}
