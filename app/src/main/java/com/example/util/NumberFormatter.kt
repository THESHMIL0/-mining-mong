package com.example.util

import java.util.Locale

object NumberFormatter {

    private val SUFFIXES = arrayOf(
        "", "K", "M", "B", "T", "Aa", "Ab", "Ac", "Ad", "Ae", "Af", "Ag",
        "Ah", "Ai", "Aj", "Ak", "Al", "Am", "An", "Ao", "Ap", "Aq", "Ar", "As", "At"
    )

    fun format(value: Double): String {
        if (value.isNaN() || value.isInfinite()) return "0"
        if (value < 0) return "-" + format(-value)
        if (value < 1000) {
            return if (value == value.toLong().toDouble()) {
                value.toLong().toString()
            } else {
                String.format(Locale.US, "%.1f", value)
            }
        }

        var base = value
        var suffixIndex = 0

        while (base >= 1000.0 && suffixIndex < SUFFIXES.size - 1) {
            base /= 1000.0
            suffixIndex++
        }

        return if (base >= 100) {
            String.format(Locale.US, "%.0f%s", base, SUFFIXES[suffixIndex])
        } else if (base >= 10) {
            String.format(Locale.US, "%.1f%s", base, SUFFIXES[suffixIndex])
        } else {
            String.format(Locale.US, "%.2f%s", base, SUFFIXES[suffixIndex])
        }
    }

    fun formatCompact(value: Double): String {
        return format(value)
    }
}
