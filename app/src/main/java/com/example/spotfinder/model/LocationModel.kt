package com.example.spotfinder.model

/**
 * A data class to hold location information.
 */
data class LocationModel(
    // The unique ID of the location.
    val id: Int,
    // The name of the location.
    val locationName: String,
    // The address of the location.
    val address: String,
    // The latitude of the location.
    val latitude: Double,
    // The longitude of the location.
    val longitude: Double
)
