package com.vector.bnh.mealsonwheels.util

import android.content.Context
import java.io.File
import java.io.FileOutputStream

object FileUtil {
    fun prepareLanguageData(context: Context) {
        val lang = "chi_sim.traineddata"
        val tessDataPath = context.filesDir.absolutePath + "/tesseract/tessdata/"
        val file = File(tessDataPath + lang)
        if (!file.exists()) {
            file.parentFile?.mkdirs()
            context.assets.open("tessdata/$lang").use { input ->
                FileOutputStream(file).use { output ->
                    input.copyTo(output)
                }
            }
        }
    }
}
