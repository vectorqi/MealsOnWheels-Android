package com.vector.bnh.mealsonwheels.model

enum class DeliveryStatus {
    NotDelivered,
    Delivered;

    fun toggle(): DeliveryStatus {
        return if (this == Delivered) NotDelivered else Delivered
    }
}
