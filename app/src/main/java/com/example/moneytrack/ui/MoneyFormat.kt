package com.example.moneytrack.ui

import java.util.Locale
import kotlin.math.abs

/** 4250.0 -> "4,250"   150.5 -> "150.50" */
fun formatMoney(value: Double): String {
    val v = abs(value)
    return if (v % 1.0 == 0.0) String.format(Locale.US, "%,.0f", v)
    else String.format(Locale.US, "%,.2f", v)
}

/** "฿ 4,250" หรือ "-฿ 500" ถ้าติดลบ */
fun formatBaht(value: Double): String =
    (if (value < 0) "-฿ " else "฿ ") + formatMoney(value)