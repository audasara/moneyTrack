//HomeScreen
package com.example.myfinance

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.moneytrack.R
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.moneytrack.model.Transaction
import com.example.moneytrack.model.TransactionType
import com.example.moneytrack.ui.formatBaht
import com.example.moneytrack.ui.formatMoney
import com.example.moneytrack.viewmodel.TransactionViewModel
import com.google.firebase.auth.FirebaseAuth

// Home color palette — ตั้งชื่อเฉพาะเพื่อป้องกันชื่อซ้ำ
val HomeDeepBerry = Color(0xFF8C2D51)
val HomeCyclamen = Color(0xFFE65E8C)
val HomeFairyTale = Color(0xFFF4C6D8)
val HomeTeaGreen = Color(0xFFC8DDA0)
val HomeViridian = Color(0xFF4A7767)
val HomeCreamBackground = Color(0xFFFFF9F4)
val HomeCardWhite = Color(0xFFFFFEFC)
val HomeDarkText = Color(0xFF40383A)
val HomeSubText = Color(0xFF8E8285)

@Composable
fun HomeScreen(
    onProfileClick: () -> Unit,
    onAddTransactionClick: () -> Unit = {},
    onHistoryClick: () -> Unit = {},
    onDashboardClick: () -> Unit = {}                       // เพิ่มใหม่
) {
    val user = FirebaseAuth.getInstance().currentUser
    val uid = user?.uid ?: "guest"

    // key ต้องตรงกับที่ AddTransactionScreen ใช้ จะได้ ViewModel ตัวเดียวกัน
    val vm: TransactionViewModel = viewModel(key = "tx_$uid")

    // ชื่อที่ใช้ตอนสมัคร ถ้าไม่มีใช้ส่วนหน้า @ ของอีเมลแทน
    val name = user?.displayName?.takeIf { it.isNotBlank() }
        ?: user?.email?.substringBefore("@")
        ?: "ผู้ใช้"

    HomeContent(
        userName = name,
        loaded = vm.loaded,
        balance = vm.balance,
        totalIncome = vm.totalIncome,
        totalExpense = vm.totalExpense,
        recent = vm.recent,
        onProfileClick = onProfileClick,
        onAddTransactionClick = onAddTransactionClick,
        onHistoryClick = onHistoryClick,
        onDashboardClick = onDashboardClick                   // เพิ่มใหม่
    )
}

@Composable
fun HomeContent(
    userName: String,
    loaded: Boolean,
    balance: Double,
    totalIncome: Double,
    totalExpense: Double,
    recent: List<Transaction>,
    onProfileClick: () -> Unit,
    onAddTransactionClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onDashboardClick: () -> Unit
) {
    val isEmpty = loaded && recent.isEmpty()

    CuteBackground {
        Scaffold(
            bottomBar = {
                BottomNavigationBar(
                    selectedIndex = 0,
                    onHomeClick = {},
                    onProfileClick = onProfileClick,
                    onTransactionClick = onAddTransactionClick,
                    onHistoryClick = onHistoryClick,
                    onDashboardClick = onDashboardClick
                )
            },
            containerColor = Color.Transparent
        ) { innerPadding ->

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
            ) {
                Spacer(modifier = Modifier.height(18.dp))

                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(58.dp)
                            .shadow(8.dp, CircleShape)
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(HomeCyclamen, HomeDeepBerry)
                                ),
                                shape = CircleShape
                            )
                            .padding(3.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            shape = CircleShape,
                            color = HomeCardWhite
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.img_profile),
                                contentDescription = "Profile picture",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(3.dp)
                                    .clip(CircleShape)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(13.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "ยินดีต้อนรับ",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = HomeDeepBerry
                        )

                        Spacer(modifier = Modifier.height(1.dp))

                        Text(
                            text = "$userName 👋",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = HomeDarkText,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // โลโก้แอป (เพิ่มใหม่)
                    AppLogo(size = 40.dp)

                    Spacer(modifier = Modifier.width(8.dp))

                    Box(contentAlignment = Alignment.TopEnd) {
                        Surface(
                            modifier = Modifier
                                .size(47.dp)
                                .shadow(8.dp, CircleShape),
                            shape = CircleShape,
                            color = HomeCardWhite
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_notifications),
                                    contentDescription = "Notifications",
                                    modifier = Modifier.size(24.dp),
                                    tint = HomeDeepBerry
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .size(11.dp)
                                .background(HomeCyclamen, CircleShape)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // น้องแมวทักทาย
                CatSays(
                    message = when {
                        !loaded -> "กำลังโหลดข้อมูลอยู่นะ รอแป๊บนึงน้า..."
                        isEmpty -> "ยังไม่มีรายการเลย มาจดรายการแรกกันนะ~ ♡"
                        balance < 0 -> "ยอดติดลบแล้วนะ ใช้เงินระวังหน่อยน้า"
                        else -> "วันนี้ก็ดูแลเงินได้ดีมากเลย เมี๊ยว ♡"
                    },
                    mood = if (isEmpty) CatMood.SLEEPY else CatMood.HAPPY
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Balance card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(16.dp, RoundedCornerShape(28.dp))
                        .clip(RoundedCornerShape(28.dp))
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(HomeDeepBerry, HomeCyclamen)
                            )
                        )
                ) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = 25.dp, y = (-25).dp)
                            .size(125.dp)
                            .background(Color.White.copy(alpha = 0.10f), CircleShape)
                    )

                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .offset(x = 18.dp, y = 30.dp)
                            .size(90.dp)
                            .background(Color.White.copy(alpha = 0.07f), CircleShape)
                    )

                    Column(
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 28.dp)
                    ) {
                        Text(
                            text = "ยอดคงเหลือ",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White.copy(alpha = 0.82f)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = formatBaht(balance),
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Income and expense
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    InfoCard(
                        modifier = Modifier.weight(1f),
                        title = "🌱 รายรับ",
                        amount = formatBaht(totalIncome),
                        backgroundColor = HomeTeaGreen,
                        amountColor = HomeViridian
                    )

                    InfoCard(
                        modifier = Modifier.weight(1f),
                        title = "🌸 รายจ่าย",
                        amount = formatBaht(totalExpense),
                        backgroundColor = HomeFairyTale,
                        amountColor = HomeDeepBerry
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Add transaction
                Button(
                    onClick = { onAddTransactionClick() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(57.dp)
                        .shadow(9.dp, RoundedCornerShape(19.dp)),
                    shape = RoundedCornerShape(19.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = HomeCyclamen),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 0.dp,
                        pressedElevation = 0.dp
                    )
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_add_circle),
                        contentDescription = "Add",
                        modifier = Modifier.size(24.dp),
                        tint = Color.White
                    )

                    Spacer(modifier = Modifier.width(9.dp))

                    Text(
                        text = if (isEmpty) "เริ่มทำธุรกรรมแรกของคุณ" else "เพิ่มรายการ",
                        fontSize = 16.sp,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Recent transactions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "รายการล่าสุด",
                        fontSize = 20.sp,
                        color = HomeDarkText
                    )

                    // กดไปหน้า History
                    Text(
                        text = "ดูทั้งหมด",
                        fontSize = 13.sp,
                        color = HomeCyclamen,
                        modifier = Modifier.clickable { onHistoryClick() }
                    )
                }

                Spacer(modifier = Modifier.height(9.dp))

                when {
                    !loaded -> Unit
                    recent.isEmpty() -> EmptyTransactions()
                    else -> recent.forEach { t ->
                        val isIncome = t.type == TransactionType.INCOME
                        TransactionItem(
                            title = t.description.ifBlank { t.category },
                            category = if (t.description.isBlank()) t.date else "${t.category} • ${t.date}",
                            amount = (if (isIncome) "+฿" else "-฿") + formatMoney(t.amount),
                            isIncome = isIncome,
                            emoji = categoryEmoji(t.category)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun EmptyTransactions() {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        shape = RoundedCornerShape(24.dp),
        elevation = 6.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CatFace(size = 92.dp, mood = CatMood.SLEEPY)

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "ยังไม่มีรายการ",
                fontSize = 17.sp,
                fontWeight = FontWeight.Medium,
                color = HomeDarkText
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "เริ่มทำธุรกรรมแรกของคุณ\nใส่รายการเงินของคุณได้ที่ปุ่มด้านบน",
                fontSize = 13.sp,
                color = HomeSubText,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun InfoCard(
    modifier: Modifier = Modifier,
    title: String,
    amount: String,
    backgroundColor: Color,
    amountColor: Color
) {
    Card(
        modifier = modifier.shadow(
            7.dp,
            RoundedCornerShape(21.dp)
        ),
        shape = RoundedCornerShape(21.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(17.dp)
        ) {
            Text(
                text = title,
                fontSize = 13.sp,
                color = HomeDarkText.copy(alpha = 0.68f)
            )

            Spacer(modifier = Modifier.height(5.dp))

            Text(
                text = amount,
                fontSize = 21.sp,
                color = amountColor
            )
        }
    }
}

@Composable
fun TransactionItem(
    title: String,
    category: String,
    amount: String,
    isIncome: Boolean,
    emoji: String? = null
) {
    val transactionIcon = if (isIncome) R.drawable.ic_income else R.drawable.ic_expense

    val iconBackground = if (isIncome) {
        HomeTeaGreen.copy(alpha = 0.75f)
    } else {
        HomeFairyTale.copy(alpha = 0.75f)
    }

    val iconColor = if (isIncome) HomeViridian else HomeDeepBerry

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        shape = RoundedCornerShape(22.dp),
        elevation = 5.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 15.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(46.dp),
                shape = CircleShape,
                color = iconBackground
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (emoji != null) {
                        Text(text = emoji, fontSize = 22.sp)
                    } else {
                        Icon(
                            painter = painterResource(id = transactionIcon),
                            contentDescription = if (isIncome) "Income" else "Expense",
                            modifier = Modifier.size(22.dp),
                            tint = iconColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(13.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    color = HomeDarkText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(1.dp))

                Text(
                    text = category,
                    fontSize = 12.sp,
                    color = HomeSubText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Text(
                text = amount,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = if (isIncome) HomeViridian else HomeCyclamen
            )
        }
    }
}

@Composable
fun BottomNavigationBar(
    selectedIndex: Int = 0,                               // เปลี่ยน: รับจากภายนอก
    onHomeClick: () -> Unit = {},                         // เพิ่มใหม่
    onProfileClick: () -> Unit,
    onTransactionClick: () -> Unit = {},
    onHistoryClick: () -> Unit = {},
    onDashboardClick: () -> Unit = {}                       // เพิ่มใหม่
) {
    val items = listOf(
        BottomNavItem("Home", R.drawable.ic_home),
        BottomNavItem("Transaction", R.drawable.ic_payments),
        BottomNavItem("History", R.drawable.ic_history),
        BottomNavItem("Dashboard", R.drawable.ic_analytics),
        BottomNavItem("Profile", R.drawable.ic_person)
    )

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(
            topStart = 28.dp,
            topEnd = 28.dp
        ),
        color = HomeCardWhite,
        shadowElevation = 12.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(96.dp)
                .padding(
                    start = 6.dp,
                    end = 6.dp,
                    top = 10.dp,
                    bottom = 8.dp
                ),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEachIndexed { index, item ->
                val selected = selectedIndex == index

                val scale by animateFloatAsState(
                    targetValue = if (selected) 1.08f else 1f,
                    label = "NavScale"
                )

                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        modifier = Modifier
                            .size(if (selected) 48.dp else 40.dp)
                            .scale(scale),
                        shape = CircleShape,
                        color = if (selected) {
                            HomeDeepBerry
                        } else {
                            Color.Transparent
                        },
                        shadowElevation = if (selected) 8.dp else 0.dp,
                        onClick = {
                            when (item.title) {
                                "Home" -> onHomeClick()
                                "Transaction" -> onTransactionClick()
                                "History" -> onHistoryClick()
                                "Dashboard" -> onDashboardClick()
                                "Profile" -> onProfileClick()
                            }
                        }
                    ) {
                        Box(
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(
                                    id = item.icon
                                ),
                                contentDescription = item.title,
                                modifier = Modifier.size(
                                    if (selected) 25.dp else 23.dp
                                ),
                                tint = if (selected) {
                                    Color.White
                                } else {
                                    HomeSubText
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(5.dp))

                    Text(
                        text = item.title,
                        fontSize = when (item.title) {
                            "Transaction", "Dashboard" -> 10.sp
                            else -> 11.sp
                        },
                        fontWeight = if (selected) {
                            FontWeight.Medium
                        } else {
                            FontWeight.Normal
                        },
                        color = if (selected) {
                            HomeDeepBerry
                        } else {
                            HomeSubText
                        },
                        maxLines = 1
                    )
                }
            }
        }
    }
}

data class BottomNavItem(
    val title: String,
    val icon: Int
)