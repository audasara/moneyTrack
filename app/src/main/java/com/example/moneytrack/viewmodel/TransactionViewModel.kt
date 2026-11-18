package com.example.moneytrack.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.moneytrack.model.Transaction
import com.example.moneytrack.model.TransactionType
import com.example.moneytrack.repository.TransactionRepository
import com.google.firebase.firestore.ListenerRegistration
import java.text.SimpleDateFormat
import java.util.Locale

class TransactionViewModel : ViewModel() {
    private val repo = TransactionRepository()
    private var listener: ListenerRegistration? = null
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.US).apply { isLenient = false }

    var transactions by mutableStateOf<List<Transaction>>(emptyList())
        private set

    // false = ยังไม่ได้ข้อมูลชุดแรกจาก Firestore กันหน้า Home โชว์ "ยังไม่มีรายการ" วูบเดียวก่อนข้อมูลมา
    var loaded by mutableStateOf(false)
        private set

    val totalIncome: Double
        get() = transactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }

    val totalExpense: Double
        get() = transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }

    val balance: Double
        get() = totalIncome - totalExpense

    /** เรียงใหม่สุดก่อน: ตามวันที่ แล้วตามเวลาที่บันทึก (วันที่อ่านไม่ออกจะไปอยู่ท้ายสุด) */
    val sortedTransactions: List<Transaction>
        get() = transactions.sortedWith(
            compareByDescending<Transaction> { dateValue(it.date) }
                .thenByDescending { it.createdAt }
        )

    val recent: List<Transaction>
        get() = sortedTransactions.take(5)

    init {
        listener = repo.listen {
            transactions = it
            loaded = true
        }
    }

    fun add(
        type: String,
        amount: Double,
        category: String,
        description: String,
        date: String,
        onDone: (Boolean) -> Unit = {}
    ) {
        repo.add(
            Transaction(
                type = type,
                amount = amount,
                category = category,
                description = description.trim(),
                date = date
            ),
            onDone
        )
    }

    fun update(t: Transaction) = repo.update(t)
    fun delete(id: String) = repo.delete(id)

    private fun dateValue(s: String): Long = try {
        dateFormat.parse(s)?.time ?: 0L
    } catch (e: Exception) {
        0L
    }

    override fun onCleared() {
        listener?.remove()
    }
}