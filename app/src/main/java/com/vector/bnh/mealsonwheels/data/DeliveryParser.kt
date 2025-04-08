package com.vector.bnh.mealsonwheels.data

import android.util.Log
import com.vector.bnh.mealsonwheels.model.DeliveryItem

object DeliveryParser {
    fun parse(text: String): List<DeliveryItem> {
        val result = mutableListOf<DeliveryItem>()
        val lines = text.lines().map { it.trim() }.filter { it.isNotEmpty() }

        var name: String? = null
        var address = ""
        var phone: String? = null
        var meals = mutableListOf<String>()
        var note: String? = null

        fun commitIfValid() {
            if (name != null && address.isNotEmpty()) {
                result.add(
                    DeliveryItem(
                        name = name!!,
                        address = address.trim(),
                        phone = phone,
                        mealContent = meals.joinToString("\n"),
                        specialNote = note
                    )
                )
                Log.d("DeliveryParser", "Parsed item: $name, $address")
            }
            name = null
            address = ""
            phone = null
            meals.clear()
            note = null
        }

        for (line in lines) {
            // 新 entry 的起点
            if (Regex("^[A-Z][a-z]+,\\s.+\\(\\d{3,4}\\)").matches(line)) {
                commitIfValid()
                name = line.substringBefore("(").trim()
            }

            // 电话
            else if (Regex("\\d{3}-\\d{3}-\\d{4}").containsMatchIn(line)) {
                phone = Regex("\\d{3}-\\d{3}-\\d{4}").find(line)?.value
            }

            // 地址开头
            else if (Regex("^\\d{3,5}\\s.+(St|Ave|Dr|Road|Rd|Lane|Ln|Court|Ct)", RegexOption.IGNORE_CASE).containsMatchIn(line)) {
                address = line
            }

            // 地址结尾（城市 + 邮编）
            else if (Regex("Burnaby.*V5\\w", RegexOption.IGNORE_CASE).matches(line)) {
                address += "\n$line"
            }

            // 餐品
            else if (line.contains("Hot", ignoreCase = true)) {
                meals.add(line)
            }

            // 指令
            else if (line.startsWith("Driver Instructions", ignoreCase = true)) {
                note = line.substringAfter(":").trim()
            }
        }

        commitIfValid()
        Log.d("DeliveryParser", "Parsed ${result.size} delivery items")
        return result
    }
}
