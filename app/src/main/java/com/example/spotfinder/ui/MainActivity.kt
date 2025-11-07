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

class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityMainBinding
    private lateinit var dbHelper: LocationDBHelper
    private lateinit var map: GoogleMap
    private lateinit var adapter: LocationAdapter
    private var locationList = mutableListOf<LocationModel>()
    private var isUpdate = false

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

        // Live search
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

        // Search Button
        binding.btnSearch.setOnClickListener {
            val name = binding.etLocationName.text.toString().trim()
            if (name.isEmpty()) {
                Toast.makeText(this, R.string.enter_name, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            searchLocations(name)
        }

        // Add Button
        binding.btnAdd.setOnClickListener {
            val name = binding.etLocationName.text.toString().trim()
            if (name.isEmpty()) {
                Toast.makeText(this, R.string.enter_name, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            isUpdate = false
            showDetailsContainer(null)
        }

        // Update Button
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

        // Delete Button
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

        // Show All Button
        binding.btnShowAll.setOnClickListener {
            map.clear()
            val latLngList = mutableListOf<LatLng>()
            for (location in locationList) {
                MapUtils.addMarker(map, location.latitude, location.longitude, location.locationName, "${location.address}\nLat: ${location.latitude}, Lng: ${location.longitude}")
                latLngList.add(LatLng(location.latitude, location.longitude))
            }
            MapUtils.focusOnAllMarkers(map, latLngList)
        }

        // Save Button
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

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map.setInfoWindowAdapter(CustomInfoWindowAdapter(this))
        val toronto = LatLng(43.7, -79.4)
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(toronto, 9f))
    }

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

    private fun searchLocations(query: String) {
        map.clear()
        val cursor = dbHelper.searchLocationsByName(query)
        val latLngList = mutableListOf<LatLng>()
        while (cursor.moveToNext()) {
            val lat = cursor.getDouble(cursor.getColumnIndexOrThrow(LocationDBHelper.COL_LAT))
            val lng = cursor.getDouble(cursor.getColumnIndexOrThrow(LocationDBHelper.COL_LNG))
            val name = cursor.getString(cursor.getColumnIndexOrThrow(LocationDBHelper.COL_LOCATION_NAME))
            val address = cursor.getString(cursor.getColumnIndexOrThrow(LocationDBHelper.COL_ADDRESS))
            MapUtils.addMarker(map, lat, lng, name, "$address\nLat: $lat, Lng: $lng")
            latLngList.add(LatLng(lat, lng))
        }
        cursor.close()
        if (latLngList.isEmpty()) {
           // No toast shown here for a smoother user experience
        }
        MapUtils.focusOnAllMarkers(map, latLngList)
    }

    private fun clearInputs() {
        binding.etLocationName.text?.clear()
        binding.etAddress.text?.clear()
        binding.etLatitude.text?.clear()
        binding.etLongitude.text?.clear()
    }

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
