//CategoryScreen
package com.example.moneytrack.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.moneytrack.model.Category
import com.example.moneytrack.model.DefaultCategories
import com.example.moneytrack.viewmodel.CategoryViewModel
import com.example.myfinance.HomeCardWhite
import com.example.myfinance.HomeCreamBackground
import com.example.myfinance.HomeDarkText
import com.example.myfinance.HomeDeepBerry
import com.example.myfinance.HomeSubText
import com.example.myfinance.HomeViridian
import com.google.firebase.auth.FirebaseAuth

@Composable
fun CategoryScreen(onBack: () -> Unit = {}) {
    // key ต้องตรงกับที่ AddTransactionScreen ใช้ จะได้ ViewModel ตัวเดียวกัน
    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: "guest"
    val vm: CategoryViewModel = viewModel(key = "cat_$uid")

    CategoryContent(
        defaultCategories = vm.defaultCategories,
        customCategories = vm.customCategories,
        onAdd = vm::add,
        onEdit = vm::update,
        onDelete = vm::delete,
        onBack = onBack
    )
}

@Composable
fun CategoryContent(
    defaultCategories: List<Category>,
    customCategories: List<Category>,
    onAdd: (String) -> Unit,
    onEdit: (String, String) -> Unit,
    onDelete: (String) -> Unit,
    onBack: () -> Unit = {}
) {
    var showAdd by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<Category?>(null) }
    var deleting by remember { mutableStateOf<Category?>(null) }

    Scaffold(
        containerColor = HomeCreamBackground,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAdd = true },
                containerColor = HomeViridian,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "เพิ่มหมวดหมู่")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 88.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                ScreenHeader(
                    title = "หมวดหมู่",
                    subtitle = "จัดการหมวดหมู่รายรับ-รายจ่าย",
                    onBack = onBack
                )
                Spacer(Modifier.height(8.dp))
            }

            // ส่วนที่ 1: หมวดของระบบ
            item { SectionTitle("หมวดหมู่ของระบบ", "แก้ไขหรือลบไม่ได้") }
            items(defaultCategories, key = { it.id }) { c ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(19.dp),
                    colors = CardDefaults.cardColors(containerColor = HomeCardWhite),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = c.name,
                            color = HomeDarkText,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = "หมวดหมู่ของระบบ",
                            tint = HomeSubText.copy(alpha = 0.6f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // ส่วนที่ 2: หมวดที่ผู้ใช้เพิ่มเอง
            item {
                Spacer(Modifier.height(8.dp))
                SectionTitle("หมวดหมู่ของฉัน", "เพิ่ม แก้ไข และลบได้")
            }
            if (customCategories.isEmpty()) {
                item {
                    Text(
                        text = "ยังไม่มีหมวดหมู่ที่เพิ่มเอง กดปุ่ม + เพื่อเพิ่ม",
                        fontSize = 13.sp,
                        color = HomeSubText
                    )
                }
            } else {
                items(customCategories, key = { it.id }) { c ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(19.dp),
                        colors = CardDefaults.cardColors(containerColor = HomeCardWhite),
                        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(start = 16.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = c.name,
                                color = HomeDarkText,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = { editing = c }) {
                                Icon(
                                    Icons.Default.Edit,
                                    contentDescription = "แก้ไข",
                                    tint = HomeViridian
                                )
                            }
                            IconButton(onClick = { deleting = c }) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "ลบ",
                                    tint = HomeDeepBerry
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAdd) {
        CategoryNameDialog(
            title = "เพิ่มหมวดหมู่",
            initial = "",
            onDismiss = { showAdd = false },
            onConfirm = {
                onAdd(it)
                showAdd = false
            }
        )
    }

    editing?.let { c ->
        CategoryNameDialog(
            title = "แก้ไขหมวดหมู่",
            initial = c.name,
            onDismiss = { editing = null },
            onConfirm = {
                onEdit(c.id, it)
                editing = null
            }
        )
    }

    deleting?.let { c ->
        AlertDialog(
            onDismissRequest = { deleting = null },
            containerColor = HomeCardWhite,
            title = { Text("ลบหมวดหมู่", color = HomeDarkText) },
            text = { Text("ต้องการลบ \"${c.name}\" ใช่ไหม", color = HomeDarkText) },
            confirmButton = {
                TextButton(onClick = { onDelete(c.id); deleting = null }) {
                    Text("ลบ", color = HomeDeepBerry)
                }
            },
            dismissButton = {
                TextButton(onClick = { deleting = null }) {
                    Text("ยกเลิก", color = HomeViridian)
                }
            }
        )
    }
}

@Composable
private fun SectionTitle(title: String, subtitle: String) {
    Column(Modifier.padding(top = 4.dp)) {
        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = HomeDarkText
        )
        Text(
            text = subtitle,
            fontSize = 12.sp,
            color = HomeSubText
        )
    }
}

@Composable
private fun CategoryNameDialog(
    title: String,
    initial: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var text by remember { mutableStateOf(initial) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = HomeCardWhite,
        title = { Text(title, color = HomeDarkText) },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text("ชื่อหมวดหมู่") },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = HomeDarkText,
                    unfocusedTextColor = HomeDarkText,
                    focusedBorderColor = HomeViridian,
                    focusedLabelColor = HomeViridian,
                    cursorColor = HomeViridian
                )
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(text) },
                enabled = text.isNotBlank()
            ) {
                Text("บันทึก", color = if (text.isNotBlank()) HomeViridian else Color.Gray)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("ยกเลิก", color = HomeDeepBerry)
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
private fun CategoryPreview() {
    MaterialTheme {
        CategoryContent(
            defaultCategories = DefaultCategories.list,
            customCategories = listOf(Category("a", "food")),
            onAdd = {},
            onEdit = { _, _ -> },
            onDelete = {}
        )
    }
}