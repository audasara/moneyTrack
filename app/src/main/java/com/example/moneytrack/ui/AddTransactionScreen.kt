//AddTransactionScreen
package com.example.moneytrack.ui

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.moneytrack.model.Category
import com.example.moneytrack.model.TransactionType
import com.example.moneytrack.viewmodel.CategoryViewModel
import com.example.moneytrack.viewmodel.TransactionViewModel
import com.example.myfinance.CatFace
import com.example.myfinance.CatMood
import com.example.myfinance.CatSays
import com.example.myfinance.ClassicGradientButton
import com.example.myfinance.HomeTeaGreen
import com.example.myfinance.Matcha
import com.example.myfinance.CuteBackground
import com.example.myfinance.CuteScreenHeader
import com.example.myfinance.GlassCard
import com.example.myfinance.HomeCyclamen
import com.example.myfinance.HomeDarkText
import com.example.myfinance.HomeDeepBerry
import com.example.myfinance.HomeFairyTale
import com.example.myfinance.HomeSubText
import com.example.myfinance.HomeViridian
import com.example.myfinance.categoryEmoji
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** ข้อมูลรายการที่เพิ่งบันทึก ใช้แสดงในป๊อปอัปแจ้งเตือน */
private data class SavedInfo(
    val type: String,
    val amount: Double,
    val category: String
)

@Composable
fun AddTransactionScreen(
    onBack: () -> Unit = {},
    onSaved: () -> Unit = {},
    onOpenCategories: () -> Unit = {}
) {
    val context = LocalContext.current

    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: "guest"
    val transactionVm: TransactionViewModel = viewModel(key = "tx_$uid")
    val categoryVm: CategoryViewModel = viewModel(key = "cat_$uid")

    var savedInfo by remember { mutableStateOf<SavedInfo?>(null) }

    // ขอสิทธิ์แจ้งเตือน (Android 13+) ตอนบันทึกครั้งแรก
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    AddTransactionContent(
        categories = categoryVm.categories,
        onBack = onBack,
        onOpenCategories = onOpenCategories,
        onSave = { type, amount, category, description, date ->
            if (NotificationHelper.needsRuntimePermission() &&
                !NotificationHelper.hasPermission(context)
            ) {
                permissionLauncher.launch(NotificationHelper.PERMISSION)
            }

            transactionVm.add(type, amount, category, description, date) { ok ->
                if (ok) {
                    val isIncome = type == TransactionType.INCOME
                    NotificationHelper.notifySaved(
                        context = context,
                        title = "บันทึกรายการแล้ว ♡",
                        text = (if (isIncome) "รายรับ " else "รายจ่าย ") +
                                formatBaht(amount) + " • " + category
                    )
                    savedInfo = SavedInfo(type, amount, category)
                } else {
                    Toast.makeText(context, "บันทึกไม่สำเร็จ", Toast.LENGTH_SHORT).show()
                }
            }
        }
    )

    savedInfo?.let { info ->
        SaveSuccessDialog(
            info = info,
            onDismiss = {
                savedInfo = null
                onSaved()
            }
        )
    }
}

@Composable
fun AddTransactionContent(
    categories: List<Category>,
    onSave: (type: String, amount: Double, category: String, description: String, date: String) -> Unit,
    onBack: () -> Unit = {},
    onOpenCategories: () -> Unit = {}
) {
    var type by remember { mutableStateOf(TransactionType.EXPENSE) }
    var amount by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(todayString()) }
    var expanded by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    // เลือกหมวดแรกให้อัตโนมัติเมื่อโหลดรายการหมวดเสร็จ
    LaunchedEffect(categories) {
        if (category.isBlank() && categories.isNotEmpty()) category = categories.first().name
    }

    val isIncome = type == TransactionType.INCOME
    val accent = HomeViridian

    CuteBackground(
        gradient = listOf(Color(0xFFD9EAB8), Color(0xFFF0F6E2), Color(0xFFF7FAEF)),
        sparkleColor = HomeCyclamen.copy(alpha = 0.55f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CuteScreenHeader(
                title = "เพิ่มรายการ",
                subtitle = "บันทึกรายรับ-รายจ่ายของคุณ",
                onBack = onBack
            )

            CatSays(
                message = if (isIncome) "ได้เงินมาแล้ว ยินดีด้วยน้า~ ♡"
                else "จดไว้ก่อน จะได้ไม่ลืมนะ เมี๊ยว~",
                mood = if (isIncome) CatMood.HAPPY else CatMood.WINK
            )

            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // ประเภท: รายรับ / รายจ่าย
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        TypePill(
                            modifier = Modifier.weight(1f),
                            text = "🌱 รายรับ",
                            selected = isIncome,
                            color = HomeViridian
                        ) { type = TransactionType.INCOME }

                        TypePill(
                            modifier = Modifier.weight(1f),
                            text = "🌸 รายจ่าย",
                            selected = !isIncome,
                            color = HomeDeepBerry
                        ) { type = TransactionType.EXPENSE }
                    }

                    OutlinedTextField(
                        value = amount,
                        onValueChange = { amount = it },
                        label = { Text("จำนวนเงิน") },
                        leadingIcon = {
                            Text(
                                "฿",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = accent
                            )
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        shape = RoundedCornerShape(20.dp),
                        colors = fieldColors(accent),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Box {
                        OutlinedTextField(
                            value = category,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("หมวดหมู่") },
                            leadingIcon = {
                                if (category.isNotBlank()) Text(categoryEmoji(category), fontSize = 18.sp)
                            },
                            trailingIcon = {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                            },
                            shape = RoundedCornerShape(20.dp),
                            colors = fieldColors(accent),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Box(Modifier.matchParentSize().clickable { expanded = true })
                        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            categories.forEach { c ->
                                DropdownMenuItem(
                                    text = { Text("${categoryEmoji(c.name)}  ${c.name}") },
                                    onClick = { category = c.name; expanded = false }
                                )
                            }
                        }
                    }

                    TextButton(
                        onClick = onOpenCategories,
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("จัดการหมวดหมู่", color = HomeViridian)
                    }

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("รายละเอียด") },
                        leadingIcon = { Text("📝", fontSize = 18.sp) },
                        shape = RoundedCornerShape(20.dp),
                        colors = fieldColors(accent),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = date,
                        onValueChange = { date = it },
                        label = { Text("วันที่ (วว/ดด/ปปปป)") },
                        leadingIcon = { Text("📅", fontSize = 18.sp) },
                        singleLine = true,
                        shape = RoundedCornerShape(20.dp),
                        colors = fieldColors(accent),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            error?.let {
                Text(it, color = HomeDeepBerry, fontWeight = FontWeight.Medium)
            }

            ClassicGradientButton(
                text = "บันทึก",
                loading = false,
                colors = listOf(Matcha, HomeViridian),
                shadowColor = HomeViridian,
                onClick = {
                    val value = amount.toDoubleOrNull()
                    when {
                        value == null || value <= 0 -> error = "กรอกจำนวนเงินให้ถูกต้อง"
                        category.isBlank() -> error = "เลือกหมวดหมู่"
                        else -> {
                            error = null
                            onSave(type, value, category, description, date)
                            amount = ""
                            description = ""
                        }
                    }
                }
            )

            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun TypePill(
    modifier: Modifier = Modifier,
    text: String,
    selected: Boolean,
    color: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(if (selected) color else Color(0xFFF1F6E4))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 15.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = if (selected) Color.White else HomeSubText
        )
    }
}

/** ป๊อปอัปแจ้งเตือนเมื่อบันทึกสำเร็จ: น้องแมวเด้งขึ้นมา แล้วปิดเองใน ~2 วินาที */
@Composable
private fun SaveSuccessDialog(info: SavedInfo, onDismiss: () -> Unit) {
    var shown by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (shown) 1f else 0.6f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "pop"
    )

    LaunchedEffect(Unit) {
        shown = true
        delay(2200)
        onDismiss()
    }

    val isIncome = info.type == TransactionType.INCOME

    Dialog(onDismissRequest = onDismiss) {
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .scale(scale),
            shape = RoundedCornerShape(30.dp),
            elevation = 14.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CatFace(size = 100.dp, mood = CatMood.WINK)

                Spacer(Modifier.height(8.dp))

                Text(
                    text = "บันทึกเรียบร้อย! ♡",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = HomeViridian
                )

                Spacer(Modifier.height(6.dp))

                Text(
                    text = (if (isIncome) "🌱 รายรับ " else "🌸 รายจ่าย ") + formatBaht(info.amount),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isIncome) HomeViridian else HomeCyclamen
                )

                Text(
                    text = "หมวด ${categoryEmoji(info.category)} ${info.category}",
                    fontSize = 13.sp,
                    color = HomeSubText
                )

                Spacer(Modifier.height(16.dp))

                ClassicGradientButton(
                    text = "ตกลง",
                    loading = false,
                    colors = listOf(Matcha, HomeViridian),
                    shadowColor = HomeViridian,
                    onClick = onDismiss
                )
            }
        }
    }
}

@Composable
private fun fieldColors(accent: Color): TextFieldColors = OutlinedTextFieldDefaults.colors(
    focusedTextColor = HomeDarkText,
    unfocusedTextColor = HomeDarkText,
    focusedBorderColor = accent,
    unfocusedBorderColor = HomeTeaGreen,
    focusedLabelColor = accent,
    cursorColor = accent,
    focusedContainerColor = Color.White,
    unfocusedContainerColor = Color(0xFFF7FAEF)
)


private fun todayString(): String =
    SimpleDateFormat("dd/MM/yyyy", Locale.US).format(Date())

@Preview(showBackground = true)
@Composable
private fun AddTransactionPreview() {
    MaterialTheme {
        AddTransactionContent(
            categories = listOf(Category("1", "อาหาร"), Category("2", "เดินทาง")),
            onSave = { _, _, _, _, _ -> }
        )
    }
}