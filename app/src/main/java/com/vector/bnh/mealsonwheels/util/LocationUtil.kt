package com.vector.bnh.mealsonwheels.util

import android.content.Context
import android.location.Geocoder
import android.location.Location
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object LocationUtil {
    suspend fun getLatLngFromAddress(context: Context, address: String): LatLng? = withContext(Dispatchers.IO) {
        try {
            val results = Geocoder(context).getFromLocationName(address, 1)
            results?.firstOrNull()?.let {
                LatLng(it.latitude, it.longitude)
            }
        } catch (e: Exception) {
            null
        }
    }

    fun distanceBetween(loc: Location?, latLng: LatLng?): Float {
        if (loc == null || latLng == null) return Float.MAX_VALUE
        val result = FloatArray(1)
        Location.distanceBetween(loc.latitude, loc.longitude, latLng.latitude, latLng.longitude, result)
        return result[0]
    }
}