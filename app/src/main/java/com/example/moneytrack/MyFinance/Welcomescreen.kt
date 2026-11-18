//WelcomeScreen  (Welcome → Login → Register เลื่อนซ้าย-ขวาได้)
@file:OptIn(ExperimentalFoundationApi::class)

package com.example.myfinance

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlin.math.abs

/**
 * หน้าเริ่มต้นของแอป: เลื่อนซ้าย-ขวาระหว่าง Welcome (0), Login (1), Register (2)
 * initialPage: 0 = Welcome, 1 = Login, 2 = Register
 * แต่ละหน้าวาดพื้นหลังของตัวเอง (Login/Register เป็นพื้นเขียวมีหญ้าตามแบบเดิม)
 */
@Composable
fun WelcomeScreen(
    initialPage: Int = 0,
    onAuthSuccess: () -> Unit
) {
    val pagerState = rememberPagerState(initialPage = initialPage, pageCount = { 3 })
    val scope = rememberCoroutineScope()
    val goTo: (Int) -> Unit = { p -> scope.launch { pagerState.animateScrollToPage(p) } }

    Box(modifier = Modifier.fillMaxSize()) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            when (page) {
                0 -> CuteBackground {
                    WelcomePage(
                        onNextClick = { goTo(1) },
                        onRegisterClick = { goTo(2) }
                    )
                }

                1 -> LoginScreen(
                    onRegisterClick = { goTo(2) },
                    onLoginSuccess = onAuthSuccess
                )

                else -> RegisterScreen(
                    onLoginClick = { goTo(1) },
                    onRegisterSuccess = { goTo(1) }
                )
            }
        }

        PagerDots(
            current = pagerState.currentPage,
            count = 3,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 10.dp)
        )
    }
}

@Composable
private fun PagerDots(current: Int, count: Int, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(count) { i ->
            val w by animateDpAsState(
                targetValue = if (i == current) 24.dp else 8.dp,
                label = "dotWidth"
            )
            Box(
                Modifier
                    .width(w)
                    .height(8.dp)
                    .background(
                        if (i == current) HomeCyclamen else HomeCyclamen.copy(alpha = 0.30f),
                        CircleShape
                    )
            )
        }
    }
}

@Composable
private fun WelcomePage(
    onNextClick: () -> Unit,
    onRegisterClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 22.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(34.dp))

        // Greeting
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CatFace(size = 46.dp, mood = CatMood.WINK)
            Spacer(Modifier.width(10.dp))
            Column {
                Text(
                    "สวัสดีจ้า~",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = HomeDeepBerry
                )
                Text("มาดูแลเงินด้วยกันนะ ♡", fontSize = 13.sp, color = HomeSubText)
            }
        }

        Spacer(Modifier.height(10.dp))

        // Brand
        Text(
            text = "MoneyTrack",
            fontSize = 36.sp,
            fontWeight = FontWeight.ExtraBold,
            color = HomeDeepBerry
        )
        Text(
            text = "ออมทีละนิด ชีวิตสดใสขึ้น",
            fontSize = 13.sp,
            color = HomeSubText
        )

        Spacer(Modifier.height(16.dp))

        // Hero card
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(370.dp)
            ) {
                Box(
                    Modifier
                        .matchParentSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0x00FFFFFF), HomeFairyTale.copy(alpha = 0.55f))
                            )
                        )
                )

                Column(Modifier.padding(start = 20.dp, top = 20.dp)) {
                    Text(
                        text = buildAnnotatedString {
                            append("จัดการเงิน\nให้ง่ายและ")
                            withStyle(SpanStyle(color = HomeCyclamen)) { append("น่ารัก") }
                        },
                        fontSize = 25.sp,
                        lineHeight = 34.sp,
                        fontWeight = FontWeight.Bold,
                        color = HomeDarkText
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "บันทึกรายรับ-รายจ่าย\nแยกหมวดหมู่ ดูประวัติได้ง่าย",
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                        color = HomeSubText
                    )
                }

                Text(
                    text = "Save\nTrack\nSmile ♡",
                    fontFamily = FontFamily.Cursive,
                    fontSize = 17.sp,
                    textAlign = TextAlign.End,
                    color = HomeDeepBerry.copy(alpha = 0.85f),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 22.dp, end = 20.dp)
                        .rotate(-8f)
                )

                WelcomeHero(Modifier.align(Alignment.BottomCenter))

                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 14.dp, bottom = 14.dp),
                    shape = RoundedCornerShape(50),
                    color = Color.White.copy(alpha = 0.92f),
                    shadowElevation = 4.dp,
                    border = BorderStroke(1.dp, HomeFairyTale)
                ) {
                    Text(
                        text = "🐾 เริ่มจดรายการแรก",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = HomeDeepBerry,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    )
                }
            }
        }

        Spacer(Modifier.height(14.dp))

        // Feature strip
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            elevation = 6.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                FeatureItem("📝", "จดง่าย")
                FeatureItem("🗂️", "แยกหมวด")
                FeatureItem("📊", "ดูประวัติ")
                FeatureItem("🔒", "ปลอดภัย")
            }
        }

        Spacer(Modifier.height(20.dp))

        CutePrimaryButton(text = "Next  →", onClick = onNextClick)

        Spacer(Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("ยังไม่มีบัญชี? ", fontSize = 14.sp, color = HomeSubText)
            Text(
                text = "สมัครสมาชิก",
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold,
                color = HomeCyclamen,
                modifier = Modifier.clickable { onRegisterClick() }
            )
        }

        Spacer(Modifier.height(8.dp))

        Text(
            text = "กด Next หรือเลื่อนไปทางซ้ายเพื่อเข้าสู่ระบบ ›",
            fontSize = 11.sp,
            color = HomeSubText.copy(alpha = 0.8f)
        )

        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun FeatureItem(emoji: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(emoji, fontSize = 22.sp)
        Spacer(Modifier.height(2.dp))
        Text(label, fontSize = 11.sp, color = HomeDarkText)
    }
}

@Composable
private fun WelcomeHero(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "float")
    val floatA by transition.animateFloat(
        initialValue = -8f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            tween(2200, easing = FastOutSlowInEasing),
            RepeatMode.Reverse
        ),
        label = "floatA"
    )
    val floatB by transition.animateFloat(
        initialValue = 8f,
        targetValue = -8f,
        animationSpec = infiniteRepeatable(
            tween(2600, easing = FastOutSlowInEasing),
            RepeatMode.Reverse
        ),
        label = "floatB"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(230.dp)
    ) {
        // แท่นกระจก
        Canvas(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 10.dp)
                .width(240.dp)
                .height(72.dp)
        ) {
            drawOval(
                brush = Brush.verticalGradient(
                    listOf(Color.White.copy(alpha = 0.95f), HomeFairyTale.copy(alpha = 0.55f))
                )
            )
            drawOval(
                color = HomeCyclamen.copy(alpha = 0.25f),
                style = Stroke(width = 2.dp.toPx())
            )
            drawOval(
                color = HomeFairyTale.copy(alpha = 0.7f),
                topLeft = Offset(size.width * 0.14f, size.height * 0.16f),
                size = Size(size.width * 0.72f, size.height * 0.68f)
            )
        }

        // โลโก้แมวกลางแท่น
        AppLogo(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = (-14).dp)
                .shadow(20.dp, RoundedCornerShape(42.dp), spotColor = HomeCyclamen),
            size = 150.dp
        )

        CoinBubble(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 26.dp, top = 8.dp)
                .offset(y = floatA.dp),
            size = 46.dp
        )
        CoinBubble(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(end = 24.dp, top = 30.dp)
                .offset(y = floatB.dp),
            size = 40.dp
        )
        CoinBubble(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 40.dp, top = 90.dp)
                .offset(y = floatA.dp),
            size = 30.dp
        )
    }
}

@Composable
private fun CoinBubble(modifier: Modifier = Modifier, size: Dp) {
    Surface(
        modifier = modifier
            .size(size)
            .shadow(8.dp, CircleShape, spotColor = HomeCyclamen),
        shape = CircleShape,
        color = Color.White.copy(alpha = 0.92f),
        border = BorderStroke(1.5.dp, HomeFairyTale)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = "฿",
                fontSize = (size.value * 0.46f).sp,
                fontWeight = FontWeight.ExtraBold,
                color = HomeCyclamen
            )
        }
    }
}