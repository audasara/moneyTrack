package com.example.moneytrack.model

object DefaultCategories {
    val list: List<Category> = listOf(
        "อาหาร", "เดินทาง", "ช้อปปิ้ง", "การเรียน", "บันเทิง", "เงินเดือน", "อื่น ๆ"
    ).mapIndexed { i, name -> Category(id = "default_$i", name = name) }
}