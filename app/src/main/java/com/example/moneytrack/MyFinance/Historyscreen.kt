//History
package com.example.myfinance

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import com.example.moneytrack.model.DefaultCategories
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.moneytrack.model.Transaction
import com.example.moneytrack.model.TransactionType
import com.example.moneytrack.repository.CategoryRepository
import com.example.moneytrack.repository.TransactionRepository
import com.example.moneytrack.ui.formatBaht
import com.example.moneytrack.ui.formatMoney
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private fun historyDateMillis(date: String): Long? {
    return try {
        SimpleDateFormat("dd/MM/yyyy", Locale.US).apply {
            isLenient = false
        }.parse(date)?.time
    } catch (_: Exception) { null }
}

@Composable
fun HistoryScreen(
    onHomeClick: () -> Unit,
    onTransactionClick: () -> Unit,
    onProfileClick: () -> Unit,
    onDashboardClick: () -> Unit = {}          // เพิ่มใหม่
) {
    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: "guest"
    val categoryRepo = remember(uid) { CategoryRepository() }
    val txRepo = remember(uid) { TransactionRepository() }

    var transactions by remember { mutableStateOf<List<Transaction>>(emptyList()) }
    var userCategories by remember { mutableStateOf<List<String>>(emptyList()) }
    var loaded by remember { mutableStateOf(false) }

    DisposableEffect(uid) {
        val r1 = txRepo.listen {
            transactions = it
            loaded = true
        }
        val r2 = categoryRepo.listen { list ->
            userCategories = list.map { it.name }
        }

        onDispose {
            r1.remove()
            r2.remove()
        }
    }

    var typeFilter by remember { mutableIntStateOf(0) }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var searchVisible by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }
    var addCategoryDialog by remember { mutableStateOf(false) }
    var deleteTarget by remember { mutableStateOf<Transaction?>(null) }
    var detailTarget by remember { mutableStateOf<Transaction?>(null) }
    var editTarget by remember { mutableStateOf<Transaction?>(null) }

    var showDateFilter by remember { mutableStateOf(false) }
    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }
    var dateError by remember { mutableStateOf<String?>(null) }

    var sortOrder by remember { mutableIntStateOf(0) }
    var showSortMenu by remember { mutableStateOf(false) }
    var showCategoryMenu by remember { mutableStateOf(false) }

    val allCategories = remember(userCategories) {
        (DefaultCategories.list.map { it.name } + userCategories)
            .filter { it.isNotBlank() }
            .distinct()
    }

    val filtered = remember(
        transactions,
        typeFilter,
        selectedCategory,
        query,
        startDate,
        endDate,
        sortOrder
    ) {
        transactions
            .filter {
                when (typeFilter) {
                    1 -> it.type == TransactionType.INCOME
                    2 -> it.type == TransactionType.EXPENSE
                    else -> true
                }
            }
            .filter {
                selectedCategory == null ||
                        it.category == selectedCategory
            }
            .filter {
                query.isBlank() ||
                        it.description.contains(query, ignoreCase = true) ||
                        it.category.contains(query, ignoreCase = true)
            }
            .filter { t ->
                val itemDate = historyDateMillis(t.date)
                val from = if (startDate.isBlank()) null
                else historyDateMillis(startDate)
                val to = if (endDate.isBlank()) null
                else historyDateMillis(endDate)

                (startDate.isBlank() || (itemDate != null && from != null && itemDate >= from)) &&
                        (endDate.isBlank() || (itemDate != null && to != null && itemDate <= to))
            }
            .sortedWith(
                when (sortOrder) {
                    1 -> compareBy<Transaction> {
                        historyDateMillis(it.date) ?: it.createdAt
                    }
                    2 -> compareByDescending { it.amount }
                    3 -> compareBy { it.amount }
                    else -> compareByDescending<Transaction> {
                        historyDateMillis(it.date) ?: it.createdAt
                    }
                }
            )
    }

    val filteredIncome = filtered
        .filter { it.type == TransactionType.INCOME }
        .sumOf { it.amount }

    val filteredExpense = filtered
        .filter { it.type == TransactionType.EXPENSE }
        .sumOf { it.amount }

    val netAmount = filteredIncome - filteredExpense

    CuteBackground {
        Scaffold(
            containerColor = Color.Transparent,
            bottomBar = {
                BottomNavigationBar(
                    selectedIndex = 2,
                    onHomeClick = onHomeClick,
                    onProfileClick = onProfileClick,
                    onTransactionClick = onTransactionClick,
                    onHistoryClick = {},
                    onDashboardClick = onDashboardClick   // เพิ่มใหม่
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                Spacer(Modifier.height(18.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AppLogo(size = 50.dp)
                    Spacer(Modifier.width(12.dp))

                    Column(Modifier.weight(1f)) {
                        Text(
                            "ประวัติรายการ",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = HomeDarkText
                        )
                        Text(
                            "รายรับ-รายจ่ายทั้งหมดของคุณ",
                            fontSize = 13.sp,
                            color = HomeSubText
                        )
                    }

                    Surface(
                        modifier = Modifier.size(44.dp),
                        shape = CircleShape,
                        color = HomeCardWhite,
                        shadowElevation = 5.dp,
                        onClick = {
                            searchVisible = !searchVisible
                            if (!searchVisible) query = ""
                        }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                if (searchVisible) "✕" else "🔍",
                                fontSize = 18.sp
                            )
                        }
                    }
                }

                if (searchVisible) {
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        placeholder = {
                            Text("ค้นหารายการหรือหมวดหมู่")
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = HomeDarkText,
                            unfocusedTextColor = HomeDarkText,
                            focusedBorderColor = HomeCyclamen,
                            unfocusedBorderColor = BorderSoft,
                            focusedContainerColor = HomeCardWhite,
                            unfocusedContainerColor = HomeCardWhite
                        )
                    )
                }

                Spacer(Modifier.height(14.dp))

                // Summary
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .shadow(10.dp, RoundedCornerShape(24.dp))
                        .clip(RoundedCornerShape(24.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(HomeDeepBerry, HomeCyclamen)
                            )
                        )
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "สรุปรายการที่แสดง (${filtered.size} รายการ)",
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.85f)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                "รายรับ",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                            Text(
                                formatBaht(filteredIncome),
                                fontSize = 17.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }

                        Column(
                            Modifier.weight(1f),
                            horizontalAlignment = Alignment.End
                        ) {
                            Text(
                                "รายจ่าย",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                            Text(
                                formatBaht(filteredExpense),
                                fontSize = 17.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }
                    }

                    HorizontalDivider(
                        color = Color.White.copy(alpha = 0.25f)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "ยอดสุทธิ",
                            fontSize = 13.sp,
                            color = Color.White
                        )
                        Text(
                            (if (netAmount > 0) "+" else "") +
                                    formatBaht(netAmount),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Type filters
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(listOf("ทั้งหมด", "รายรับ", "รายจ่าย").size) { i ->
                        val labels = listOf("ทั้งหมด", "รายรับ", "รายจ่าย")
                        FilterChipItem(
                            text = labels[i],
                            selected = typeFilter == i,
                            selectedColor = HomeDeepBerry
                        ) {
                            typeFilter = i
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                // Category filter dropdown
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                ) {
                    OutlinedButton(
                        onClick = { showCategoryMenu = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = HomeCardWhite,
                            contentColor = HomeDarkText
                        )
                    ) {
                        Text(
                            text = selectedCategory ?: "ทุกหมวดหมู่",
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Start,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Text("⌄", fontSize = 18.sp, color = HomeSubText)
                    }

                    DropdownMenu(
                        expanded = showCategoryMenu,
                        onDismissRequest = { showCategoryMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("ทุกหมวดหมู่") },
                            onClick = {
                                selectedCategory = null
                                showCategoryMenu = false
                            }
                        )

                        allCategories.forEach { cat ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        cat,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                },
                                onClick = {
                                    selectedCategory = cat
                                    showCategoryMenu = false
                                }
                            )
                        }
                    }
                }

                // Date filter and sort
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = {
                            showDateFilter = !showDateFilter
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            if (showDateFilter) "ซ่อนตัวกรองวันที่"
                            else "กรองวันที่"
                        )
                    }

                    Box {
                        OutlinedButton(
                            onClick = { showSortMenu = true }
                        ) {
                            Text(
                                when (sortOrder) {
                                    1 -> "วันที่เก่า"
                                    2 -> "ยอดมาก"
                                    3 -> "ยอดน้อย"
                                    else -> "วันที่ใหม่"
                                }
                            )
                        }

                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false }
                        ) {
                            listOf(
                                "วันที่ใหม่ → เก่า",
                                "วันที่เก่า → ใหม่",
                                "จำนวนเงินมาก → น้อย",
                                "จำนวนเงินน้อย → มาก"
                            ).forEachIndexed { index, label ->
                                DropdownMenuItem(
                                    text = { Text(label) },
                                    onClick = {
                                        sortOrder = index
                                        showSortMenu = false
                                    }
                                )
                            }
                        }
                    }
                }

                if (showDateFilter) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = HomeCardWhite
                        ),
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                "ช่วงวันที่ (วว/ดด/ปปปป)",
                                fontWeight = FontWeight.Medium,
                                color = HomeDarkText
                            )

                            OutlinedTextField(
                                value = startDate,
                                onValueChange = {
                                    startDate = it
                                    dateError = null
                                },
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text("ตั้งแต่วันที่") },
                                placeholder = { Text("01/10/2026") },
                                singleLine = true
                            )

                            OutlinedTextField(
                                value = endDate,
                                onValueChange = {
                                    endDate = it
                                    dateError = null
                                },
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text("ถึงวันที่") },
                                placeholder = { Text("31/10/2026") },
                                singleLine = true
                            )

                            dateError?.let {
                                Text(it, color = HomeDeepBerry, fontSize = 12.sp)
                            }

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                TextButton(
                                    onClick = {
                                        startDate = ""
                                        endDate = ""
                                        dateError = null
                                    }
                                ) {
                                    Text("ล้างวันที่")
                                }

                                Spacer(Modifier.weight(1f))

                                Button(
                                    onClick = {
                                        val from = if (startDate.isBlank()) null
                                        else historyDateMillis(startDate)
                                        val to = if (endDate.isBlank()) null
                                        else historyDateMillis(endDate)

                                        when {
                                            startDate.isNotBlank() && from == null ->
                                                dateError = "วันที่เริ่มต้นไม่ถูกต้อง"
                                            endDate.isNotBlank() && to == null ->
                                                dateError = "วันที่สิ้นสุดไม่ถูกต้อง"
                                            from != null && to != null && from > to ->
                                                dateError = "วันที่เริ่มต้นต้องไม่เกินวันที่สิ้นสุด"
                                            else -> {
                                                dateError = null
                                                showDateFilter = false
                                            }
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = HomeViridian
                                    )
                                ) {
                                    Text("ใช้ตัวกรอง")
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(8.dp))
                }

                // Transaction list
                if (!loaded) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = HomeViridian)
                    }
                } else if (filtered.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        GlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            elevation = 6.dp
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                CatFace(size = 96.dp, mood = CatMood.SLEEPY)
                                Spacer(Modifier.height(10.dp))
                                Text(
                                    "ไม่พบรายการ",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = HomeDarkText
                                )
                                Text(
                                    "ลองเปลี่ยนตัวกรองหรือค้นหาใหม่",
                                    fontSize = 13.sp,
                                    color = HomeSubText,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(
                            start = 20.dp,
                            end = 20.dp,
                            bottom = 16.dp
                        )
                    ) {
                        filtered.forEachIndexed { index, t ->
                            if (sortOrder < 2 &&
                                (index == 0 || filtered[index - 1].date != t.date)
                            ) {
                                item(key = "date_${t.date}_$index") {
                                    Text(
                                        text = t.date.ifBlank { "ไม่ระบุวันที่" },
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = HomeSubText,
                                        modifier = Modifier.padding(
                                            top = 12.dp,
                                            bottom = 4.dp
                                        )
                                    )
                                }
                            }

                            item(key = "transaction_${t.id}") {
                                HistoryItem(
                                    t = t,
                                    onClick = { detailTarget = t },
                                    onDelete = { deleteTarget = t }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Add category dialog
    if (addCategoryDialog) {
        var newName by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { addCategoryDialog = false },
            title = { Text("เพิ่มหมวดหมู่ใหม่") },
            text = {
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text("ชื่อหมวดหมู่") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val n = newName.trim()
                        if (n.isNotEmpty()) {
                            categoryRepo.add(n)
                            selectedCategory = n
                        }
                        addCategoryDialog = false
                    }
                ) {
                    Text("เพิ่ม")
                }
            },
            dismissButton = {
                TextButton(onClick = { addCategoryDialog = false }) {
                    Text("ยกเลิก")
                }
            }
        )
    }

    // Transaction detail
    detailTarget?.let { t ->
        AlertDialog(
            onDismissRequest = { detailTarget = null },
            title = {
                Text("รายละเอียดรายการ", color = HomeDarkText)
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    DetailRow("รายการ", t.description.ifBlank { t.category })
                    DetailRow(
                        "ประเภท",
                        if (t.type == TransactionType.INCOME) "รายรับ" else "รายจ่าย"
                    )
                    DetailRow("จำนวนเงิน", formatBaht(t.amount))
                    DetailRow("หมวดหมู่", t.category)
                    DetailRow("วันที่", t.date.ifBlank { "ไม่ระบุ" })
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        editTarget = t
                        detailTarget = null
                    }
                ) {
                    Text("แก้ไข", color = HomeViridian)
                }
            },
            dismissButton = {
                TextButton(onClick = { detailTarget = null }) {
                    Text("ปิด")
                }
            }
        )
    }

    // Edit transaction
    editTarget?.let { t ->
        EditHistoryTransactionDialog(
            transaction = t,
            categories = allCategories,
            onDismiss = { editTarget = null },
            onSave = { updated ->
                txRepo.update(updated)
                editTarget = null
            }
        )
    }

    // Confirm delete
    deleteTarget?.let { t ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text("ลบรายการ") },
            text = {
                Text(
                    "ต้องการลบ \"${t.description.ifBlank { t.category }}\" ใช่ไหม?"
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        txRepo.delete(t.id)
                        deleteTarget = null
                        if (detailTarget?.id == t.id) detailTarget = null
                    }
                ) {
                    Text("ลบ", color = HomeDeepBerry)
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteTarget = null }) {
                    Text("ยกเลิก")
                }
            }
        )
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = HomeSubText)
        Spacer(Modifier.width(12.dp))
        Text(
            value,
            color = HomeDarkText,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun EditHistoryTransactionDialog(
    transaction: Transaction,
    categories: List<String>,
    onDismiss: () -> Unit,
    onSave: (Transaction) -> Unit
) {
    var type by remember(transaction.id) {
        mutableStateOf(transaction.type)
    }
    var amount by remember(transaction.id) {
        mutableStateOf(transaction.amount.toString())
    }
    var category by remember(transaction.id) {
        mutableStateOf(transaction.category)
    }
    var description by remember(transaction.id) {
        mutableStateOf(transaction.description)
    }
    var date by remember(transaction.id) {
        mutableStateOf(transaction.date)
    }
    var expanded by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = HomeCardWhite,
        title = {
            Text("แก้ไขรายการ", color = HomeDarkText)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 480.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = type == TransactionType.INCOME,
                        onClick = { type = TransactionType.INCOME }
                    )
                    Text("รายรับ", color = HomeDarkText)

                    RadioButton(
                        selected = type == TransactionType.EXPENSE,
                        onClick = { type = TransactionType.EXPENSE }
                    )
                    Text("รายจ่าย", color = HomeDarkText)
                }

                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("จำนวนเงิน") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Box {
                    OutlinedTextField(
                        value = category,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("หมวดหมู่") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expanded = true }
                    )

                    Box(
                        Modifier
                            .matchParentSize()
                            .clickable { expanded = true }
                    )

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat) },
                                onClick = {
                                    category = cat
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("รายละเอียด") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("วันที่ (วว/ดด/ปปปป)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                error?.let {
                    Text(it, color = HomeDeepBerry, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val value = amount.toDoubleOrNull()
                    when {
                        value == null || value <= 0 ->
                            error = "กรอกจำนวนเงินให้ถูกต้อง"

                        category.isBlank() ->
                            error = "เลือกหมวดหมู่"

                        historyDateMillis(date) == null ->
                            error = "วันที่ไม่ถูกต้อง กรุณาใช้ วว/ดด/ปปปป"

                        else -> {
                            onSave(
                                transaction.copy(
                                    type = type,
                                    amount = value,
                                    category = category,
                                    description = description.trim(),
                                    date = date.trim()
                                )
                            )
                        }
                    }
                }
            ) {
                Text("บันทึก", color = HomeViridian)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("ยกเลิก", color = HomeDeepBerry)
            }
        }
    )
}

@Composable
private fun HistoryItem(
    t: Transaction,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val isIncome = t.type == TransactionType.INCOME
    val bg = if (isIncome) {
        HomeTeaGreen.copy(alpha = 0.75f)
    } else {
        HomeFairyTale.copy(alpha = 0.75f)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp)
            .shadow(5.dp, RoundedCornerShape(19.dp))
            .clip(RoundedCornerShape(19.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(19.dp),
        colors = CardDefaults.cardColors(
            containerColor = HomeCardWhite
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 15.dp, end = 4.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(46.dp),
                shape = CircleShape,
                color = bg
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(categoryEmoji(t.category), fontSize = 22.sp)
                }
            }

            Spacer(Modifier.width(13.dp))

            Column(Modifier.weight(1f)) {
                Text(
                    text = t.description.ifBlank { t.category },
                    fontSize = 16.sp,
                    color = HomeDarkText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = t.category,
                    fontSize = 12.sp,
                    color = HomeSubText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = t.date,
                    fontSize = 11.sp,
                    color = HomeSubText
                )
            }

            Text(
                text = (if (isIncome) "+" else "-") +
                        "฿" + formatMoney(t.amount),
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = if (isIncome) HomeViridian else HomeCyclamen
            )

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "ลบรายการ",
                    tint = HomeSubText
                )
            }
        }
    }
}

@Composable
private fun FilterChipItem(
    text: String,
    selected: Boolean,
    selectedColor: Color,
    outlined: Boolean = false,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(50),
        color = when {
            selected -> selectedColor
            outlined -> Color.Transparent
            else -> HomeCardWhite
        },
        shadowElevation = if (selected || outlined) 0.dp else 2.dp,
        border = if (outlined) {
            androidx.compose.foundation.BorderStroke(1.dp, HomeCyclamen)
        } else {
            null
        }
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(
                horizontal = 16.dp,
                vertical = 8.dp
            ),
            fontSize = 13.sp,
            fontWeight = if (selected) {
                FontWeight.Medium
            } else {
                FontWeight.Normal
            },
            color = when {
                selected -> Color.White
                outlined -> HomeCyclamen
                else -> HomeSubText
            },
            maxLines = 1
        )
    }
}