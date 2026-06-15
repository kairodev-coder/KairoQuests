package dev.kairo.kairoquests.util

import java.text.NumberFormat
import java.util.Locale

object NumberUtil {
    private val format = NumberFormat.getIntegerInstance(Locale.US)

    fun whole(value: Number): String = format.format(value)

    fun percent(progress: Int, required: Int): String {
        if (required <= 0) return "0%"
        return "${((progress.coerceAtMost(required).toDouble() / required) * 100).toInt()}%"
    }
}
