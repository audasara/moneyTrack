package com.example.moneytrack.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.example.moneytrack.R

// ฟอนต์ Mitr จากโฟลเดอร์ res/font จับคู่ไฟล์กับน้ำหนักตัวอักษร
val AppFont = FontFamily(
    Font(R.font.mitr_regular, FontWeight.Normal),
    Font(R.font.mitr_medium, FontWeight.Medium),
    Font(R.font.mitr_semibold, FontWeight.SemiBold),
    Font(R.font.mitr_bold, FontWeight.Bold)
)

// ชุดสไตล์ตัวอักษรมาตรฐานของ Material 3 (ยังเป็นฟอนต์เดิม) ใช้เป็นต้นแบบ
private val base = Typography()

// เปลี่ยนเฉพาะตระกูลฟอนต์ ขนาดกับน้ำหนักของแต่ละสไตล์ยังเป็นค่าเดิม
val Typography = Typography(
    displayLarge = base.displayLarge.copy(fontFamily = AppFont),
    displayMedium = base.displayMedium.copy(fontFamily = AppFont),
    displaySmall = base.displaySmall.copy(fontFamily = AppFont),
    headlineLarge = base.headlineLarge.copy(fontFamily = AppFont),
    headlineMedium = base.headlineMedium.copy(fontFamily = AppFont),
    headlineSmall = base.headlineSmall.copy(fontFamily = AppFont),
    titleLarge = base.titleLarge.copy(fontFamily = AppFont),
    titleMedium = base.titleMedium.copy(fontFamily = AppFont),
    titleSmall = base.titleSmall.copy(fontFamily = AppFont),
    bodyLarge = base.bodyLarge.copy(fontFamily = AppFont),
    bodyMedium = base.bodyMedium.copy(fontFamily = AppFont),
    bodySmall = base.bodySmall.copy(fontFamily = AppFont),
    labelLarge = base.labelLarge.copy(fontFamily = AppFont),
    labelMedium = base.labelMedium.copy(fontFamily = AppFont),
    labelSmall = base.labelSmall.copy(fontFamily = AppFont)
)

// ชื่อเดิมที่ MainActivity.kt เรียกใช้ ชี้ไปที่ตัวเดียวกัน จะได้ไม่ต้องแก้ไฟล์ที่ทุกคนใช้ร่วมกัน
val AppTypography = Typography