package com.example.petto.ui.Services

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.petto.R
import com.example.petto.data.model.PetService

class ServicesAdapter(
    private val serviceList: List<PetService>,
    private val onClick: (PetService) -> Unit
) : RecyclerView.Adapter<ServicesAdapter.ServiceViewHolder>() {

    inner class ServiceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image: ImageView = itemView.findViewById(R.id.serviceImage)
        val name: TextView = itemView.findViewById(R.id.serviceName)
        val rating: TextView = itemView.findViewById(R.id.serviceRating)
        val bookmark: ImageView = itemView.findViewById(R.id.bookmarkIcon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServiceViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_services, parent, false)
        return ServiceViewHolder(view)
    }

    override fun onBindViewHolder(holder: ServiceViewHolder, position: Int) {
        val service = serviceList[position]

        holder.name.text = service.name
        holder.rating.text = String.format("%.1f", service.average_rating)

        Glide.with(holder.itemView.context)
            .asBitmap()
            .load(service.imageUrl)
            .into(object : com.bumptech.glide.request.target.CustomTarget<android.graphics.Bitmap>() {
                override fun onResourceReady(resource: android.graphics.Bitmap, transition: com.bumptech.glide.request.transition.Transition<in android.graphics.Bitmap>?) {
                    val resized = android.graphics.Bitmap.createScaledBitmap(resource, 250, 110, true)
                    holder.image.setImageBitmap(resized)
                }

                override fun onLoadCleared(placeholder: android.graphics.drawable.Drawable?) {
                    holder.image.setImageDrawable(placeholder)
                }
            })


        holder.bookmark.setImageResource(R.drawable.save)

        holder.itemView.setOnClickListener {
            onClick(service)
        }
    }

    override fun getItemCount(): Int = serviceList.size
}
