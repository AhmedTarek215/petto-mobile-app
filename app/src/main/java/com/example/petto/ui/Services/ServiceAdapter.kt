package com.example.petto.ui.Services

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.petto.R
import com.example.petto.data.model.PetService


class ServiceAdapter(private val services: List<PetService>) :
    RecyclerView.Adapter<ServiceAdapter.ServiceViewHolder>() {


    class ServiceViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val serviceName: TextView = view.findViewById(R.id.tvServiceName)
        val serviceIcon: ImageView = view.findViewById(R.id.imgServiceIcon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServiceViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_service, parent, false)
        return ServiceViewHolder(view)
    }

    override fun onBindViewHolder(holder: ServiceViewHolder, position: Int) {
        holder.serviceName.text = services[position].name
        holder.serviceIcon.setImageResource(R.drawable.paws)
    }

    override fun getItemCount(): Int = services.size
}
