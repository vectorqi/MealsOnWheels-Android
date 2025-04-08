package com.vector.bnh.mealsonwheels.ui

import com.vector.bnh.mealsonwheels.model.DeliveryItem

sealed class DeliveryUiState {
    object Idle : DeliveryUiState()
    object Recognizing : DeliveryUiState()
    data class Recognized(val deliveries: List<DeliveryItem>) : DeliveryUiState()
    data class Error(val message: String) : DeliveryUiState()
}
