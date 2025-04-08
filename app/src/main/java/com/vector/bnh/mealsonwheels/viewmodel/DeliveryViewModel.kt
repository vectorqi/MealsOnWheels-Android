package com.vector.bnh.mealsonwheels.viewmodel

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.Geocoder
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.vector.bnh.mealsonwheels.model.DeliveryItem
import com.vector.bnh.mealsonwheels.model.DeliveryStatus
import com.vector.bnh.mealsonwheels.ui.DeliveryUiState
import com.vector.bnh.mealsonwheels.util.MlKitStructuredRecognizer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class DeliveryViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<DeliveryUiState>(DeliveryUiState.Idle)
    val uiState: StateFlow<DeliveryUiState> = _uiState

    private val _deliveries = MutableStateFlow<List<DeliveryItem>>(emptyList())
    val deliveries: StateFlow<List<DeliveryItem>> = _deliveries.asStateFlow()

    fun recognizeTextFromImage(context: Context, bitmap: Bitmap) {
        viewModelScope.launch {
            try {
                Log.d("DeliveryViewModel", "Start OCR flow")
                val items = withContext(Dispatchers.Default) {
                    MlKitStructuredRecognizer.recognizeText(bitmap)
                }
                Log.d("DeliveryViewModel", "Parsed ${items.size} delivery items")
                _deliveries.value = items
                _uiState.value = DeliveryUiState.Recognized(items)
            } catch (e: Exception) {
                Log.e("DeliveryViewModel", "OCR failed: ${e.message}", e)
                _uiState.value = DeliveryUiState.Error("OCR failed: ${e.message}")
            }
        }
    }

    fun toggleStatus(index: Int) {
        val updatedList = _deliveries.value.toMutableList().apply {
            val item = this[index]
            this[index] = item.copy(status = if (item.status == DeliveryStatus.Delivered)
                DeliveryStatus.NotDelivered else DeliveryStatus.Delivered)
        }
        _deliveries.value = updatedList
        _uiState.value = DeliveryUiState.Recognized(updatedList)
    }

    fun sortByDistance(context: Context) {
        viewModelScope.launch {
            try {
                val hasPermission = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED

                if (!hasPermission) {
                    _uiState.value = DeliveryUiState.Error("Location permission not granted")
                    return@launch
                }

                val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
                val location = withContext(Dispatchers.IO) {
                    try {
                        fusedLocationClient.lastLocation.await()
                    } catch (e: SecurityException) {
                        Log.e("DeliveryViewModel", "SecurityException: ${e.message}")
                        null
                    }
                }

                if (location == null) {
                    _uiState.value = DeliveryUiState.Error("Unable to get location.")
                    return@launch
                }

                val userLatLng = LatLng(location.latitude, location.longitude)
                val geocoder = Geocoder(context)

                val updated = _deliveries.value.map {
                    val geoResult = withContext(Dispatchers.IO) {
                        try {
                            geocoder.getFromLocationName(it.address, 1)?.firstOrNull()
                        } catch (e: Exception) {
                            null
                        }
                    }
                    val latLng = geoResult?.let { LatLng(it.latitude, it.longitude) }
                    it.copy(latLng = latLng)
                }

                val sorted = updated.sortedBy {
                    it.latLng?.let { loc ->
                        val dLat = loc.latitude - userLatLng.latitude
                        val dLng = loc.longitude - userLatLng.longitude
                        dLat * dLat + dLng * dLng
                    } ?: Double.MAX_VALUE
                }

                _deliveries.value = sorted
                _uiState.value = DeliveryUiState.Recognized(sorted)

            } catch (e: Exception) {
                Log.e("DeliveryViewModel", "Error sorting by distance: ${e.message}", e)
                _uiState.value = DeliveryUiState.Error("Failed to sort by distance.")
            }
        }
    }
}