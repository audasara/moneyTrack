//TransactionRepository
package com.example.moneytrack.repository

import com.example.moneytrack.model.Transaction
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class TransactionRepository {
    private val db = FirebaseFirestore.getInstance()

    private fun col() = db.collection("users")
        .document(FirebaseAuth.getInstance().currentUser?.uid ?: "guest")
        .collection("transactions")

    fun listen(onResult: (List<Transaction>) -> Unit): ListenerRegistration =
        col().addSnapshotListener { snap, _ ->
            val list = snap?.documents?.mapNotNull { d ->
                d.toObject(Transaction::class.java)?.copy(id = d.id)
            } ?: emptyList()
            onResult(list)
        }

    fun add(t: Transaction, onDone: (Boolean) -> Unit) {
        val doc = col().document()
        doc.set(t.copy(id = doc.id, createdAt = System.currentTimeMillis()))
            .addOnSuccessListener { onDone(true) }
            .addOnFailureListener { onDone(false) }
    }

    fun update(t: Transaction) {
        col().document(t.id).set(t)
    }

    fun delete(id: String) {
        col().document(id).delete()
    }
}