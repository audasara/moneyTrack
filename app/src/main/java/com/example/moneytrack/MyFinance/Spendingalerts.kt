//SpendingAlerts
package com.example.myfinance

import android.content.Context
import com.example.moneytrack.model.Transaction
import com.example.moneytrack.model.TransactionType
import com.example.moneytrack.ui.formatBaht
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** การแจ้งเตือนหนึ่งรายการ (แสดงในไดอะล็อกกระดิ่ง + ส่งเป็นแจ้งเตือนระบบ) */
data class AppAlert(
    val id: String,        // daily / low / negative
    val emoji: String,
    val title: String,
    val message: String
)

fun todayKey(): String =
    SimpleDateFormat("dd/MM/yyyy", Locale.US).format(Date())

/**
 * คำนวณรายการแจ้งเตือนจากข้อมูลปัจจุบัน
 * - dailyLimit: เตือนเมื่อรายจ่ายของ "วันนี้" รวมถึงยอดนี้ (0 = ปิด)
 * - lowBalance: เตือนเมื่อเงินคงเหลือ <= ยอดนี้ (0 = ปิด)
 */
fun buildAlerts(
    transactions: List<Transaction>,
    balance: Double,
    dailyLimit: Double,
    lowBalance: Double
): List<AppAlert> {
    if (transactions.isEmpty()) return emptyList()

    val today = todayKey()
    val todayExpense = transactions
        .filter { it.type == TransactionType.EXPENSE && it.date == today }
        .sumOf { it.amount }

    val list = mutableListOf<AppAlert>()

    if (dailyLimit > 0 && todayExpense >= dailyLimit) {
        list += AppAlert(
            id = "daily",
            emoji = "💸",
            title = "วันนี้ใช้เงินไปแล้ว ${formatBaht(todayExpense)}",
            message = "ถึงวงเงินรายวันที่ตั้งไว้ (${formatBaht(dailyLimit)}) แล้วนะ ระวังหน่อยน้า"
        )
    }

    if (balance < 0) {
        list += AppAlert(
            id = "negative",
            emoji = "🚨",
            title = "ยอดคงเหลือติดลบ",
            message = "ตอนนี้เหลือ ${formatBaht(balance)} ใช้จ่ายมากกว่ารายรับแล้ว"
        )
    } else if (lowBalance > 0 && balance <= lowBalance) {
        list += AppAlert(
            id = "low",
            emoji = "🪙",
            title = "เงินเหลือน้อยแล้ว",
            message = "คงเหลือ ${formatBaht(balance)} ต่ำกว่าที่ตั้งไว้ (${formatBaht(lowBalance)}) ลองเก็บไว้หน่อยนะ"
        )
    }

    return list
}

/** เก็บค่าตั้งค่า + จำว่าวันนี้เตือนอะไรไปแล้ว (กันเด้งซ้ำ) แยกตามผู้ใช้ */
class AlertPrefs(context: Context, uid: String) {
    private val sp = context.getSharedPreferences("alert_prefs_$uid", Context.MODE_PRIVATE)

    var dailyLimit: Double
        get() = sp.getFloat("daily_limit", 100f).toDouble()
        set(v) = sp.edit().putFloat("daily_limit", v.toFloat()).apply()

    var lowBalance: Double
        get() = sp.getFloat("low_balance", 500f).toDouble()
        set(v) = sp.edit().putFloat("low_balance", v.toFloat()).apply()

    fun wasNotified(key: String): Boolean =
        sp.getStringSet("notified", emptySet())?.contains(key) == true

    fun markNotified(key: String, today: String) {
        val current = sp.getStringSet("notified", emptySet()) ?: emptySet()
        // เก็บเฉพาะของวันนี้ ไม่ให้ข้อมูลบวมไปเรื่อยๆ
        val set = current.filter { it.endsWith("_$today") }.toMutableSet()
        set.add(key)
        sp.edit().putStringSet("notified", set).apply()
    }

    fun clearNotified() = sp.edit().remove("notified").apply()
}