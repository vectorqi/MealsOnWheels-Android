package com.vector.bnh.mealsonwheels.util

import android.graphics.Bitmap
import android.util.Log
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.vector.bnh.mealsonwheels.model.DeliveryItem
import com.vector.bnh.mealsonwheels.model.DeliveryStatus
import kotlinx.coroutines.tasks.await

object MlKitStructuredRecognizer {

    suspend fun recognizeText(bitmap: Bitmap): List<DeliveryItem> {
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        val image = InputImage.fromBitmap(bitmap, 0)
        val result = recognizer.process(image).await()

        val blocks = result.textBlocks.map { it.text }
        val deliveryItems = mutableListOf<DeliveryItem>()

        var currentName: String? = null
        var currentAddress: String? = null
        var currentPhone: String? = null
        var mealContent: String = ""
        var specialNote: String = ""

        for (block in blocks) {
            Log.d("MlKitStructuredRecognizer", "Block: $block")
            val line = block.trim()

            when {
                line.contains("Delivered", ignoreCase = true) -> {
                    if (!currentName.isNullOrBlank() && !currentAddress.isNullOrBlank()) {
                        deliveryItems.add(
                            DeliveryItem(
                                name = currentName!!,
                                address = currentAddress!!,
                                phone = currentPhone,
                                mealContent = mealContent.trim(),
                                specialNote = specialNote.trim(),
                                status = DeliveryStatus.NotDelivered,
                                latLng = null
                            )
                        )
                    }

                    // reset for next block
                    currentName = null
                    currentAddress = null
                    currentPhone = null
                    mealContent = ""
                    specialNote = ""
                }

                Regex("\\d{3,4} .+ St").containsMatchIn(line) ||
                        Regex("\\d{3,4} .+ Ave").containsMatchIn(line) -> {
                    currentAddress = line
                }

                Regex("\\d{3}-\\d{3}-\\d{4}").containsMatchIn(line) -> {
                    currentPhone = Regex("\\d{3}-\\d{3}-\\d{4}")
                        .find(line)?.value ?: currentPhone
                }

                line.contains("Instructions", true) -> {
                    specialNote += "$line "
                }

                line.contains("Hot", true) || line.contains("Soup", true) -> {
                    mealContent += "$line "
                }

                Regex("[A-Z][a-z]+, [A-Z][a-z]+").containsMatchIn(line) -> {
                    currentName = line
                }
            }
        }

        return deliveryItems
    }
}
