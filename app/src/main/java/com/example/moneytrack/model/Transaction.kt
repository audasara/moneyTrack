package com.example.moneytrack.model

data class Transaction(
    val id: String = "",
    val type: String = "",
    val amount: Double = 0.0,
    val category: String = "",
    val description: String = "",
    val date: String = "",
    val createdAt: Long = 0L
)

object TransactionType {
    const val INCOME = "income"
    const val EXPENSE = "expense"
}