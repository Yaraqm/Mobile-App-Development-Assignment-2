package com.example.spotfinder.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.spotfinder.R
import com.example.spotfinder.model.LocationModel

/**
 * Adapter for the RecyclerView that displays a list of locations.
 * @param locationList The list of locations to display.
 * @param onItemClick A lambda function to be invoked when an item in the list is clicked.
 */
class LocationAdapter(
    private var locationList: List<LocationModel>,
    private val onItemClick: (LocationModel) -> Unit
) : RecyclerView.Adapter<LocationAdapter.LocationViewHolder>() {

    /**
     * ViewHolder for the location items.
     * @param itemView The view for a single item in the list.
     */
    inner class LocationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvLocationName: TextView = itemView.findViewById(R.id.tvLocationName)
        val tvAddress: TextView = itemView.findViewById(R.id.tvAddress)
        val tvCoordinates: TextView = itemView.findViewById(R.id.tvCoordinates)
    }


     //Creates a new ViewHolder by inflating the item layout.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_location, parent, false)
        return LocationViewHolder(view)
    }

     //Binds the data to the views in the ViewHolder.
    override fun onBindViewHolder(holder: LocationViewHolder, position: Int) {
        val location = locationList[position]
        holder.tvLocationName.text = location.locationName
        holder.tvAddress.text = location.address
        holder.tvCoordinates.text = "(${location.latitude}, ${location.longitude})"
        holder.itemView.setOnClickListener { onItemClick(location) }
    }

     //Returns the total number of items in the list.
    override fun getItemCount(): Int = locationList.size

    /**
     * Updates the data in the adapter and refreshes the list.
     * @param newList The new list of locations.
     */
    fun updateData(newList: List<LocationModel>) {
        locationList = newList
        notifyDataSetChanged()
    }
}
