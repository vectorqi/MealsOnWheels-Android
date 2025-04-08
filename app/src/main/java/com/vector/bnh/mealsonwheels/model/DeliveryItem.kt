package com.vector.bnh.mealsonwheels.model

import com.google.android.gms.maps.model.LatLng

data class DeliveryItem(
    val name: String,
    val address: String,
    val phone: String?,
    val mealContent: String,
    val specialNote: String?,
    var status: DeliveryStatus = DeliveryStatus.NotDelivered,
    var latLng: LatLng? = null
)
