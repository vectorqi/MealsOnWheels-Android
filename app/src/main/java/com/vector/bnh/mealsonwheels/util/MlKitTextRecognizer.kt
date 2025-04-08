package com.vector.bnh.mealsonwheels.util

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.tasks.await

object MlKitTextRecognizer {
    suspend fun recognizeText(context: Context, bitmap: Bitmap): String {
        val image = InputImage.fromBitmap(bitmap, 0)
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

        return try {
            val result = recognizer.process(image).await()
            val blocks = result.textBlocks
            val fullText = blocks.joinToString("\n") { it.text }

            Log.d("MlKitOCR", "Detected text:\n$fullText")
            fullText
        } catch (e: Exception) {
            Log.e("MlKitOCR", "Recognition failed: ${e.message}")
            ""
        }
    }
}
