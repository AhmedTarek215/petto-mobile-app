package com.example.petto.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.petto.R

class ProfileImageAdapter(
    private val imageUrls: List<String>,
    private val onImageSelected: (String) -> Unit
) : RecyclerView.Adapter<ProfileImageAdapter.ImageViewHolder>() {

    private var selectedPosition = -1

    inner class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageProfileOption: ImageView = itemView.findViewById(R.id.imageProfileOption)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    selectedPosition = position
                    notifyDataSetChanged()
                    onImageSelected(imageUrls[position])
                }
            }
        }

        fun bind(url: String, isSelected: Boolean) {
            Glide.with(itemView.context).load(url).into(imageProfileOption)
            imageProfileOption.alpha = if (isSelected) 1.0f else 0.5f
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_profile_image, parent, false)
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val url = imageUrls[position]
        holder.bind(url, position == selectedPosition)
    }

    override fun getItemCount(): Int = imageUrls.size
}
