package com.example.moneytrack.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.moneytrack.model.Category
import com.example.moneytrack.model.DefaultCategories
import com.example.moneytrack.repository.CategoryRepository
import com.google.firebase.firestore.ListenerRegistration

class CategoryViewModel : ViewModel() {
    private val repo = CategoryRepository()
    private var listener: ListenerRegistration? = null

    // หมวดของระบบ อยู่ในโค้ด แก้หรือลบไม่ได้
    val defaultCategories: List<Category> = DefaultCategories.list

    // หมวดที่ผู้ใช้เพิ่มเอง อยู่ใน Firestore
    var customCategories by mutableStateOf<List<Category>>(emptyList())
        private set

    // รายการรวม ใช้ใน dropdown (ระบบก่อน แล้วตามด้วยของผู้ใช้)
    val categories: List<Category>
        get() = defaultCategories + customCategories

    init {
        listener = repo.listen { list ->
            // กรองเอกสาร default_* เก่าที่เคยถูกสร้างไว้ใน Firestore ออก กันซ้ำ
            customCategories = list.filterNot { it.id.startsWith("default_") }
        }
    }

    fun add(name: String) {
        if (name.isNotBlank()) repo.add(name.trim())
    }

    fun update(id: String, name: String) {
        if (!id.startsWith("default_") && name.isNotBlank()) repo.update(id, name.trim())
    }

    fun delete(id: String) {
        if (!id.startsWith("default_")) repo.delete(id)
    }

    override fun onCleared() {
        listener?.remove()
    }
}