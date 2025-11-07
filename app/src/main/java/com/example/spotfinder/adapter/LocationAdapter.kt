package com.example.spotfinder.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.spotfinder.R
import com.example.spotfinder.model.LocationModel

class LocationAdapter(
    private var locationList: List<LocationModel>,
    private val onItemClick: (LocationModel) -> Unit
) : RecyclerView.Adapter<LocationAdapter.LocationViewHolder>() {

    inner class LocationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvLocationName: TextView = itemView.findViewById(R.id.tvLocationName)
        val tvAddress: TextView = itemView.findViewById(R.id.tvAddress)
        val tvCoordinates: TextView = itemView.findViewById(R.id.tvCoordinates)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_location, parent, false)
        return LocationViewHolder(view)
    }

    override fun onBindViewHolder(holder: LocationViewHolder, position: Int) {
        val location = locationList[position]
        holder.tvLocationName.text = location.locationName
        holder.tvAddress.text = location.address
        holder.tvCoordinates.text = "(${location.latitude}, ${location.longitude})"
        holder.itemView.setOnClickListener { onItemClick(location) }
    }

    override fun getItemCount(): Int = locationList.size

    fun updateData(newList: List<LocationModel>) {
        locationList = newList
        notifyDataSetChanged()
    }
}
