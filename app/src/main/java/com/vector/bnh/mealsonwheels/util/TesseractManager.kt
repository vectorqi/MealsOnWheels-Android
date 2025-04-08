package com.vector.bnh.mealsonwheels.util

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import android.widget.Toast
import com.googlecode.tesseract.android.TessBaseAPI
import java.io.File
import java.io.FileOutputStream

object TesseractManager {
    private const val TAG = "TesseractManager"
    private const val LANG = "eng"
    private var tessBaseAPI: TessBaseAPI? = null

    fun initIfNeeded(context: Context): Boolean {
        return try {
            val tessDataPath = File(context.filesDir, "tesseract/tessdata")
            if (!tessDataPath.exists()) tessDataPath.mkdirs()

            val trainedDataFile = File(tessDataPath, "$LANG.traineddata")
            if (!trainedDataFile.exists()) {
                context.assets.open("tessdata/$LANG.traineddata").use { input ->
                    FileOutputStream(trainedDataFile).use { output ->
                        input.copyTo(output)
                    }
                }
            }

            val dataPath = File(context.filesDir, "tesseract").absolutePath
            tessBaseAPI = TessBaseAPI()
            tessBaseAPI?.init(dataPath, LANG)
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize OCR: ${e.message}")
            false
        }
    }

    fun recognizeText(context: Context, bitmap: Bitmap): String {
        if (tessBaseAPI == null && !initIfNeeded(context)) {
            Log.e(TAG, "Tesseract not initialized")
            Toast.makeText(context, "OCR 初始化失败", Toast.LENGTH_SHORT).show()
            return ""
        }

        return try {
            Log.d(TAG, "Starting OCR recognition")
            tessBaseAPI?.setImage(bitmap)
            val result = tessBaseAPI?.utF8Text ?: ""
            Log.d(TAG, "OCR result:\n$result")
            result
        } catch (e: Exception) {
            Log.e(TAG, "OCR failed: ${e.message}")
            Toast.makeText(context, "OCR 识别失败", Toast.LENGTH_SHORT).show()
            ""
        }
    }


    fun release() {
        tessBaseAPI?.end()
        tessBaseAPI = null
    }
}
