//CategoryRepository
package com.example.moneytrack.repository

import com.example.moneytrack.model.Category
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class CategoryRepository {
    private val db = FirebaseFirestore.getInstance()

    // ยังไม่มี Login ให้ใช้ "guest" ไปก่อน พอรวมโปรเจกต์จะได้ uid จริงของผู้ใช้เอง
    private fun col() = db.collection("users")
        .document(FirebaseAuth.getInstance().currentUser?.uid ?: "guest")
        .collection("categories")

    fun listen(onResult: (List<Category>) -> Unit): ListenerRegistration =
        col().addSnapshotListener { snap, _ ->
            val list = snap?.documents?.mapNotNull { d ->
                d.toObject(Category::class.java)?.copy(id = d.id)
            } ?: emptyList()
            onResult(list)
        }

    fun add(name: String) {
        val doc = col().document()
        doc.set(Category(id = doc.id, name = name))
    }

    fun update(id: String, name: String) {
        col().document(id).update("name", name)
    }

    fun delete(id: String) {
        col().document(id).delete()
    }

    fun seedDefaultsIfEmpty() {
        col().get().addOnSuccessListener { snap ->
            if (snap.isEmpty) {
                val defaults = listOf("อาหาร", "เดินทาง", "ช้อปปิ้ง", "การเรียน", "บันเทิง", "เงินเดือน", "อื่น ๆ")
                defaults.forEachIndexed { i, name ->
                    val id = "default_$i"
                    col().document(id).set(Category(id = id, name = name))
                }
            }
        }
    }
}