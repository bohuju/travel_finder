package com.travelfinder.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
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
    private val onRemoveClick: ((POI) -> Unit)? = null
) : ListAdapter<POI, POIAdapter.POIViewHolder>(POIDiffCallback()) {

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

    inner class POIViewHolder(
        private val binding: ItemPoiBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onPOIClick(getItem(position))
                }
            }

            binding.removeButton.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onRemoveClick?.invoke(getItem(position))
                }
            }
        }

        fun bind(poi: POI) {
            binding.poiName.text = poi.name
            binding.poiAddress.text = poi.location.address
            binding.poiRating.text = String.format("%.1f", poi.rating)
            binding.poiTags.text = poi.tags.take(3).joinToString(" / ")

            // 显示第一张图片
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
                binding.visitOrder.visibility = android.view.View.VISIBLE
                binding.visitOrder.text = order.toString()
            } ?: run {
                binding.visitOrder.visibility = android.view.View.GONE
            }
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
