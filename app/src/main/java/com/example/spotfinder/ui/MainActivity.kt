package com.example.spotfinder.ui

import android.database.Cursor
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.spotfinder.R
import com.example.spotfinder.adapter.CustomInfoWindowAdapter
import com.example.spotfinder.adapter.LocationAdapter
import com.example.spotfinder.database.LocationDBHelper
import com.example.spotfinder.databinding.ActivityMainBinding
import com.example.spotfinder.model.LocationModel
import com.example.spotfinder.utils.MapUtils
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng

/**
 * The main activity of the application. It handles the user interface and interactions.
 */
class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    // View binding for the activity
    private lateinit var binding: ActivityMainBinding
    // Database helper for location data
    private lateinit var dbHelper: LocationDBHelper
    // Google Map instance
    private lateinit var map: GoogleMap
    // Adapter for the location list
    private lateinit var adapter: LocationAdapter
    // List of locations
    private var locationList = mutableListOf<LocationModel>()
    // Flag to check if the user is updating a location
    private var isUpdate = false

    /**
     * Called when the activity is first created.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        dbHelper = LocationDBHelper(this)

        // Initialize map fragment
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Initialize RecyclerView
        adapter = LocationAdapter(locationList) { location ->
            binding.etLocationName.setText(location.locationName)
            MapUtils.showMarker(map, location.latitude, location.longitude, location.locationName, "${location.address}\nLat: ${location.latitude}, Lng: ${location.longitude}")
        }
        binding.rvLocations.layoutManager = LinearLayoutManager(this)
        binding.rvLocations.adapter = adapter

        // Live search listener
        binding.etLocationName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val searchText = s.toString().trim()
                if (searchText.isNotEmpty()) {
                    searchLocations(searchText)
                } else {
                    map.clear()
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        // Search button listener
        binding.btnSearch.setOnClickListener {
            val name = binding.etLocationName.text.toString().trim()
            if (name.isEmpty()) {
                Toast.makeText(this, R.string.enter_name, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            searchLocations(name)
        }

        // Add button listener
        binding.btnAdd.setOnClickListener {
            val name = binding.etLocationName.text.toString().trim()
            if (name.isEmpty()) {
                Toast.makeText(this, R.string.enter_name, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            isUpdate = false
            showDetailsContainer(null)
        }

        // Update button listener
        binding.btnUpdate.setOnClickListener {
            val name = binding.etLocationName.text.toString().trim()
            if (name.isEmpty()) {
                Toast.makeText(this, R.string.enter_name, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val cursor: Cursor = dbHelper.getLocationByName(name)
            if (cursor.moveToFirst()) {
                val location = LocationModel(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow(LocationDBHelper.COL_ID)),
                    locationName = cursor.getString(cursor.getColumnIndexOrThrow(LocationDBHelper.COL_LOCATION_NAME)),
                    address = cursor.getString(cursor.getColumnIndexOrThrow(LocationDBHelper.COL_ADDRESS)),
                    latitude = cursor.getDouble(cursor.getColumnIndexOrThrow(LocationDBHelper.COL_LAT)),
                    longitude = cursor.getDouble(cursor.getColumnIndexOrThrow(LocationDBHelper.COL_LNG))
                )
                isUpdate = true
                showDetailsContainer(location)
            } else {
                Toast.makeText(this, R.string.no_result_found, Toast.LENGTH_SHORT).show()
            }
            cursor.close()
        }

        // Delete button listener
        binding.btnDelete.setOnClickListener {
            val name = binding.etLocationName.text.toString().trim()
            val cursor = dbHelper.getLocationByName(name)
            if (cursor.moveToFirst()) {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow(LocationDBHelper.COL_ID))
                dbHelper.deleteLocation(id)
                Toast.makeText(this, R.string.deleted_success, Toast.LENGTH_SHORT).show()
                refreshList()
                clearInputs()
            } else {
                Toast.makeText(this, R.string.no_result_found, Toast.LENGTH_SHORT).show()
            }
            cursor.close()
        }

        // Show all button listener
        binding.btnShowAll.setOnClickListener {
            map.clear()
            val latLngList = mutableListOf<LatLng>()
            for (location in locationList) {
                MapUtils.addMarker(map, location.latitude, location.longitude, location.locationName, "${location.address}\nLat: ${location.latitude}, Lng: ${location.longitude}")
                latLngList.add(LatLng(location.latitude, location.longitude))
            }
            MapUtils.focusOnAllMarkers(map, latLngList)
        }

        // Save button listener
        binding.btnSave.setOnClickListener {
            val name = binding.etLocationName.text.toString().trim()
            val addr = binding.etAddress.text.toString().trim()
            val lat = binding.etLatitude.text.toString().toDoubleOrNull()
            val lng = binding.etLongitude.text.toString().toDoubleOrNull()

            if (name.isEmpty() || addr.isEmpty() || lat == null || lng == null) {
                Toast.makeText(this, R.string.fill_all_fields, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (isUpdate) {
                val cursor = dbHelper.getLocationByName(name)
                if (cursor.moveToFirst()) {
                    val id = cursor.getInt(cursor.getColumnIndexOrThrow(LocationDBHelper.COL_ID))
                    dbHelper.updateLocation(id, name, addr, lat, lng)
                    Toast.makeText(this, R.string.updated_success, Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, R.string.no_result_found, Toast.LENGTH_SHORT).show()
                }
                cursor.close()
            } else {
                val success = dbHelper.insertLocation(name, addr, lat, lng)
                if (success) {
                    Toast.makeText(this, R.string.added_success, Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, R.string.error_insert, Toast.LENGTH_SHORT).show()
                }
            }
            refreshList()
            clearInputs()
            binding.detailsContainer.visibility = View.GONE
        }

        refreshList()
    }

    /**
     * Called when the map is ready to be used.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map.setInfoWindowAdapter(CustomInfoWindowAdapter(this))
        val toronto = LatLng(43.7, -79.4)
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(toronto, 9f))
    }

    /**
     * Refreshes the list of locations from the database.
     */
    private fun refreshList() {
        locationList.clear()
        val cursor = dbHelper.getAllLocations()
        while (cursor.moveToNext()) {
            locationList.add(
                LocationModel(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow(LocationDBHelper.COL_ID)),
                    locationName = cursor.getString(cursor.getColumnIndexOrThrow(LocationDBHelper.COL_LOCATION_NAME)),
                    address = cursor.getString(cursor.getColumnIndexOrThrow(LocationDBHelper.COL_ADDRESS)),
                    latitude = cursor.getDouble(cursor.getColumnIndexOrThrow(LocationDBHelper.COL_LAT)),
                    longitude = cursor.getDouble(cursor.getColumnIndexOrThrow(LocationDBHelper.COL_LNG))
                )
            )
        }
        cursor.close()
        adapter.updateData(locationList)
    }

    /**
     * Searches for locations by name and displays them on the map.
     */
    private fun searchLocations(query: String) {
        map.clear()
        val cursor = dbHelper.searchLocationsByName(query)
        if (cursor.moveToFirst()) {
            val lat = cursor.getDouble(cursor.getColumnIndexOrThrow(LocationDBHelper.COL_LAT))
            val lng = cursor.getDouble(cursor.getColumnIndexOrThrow(LocationDBHelper.COL_LNG))
            val name = cursor.getString(cursor.getColumnIndexOrThrow(LocationDBHelper.COL_LOCATION_NAME))
            val address = cursor.getString(cursor.getColumnIndexOrThrow(LocationDBHelper.COL_ADDRESS))
            MapUtils.showMarker(map, lat, lng, name, "$address\nLat: $lat, Lng: $lng")
        } else {
            Toast.makeText(this, R.string.no_result_found, Toast.LENGTH_SHORT).show()
        }
        cursor.close()
    }

    /**
     * Clears all input fields.
     */
    private fun clearInputs() {
        binding.etLocationName.text?.clear()
        binding.etAddress.text?.clear()
        binding.etLatitude.text?.clear()
        binding.etLongitude.text?.clear()
    }

    /**
     * Shows the details container for adding or updating a location.
     */
    private fun showDetailsContainer(location: LocationModel?) {
        binding.detailsContainer.visibility = View.VISIBLE
        if (location != null) {
            binding.etAddress.setText(location.address)
            binding.etLatitude.setText(location.latitude.toString())
            binding.etLongitude.setText(location.longitude.toString())
        } else {
            binding.etAddress.text?.clear()
            binding.etLatitude.text?.clear()
            binding.etLongitude.text?.clear()
        }
    }
}
