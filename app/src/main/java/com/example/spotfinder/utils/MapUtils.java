package com.example.spotfinder.utils;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;

/**
 * A utility class for common map operations.
 */
public class MapUtils {
    /**
     * Clears the map and shows a single marker.
     */
    public static void showMarker(GoogleMap map, double lat, double lng, String title, String snippet) {
        LatLng point = new LatLng(lat, lng);
        map.clear();
        map.addMarker(new MarkerOptions().position(point).title(title).snippet(snippet));
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(point, 12f));
    }

    /**
     * Adds a marker to the map without clearing it.
     */
    public static void addMarker(GoogleMap map, double lat, double lng, String title, String snippet) {
        LatLng point = new LatLng(lat, lng);
        map.addMarker(new MarkerOptions().position(point).title(title).snippet(snippet));
    }

    /**
     * Focuses the camera on a list of markers.
     */
    public static void focusOnAllMarkers(GoogleMap map, List<LatLng> points) {
        if (points == null || points.isEmpty()) {
            return;
        }
        if (points.size() == 1) {
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(points.get(0), 12f));
            return;
        }

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (LatLng point : points) {
            builder.include(point);
        }
        LatLngBounds bounds = builder.build();
        int padding = 100; // offset from edges of the map in pixels
        map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding));
    }
}
