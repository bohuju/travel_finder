package com.travelfinder.presentation.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.travelfinder.R
import com.travelfinder.databinding.ItemPoiBinding
import com.travelfinder.domain.model.POI

/**
 * POI 列表适配器
 */
class POIAdapter(
    private val onPOIClick: (POI) -> Unit,
    private val onRemoveClick: ((POI) -> Unit)? = null,
    private val distanceProvider: ((POI) -> String?)? = null
) : ListAdapter<POI, POIAdapter.POIViewHolder>(POIDiffCallback()) {

    private var selectedPoiId: String? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): POIViewHolder {
        val binding = ItemPoiBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return POIViewHolder(binding)
    }

    override fun onBindViewHolder(holder: POIViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    fun setSelectedPoi(selectedPoiId: String?) {
        if (this.selectedPoiId == selectedPoiId) return
        this.selectedPoiId = selectedPoiId
        notifyDataSetChanged()
    }

    inner class POIViewHolder(
        private val binding: ItemPoiBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onPOIClick(getItem(position))
                }
            }

            binding.removeButton.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onRemoveClick?.invoke(getItem(position))
                }
            }
        }

        fun bind(poi: POI) {
            binding.poiName.text = poi.name
            binding.poiAddress.text = poi.location.address
            binding.poiRating.text = binding.root.context.getString(R.string.map_rating, poi.rating)
            binding.poiTags.text = poi.tags.take(3).joinToString(" / ")
            binding.poiTags.visibility = if (poi.tags.isEmpty()) View.GONE else View.VISIBLE
            binding.removeButton.visibility = if (onRemoveClick != null) View.VISIBLE else View.GONE

            val distanceText = distanceProvider?.invoke(poi)
            binding.poiDistance.text = distanceText
            binding.poiDistance.visibility = if (distanceText.isNullOrBlank()) View.GONE else View.VISIBLE

            if (poi.images.isNotEmpty()) {
                binding.poiImage.load(poi.images.first()) {
                    placeholder(R.drawable.placeholder_image)
                    error(R.drawable.placeholder_image)
                }
            } else {
                binding.poiImage.setImageResource(R.drawable.placeholder_image)
            }

            // 显示顺序（如果已排序）
            poi.visitOrder?.let { order ->
                binding.visitOrder.visibility = View.VISIBLE
                binding.visitOrder.text = order.toString()
            } ?: run {
                binding.visitOrder.visibility = View.GONE
            }

            val isSelected = poi.id == selectedPoiId
            binding.root.strokeWidth = if (isSelected) 4 else 0
            binding.root.strokeColor = ContextCompat.getColor(
                binding.root.context,
                if (isSelected) R.color.teal_700 else android.R.color.transparent
            )
        }
    }

    private class POIDiffCallback : DiffUtil.ItemCallback<POI>() {
        override fun areItemsTheSame(oldItem: POI, newItem: POI): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: POI, newItem: POI): Boolean {
            return oldItem == newItem
        }
    }
}
