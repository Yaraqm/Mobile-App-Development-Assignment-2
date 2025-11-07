package com.example.spotfinder.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.example.spotfinder.R
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker

/**
 * A custom adapter for displaying a custom info window for a marker on a Google Map.
 *
 * This adapter is responsible for inflating a custom layout and binding the marker's
 * title and snippet data to the views within that layout. It uses the default info
 * window frame/background by returning null from `getInfoWindow` and provides the
 * custom content view via `getInfoContents`.
 *
 */
class CustomInfoWindowAdapter(private val context: Context) : GoogleMap.InfoWindowAdapter {

    override fun getInfoWindow(marker: Marker): View? {
        return null // Use default info window background
    }

    override fun getInfoContents(marker: Marker): View {
        val view = LayoutInflater.from(context).inflate(R.layout.custom_info_window, null)

        val title = view.findViewById<TextView>(R.id.tvTitle)
        val snippet = view.findViewById<TextView>(R.id.tvSnippet)

        title.text = marker.title
        snippet.text = marker.snippet

        return view
    }
}
