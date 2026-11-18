//DashboardScreen  (โทนเขียวเหมือนหน้า Login + ชมพูแทรก + ของตกแต่งแบบ Home/Welcome)
package com.example.myfinance

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.moneytrack.model.DefaultCategories
import com.example.moneytrack.model.Transaction
import com.example.moneytrack.model.TransactionType
import com.example.moneytrack.ui.formatBaht
import com.example.moneytrack.viewmodel.CategoryViewModel
import com.example.moneytrack.viewmodel.TransactionViewModel
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.abs

/* ---------------------------------------------------------------
 * data class / enum ของหน้า Dashboard
 * --------------------------------------------------------------- */
private enum class DashPeriod(val label: String) {
    MONTH("เดือนนี้"),
    WEEK("7 วัน"),
    ALL("ทั้งหมด")
}

private data class DashSlice(val name: String, val value: Double, val color: Color)

private data class MonthStat(val label: String, val income: Double, val expense: Double)

private val dashPalette = listOf(
    Viridian, Cyclamen, Matcha, CamaronPink,
    AvocadoCream, QuinacridoneMagenta, TeaGreen, StrawberryMilk
)

private val thaiMonths = listOf(
    "ม.ค.", "ก.พ.", "มี.ค.", "เม.ย.", "พ.ค.", "มิ.ย.",
    "ก.ค.", "ส.ค.", "ก.ย.", "ต.ค.", "พ.ย.", "ธ.ค."
)

/* ---------------------------------------------------------------
 * Helper: วันที่รูปแบบ dd/MM/yyyy (เหมือนหน้า Transaction / History)
 * --------------------------------------------------------------- */
private fun dashMillis(date: String): Long? = try {
    SimpleDateFormat("dd/MM/yyyy", Locale.US).apply { isLenient = false }
        .parse(date)?.time
} catch (_: Exception) {
    null
}

private fun periodMatches(date: String, period: DashPeriod): Boolean {
    if (period == DashPeriod.ALL) return true
    val ms = dashMillis(date) ?: return false
    val now = Calendar.getInstance()

    return when (period) {
        DashPeriod.MONTH -> {
            val c = Calendar.getInstance().apply { timeInMillis = ms }
            c.get(Calendar.YEAR) == now.get(Calendar.YEAR) &&
                    c.get(Calendar.MONTH) == now.get(Calendar.MONTH)
        }

        DashPeriod.WEEK -> {
            val start = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                add(Calendar.DAY_OF_YEAR, -6)
            }.timeInMillis
            ms >= start
        }

        DashPeriod.ALL -> true
    }
}

private fun buildMonthStats(list: List<Transaction>): List<MonthStat> {
    val months = (5 downTo 0).map { back ->
        Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            add(Calendar.MONTH, -back)
        }
    }

    return months.map { c ->
        val y = c.get(Calendar.YEAR)
        val m = c.get(Calendar.MONTH)
        var income = 0.0
        var expense = 0.0

        list.forEach { t ->
            val ms = dashMillis(t.date) ?: return@forEach
            val tc = Calendar.getInstance().apply { timeInMillis = ms }
            if (tc.get(Calendar.YEAR) == y && tc.get(Calendar.MONTH) == m) {
                if (t.type == TransactionType.INCOME) income += t.amount
                else if (t.type == TransactionType.EXPENSE) expense += t.amount
            }
        }
        MonthStat(thaiMonths[m], income, expense)
    }
}

/* ---------------------------------------------------------------
 * Stateful: ดึงข้อมูลจาก ViewModel ตัวเดียวกับ Home / Transaction
 * --------------------------------------------------------------- */
@Composable
fun DashboardScreen(
    onHomeClick: () -> Unit,
    onTransactionClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: "guest"
    val txVm: TransactionViewModel = viewModel(key = "tx_$uid")
    val catVm: CategoryViewModel = viewModel(key = "cat_$uid")

    // หมวดหมู่ชุดเดียวกับหน้า Transaction: หมวดของระบบ + หมวดที่ผู้ใช้เพิ่มเอง
    val categoryNames = (DefaultCategories.list.map { it.name } + catVm.categories.map { it.name })
        .distinct()

    DashboardContent(
        loaded = txVm.loaded,
        transactions = txVm.transactions,
        categoryNames = categoryNames,
        onHomeClick = onHomeClick,
        onTransactionClick = onTransactionClick,
        onHistoryClick = onHistoryClick,
        onProfileClick = onProfileClick
    )
}

@Composable
fun DashboardContent(
    loaded: Boolean,
    transactions: List<Transaction>,
    categoryNames: List<String>,
    onHomeClick: () -> Unit,
    onTransactionClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    var period by remember { mutableStateOf(DashPeriod.MONTH) }
    var selectedCategory by remember { mutableStateOf<String?>(null) }

    val allCategories = remember(categoryNames, transactions) {
        (categoryNames + transactions.map { it.category })
            .filter { it.isNotBlank() }
            .distinct()
    }

    val periodTx = remember(transactions, period) {
        transactions.filter { periodMatches(it.date, period) }
    }

    val scopedTx = remember(periodTx, selectedCategory) {
        if (selectedCategory == null) periodTx
        else periodTx.filter { it.category == selectedCategory }
    }

    val income = remember(scopedTx) {
        scopedTx.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
    }
    val expense = remember(scopedTx) {
        scopedTx.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
    }
    val net = income - expense

    val slices = remember(periodTx) {
        periodTx
            .filter { it.type == TransactionType.EXPENSE }
            .groupBy { it.category.ifBlank { "อื่น ๆ" } }
            .map { (name, list) -> name to list.sumOf { it.amount } }
            .sortedByDescending { it.second }
            .mapIndexed { i, (name, value) ->
                DashSlice(name, value, dashPalette[i % dashPalette.size])
            }
    }
    val totalSlices = slices.sumOf { it.value }

    val monthStats = remember(transactions, selectedCategory) {
        buildMonthStats(
            if (selectedCategory == null) transactions
            else transactions.filter { it.category == selectedCategory }
        )
    }

    val savingRate: Int? = if (income > 0) ((income - expense) / income * 100).toInt() else null

    CuteBackground(
        gradient = listOf(Color(0xFFE2EEC6), Color(0xFFF0F5E2), AppBackground),
        sparkleColor = Cyclamen.copy(alpha = 0.55f)
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            bottomBar = {
                BottomNavigationBar(
                    selectedIndex = 3,
                    onHomeClick = onHomeClick,
                    onProfileClick = onProfileClick,
                    onTransactionClick = onTransactionClick,
                    onHistoryClick = onHistoryClick,
                    onDashboardClick = {}
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
            ) {
                Spacer(Modifier.height(18.dp))

                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = "Dashboard",
                            fontSize = 27.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = HomeDarkText
                        )
                        Text(
                            text = "ภาพรวมการเงินของคุณ ✦",
                            fontSize = 13.sp,
                            color = Viridian
                        )
                    }
                    AppLogo(size = 48.dp)
                }

                Spacer(Modifier.height(14.dp))

                CatSays(
                    message = when {
                        !loaded -> "กำลังรวบรวมตัวเลขให้อยู่นะ รอแป๊บน้า..."
                        periodTx.isEmpty() -> "ช่วงนี้ยังไม่มีรายการเลย ไปจดกันเถอะ~ ♡"
                        expense > income && income > 0 -> "รายจ่ายมากกว่ารายรับแล้วนะ ระวังหน่อยน้า"
                        else -> "เก่งมากเลย! ดูแลเงินได้ดีแบบนี้ต่อไปนะ ♡"
                    },
                    mood = if (loaded && periodTx.isEmpty()) CatMood.SLEEPY else CatMood.WINK
                )

                Spacer(Modifier.height(14.dp))

                // Period chips
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(DashPeriod.values().toList()) { p ->
                        DashChip(
                            text = p.label,
                            selected = period == p,
                            selectedColor = Viridian
                        ) { period = p }
                    }
                }

                Spacer(Modifier.height(8.dp))

                // Category chips (หมวดเดียวกับหน้า Transaction / History)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        DashChip(
                            text = "ทุกหมวด",
                            selected = selectedCategory == null,
                            selectedColor = Cyclamen
                        ) { selectedCategory = null }
                    }
                    items(allCategories) { cat ->
                        DashChip(
                            text = "${categoryEmoji(cat)} $cat",
                            selected = selectedCategory == cat,
                            selectedColor = Cyclamen
                        ) { selectedCategory = if (selectedCategory == cat) null else cat }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Summary card
                SummaryCard(
                    periodLabel = period.label,
                    categoryLabel = selectedCategory,
                    net = net,
                    income = income,
                    expense = expense,
                    savingRate = savingRate
                )

                Spacer(Modifier.height(16.dp))

                // Donut: สัดส่วนรายจ่ายตามหมวดหมู่
                DashCard(
                    title = "สัดส่วนรายจ่าย",
                    subtitle = "แยกตามหมวดหมู่ (${period.label}) · แตะรายการเพื่อกรอง"
                ) {
                    if (!loaded) {
                        Box(Modifier.fillMaxWidth().height(120.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = Viridian)
                        }
                    } else if (slices.isEmpty()) {
                        EmptyChart("ยังไม่มีรายจ่ายในช่วงนี้")
                    } else {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            DonutChart(
                                slices = slices,
                                total = totalSlices,
                                selected = selectedCategory
                            )
                        }

                        Spacer(Modifier.height(14.dp))

                        slices.forEach { s ->
                            LegendRow(
                                slice = s,
                                percent = if (totalSlices > 0) (s.value / totalSlices * 100).toInt() else 0,
                                selected = selectedCategory == s.name,
                                dimmed = selectedCategory != null && selectedCategory != s.name,
                                onClick = {
                                    selectedCategory = if (selectedCategory == s.name) null else s.name
                                }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Bars: 6 เดือนล่าสุด
                DashCard(
                    title = "รายรับ-รายจ่าย 6 เดือนล่าสุด",
                    subtitle = if (selectedCategory == null) "ทุกหมวดหมู่" else "เฉพาะหมวด ${selectedCategory}"
                ) {
                    if (monthStats.all { it.income == 0.0 && it.expense == 0.0 }) {
                        EmptyChart("ยังไม่มีข้อมูลรายเดือน")
                    } else {
                        MonthBars(monthStats)
                        Spacer(Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            LegendDot(Viridian, "รายรับ")
                            Spacer(Modifier.width(18.dp))
                            LegendDot(Cyclamen, "รายจ่าย")
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Insight cards
                val top = slices.firstOrNull()
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    InsightCard(
                        modifier = Modifier.weight(1f),
                        emoji = if (top != null) categoryEmoji(top.name) else "🏆",
                        title = "ใช้จ่ายมากสุด",
                        value = top?.name ?: "-",
                        sub = if (top != null) formatBaht(top.value) else "ยังไม่มีข้อมูล"
                    )
                    InsightCard(
                        modifier = Modifier.weight(1f),
                        emoji = "🧾",
                        title = "จำนวนรายการ",
                        value = "${scopedTx.size} รายการ",
                        sub = period.label
                    )
                }

                Spacer(Modifier.height(18.dp))

                // Quick actions → Transaction / History
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(54.dp)
                            .shadow(8.dp, RoundedCornerShape(27.dp), spotColor = Cyclamen)
                            .clip(RoundedCornerShape(27.dp))
                            .background(Brush.horizontalGradient(listOf(Cyclamen, HomeDeepBerry)))
                            .clickable { onTransactionClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "＋ เพิ่มรายการ",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(54.dp)
                            .shadow(8.dp, RoundedCornerShape(27.dp), spotColor = Viridian)
                            .clip(RoundedCornerShape(27.dp))
                            .background(Brush.horizontalGradient(listOf(Matcha, Viridian)))
                            .clickable { onHistoryClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "ดูประวัติ →",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

/* ---------------------------------------------------------------
 * Components
 * --------------------------------------------------------------- */
@Composable
private fun DashChip(
    text: String,
    selected: Boolean,
    selectedColor: Color,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(50),
        color = if (selected) selectedColor else Color.White.copy(alpha = 0.92f),
        shadowElevation = if (selected) 0.dp else 2.dp,
        border = if (selected) null else BorderStroke(1.dp, FairyTale)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal,
            color = if (selected) Color.White else HomeSubText,
            maxLines = 1
        )
    }
}

@Composable
private fun SummaryCard(
    periodLabel: String,
    categoryLabel: String?,
    net: Double,
    income: Double,
    expense: Double,
    savingRate: Int?
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(16.dp, RoundedCornerShape(28.dp), spotColor = Viridian)
            .clip(RoundedCornerShape(28.dp))
            .background(Brush.linearGradient(listOf(Viridian, Matcha)))
    ) {
        // ของตกแต่งสีชมพู
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 28.dp, y = (-28).dp)
                .size(130.dp)
                .background(Cyclamen.copy(alpha = 0.28f), CircleShape)
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = 20.dp, y = 34.dp)
                .size(92.dp)
                .background(FairyTale.copy(alpha = 0.25f), CircleShape)
        )
        Text(
            text = "✦",
            color = Color.White.copy(alpha = 0.85f),
            fontSize = 20.sp,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 22.dp, end = 30.dp)
        )

        Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 24.dp)) {
            Text(
                text = "ยอดสุทธิ · $periodLabel" +
                        (if (categoryLabel != null) " · $categoryLabel" else ""),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.88f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.height(6.dp))

            Text(
                text = (if (net > 0) "+" else "") + formatBaht(net),
                fontSize = 34.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MiniStat(Modifier.weight(1f), "🌱 รายรับ", formatBaht(income))
                MiniStat(Modifier.weight(1f), "🌸 รายจ่าย", formatBaht(expense))
            }

            if (savingRate != null) {
                Spacer(Modifier.height(12.dp))
                Surface(
                    shape = RoundedCornerShape(50),
                    color = FairyTale
                ) {
                    Text(
                        text = if (savingRate >= 0) "🌸 ออมได้ $savingRate% ของรายรับ"
                        else "⚠️ ใช้เกินรายรับ ${abs(savingRate)}%",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = HomeDeepBerry,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun MiniStat(modifier: Modifier, title: String, value: String) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White.copy(alpha = 0.18f))
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Text(title, fontSize = 12.sp, color = Color.White.copy(alpha = 0.85f))
        Text(
            value,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun DashCard(
    title: String,
    subtitle: String,
    content: @Composable ColumnScope.() -> Unit
) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = title,
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color = HomeDarkText
            )
            Text(text = subtitle, fontSize = 12.sp, color = HomeSubText)
            Spacer(Modifier.height(14.dp))
            content()
        }
    }
}

@Composable
private fun EmptyChart(message: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CatFace(size = 72.dp, mood = CatMood.SLEEPY)
        Spacer(Modifier.height(6.dp))
        Text(message, fontSize = 13.sp, color = HomeSubText, textAlign = TextAlign.Center)
    }
}

@Composable
private fun DonutChart(
    slices: List<DashSlice>,
    total: Double,
    selected: String?,
    size: Dp = 176.dp
) {
    Box(modifier = Modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeW = 26.dp.toPx()
            val diameter = this.size.minDimension - strokeW
            val topLeft = Offset(
                (this.size.width - diameter) / 2f,
                (this.size.height - diameter) / 2f
            )
            val arcSize = Size(diameter, diameter)

            drawArc(
                color = FairyTale.copy(alpha = 0.35f),
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeW)
            )

            var start = -90f
            slices.forEach { s ->
                val sweep = (s.value / total * 360.0).toFloat()
                val alpha = if (selected == null || selected == s.name) 1f else 0.3f
                drawArc(
                    color = s.color.copy(alpha = alpha),
                    startAngle = start,
                    sweepAngle = (sweep - 2f).coerceAtLeast(0.5f),
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeW, cap = StrokeCap.Butt)
                )
                start += sweep
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("รายจ่ายรวม", fontSize = 12.sp, color = HomeSubText)
            Text(
                formatBaht(total),
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = HomeDarkText,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun LegendRow(
    slice: DashSlice,
    percent: Int,
    selected: Boolean,
    dimmed: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .background(if (selected) FairyTale.copy(alpha = 0.35f) else Color.Transparent)
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(10.dp)
                    .background(slice.color.copy(alpha = if (dimmed) 0.35f else 1f), CircleShape)
            )
            Spacer(Modifier.width(8.dp))
            Text(categoryEmoji(slice.name), fontSize = 16.sp)
            Spacer(Modifier.width(6.dp))
            Text(
                text = slice.name,
                modifier = Modifier.weight(1f),
                fontSize = 14.sp,
                color = if (dimmed) HomeSubText else HomeDarkText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "$percent%",
                fontSize = 12.sp,
                color = HomeSubText
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text = formatBaht(slice.value),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = if (dimmed) HomeSubText else HomeDarkText
            )
        }

        Spacer(Modifier.height(5.dp))

        Box(
            Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(50))
                .background(FairyTale.copy(alpha = 0.3f))
        ) {
            Box(
                Modifier
                    .fillMaxHeight()
                    .fillMaxWidth((percent / 100f).coerceIn(0.02f, 1f))
                    .clip(RoundedCornerShape(50))
                    .background(slice.color.copy(alpha = if (dimmed) 0.35f else 1f))
            )
        }
    }
}

@Composable
private fun MonthBars(data: List<MonthStat>) {
    val maxV = (data.maxOfOrNull { maxOf(it.income, it.expense) } ?: 0.0).coerceAtLeast(1.0)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom
    ) {
        data.forEach { m ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(
                    modifier = Modifier.height(120.dp),
                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    Bar(m.income / maxV, Viridian)
                    Bar(m.expense / maxV, Cyclamen)
                }
                Spacer(Modifier.height(4.dp))
                Text(m.label, fontSize = 11.sp, color = HomeSubText)
            }
        }
    }
}

@Composable
private fun Bar(fraction: Double, color: Color) {
    if (fraction <= 0.0) {
        Spacer(Modifier.width(12.dp))
    } else {
        Box(
            modifier = Modifier
                .width(12.dp)
                .fillMaxHeight(fraction.toFloat().coerceIn(0.03f, 1f))
                .background(color, RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
        )
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(10.dp).background(color, CircleShape))
        Spacer(Modifier.width(6.dp))
        Text(label, fontSize = 12.sp, color = HomeSubText)
    }
}

@Composable
private fun InsightCard(
    modifier: Modifier,
    emoji: String,
    title: String,
    value: String,
    sub: String
) {
    GlassCard(
        modifier = modifier,
        shape = RoundedCornerShape(22.dp),
        elevation = 6.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(emoji, fontSize = 24.sp)
            Spacer(Modifier.height(6.dp))
            Text(title, fontSize = 12.sp, color = HomeSubText)
            Text(
                value,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = HomeDarkText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                sub,
                fontSize = 12.sp,
                color = Viridian,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}