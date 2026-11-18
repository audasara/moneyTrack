//LoginScreen  (หน้าเดิมสีเขียวมีหญ้า + น้องแมว + ชื่อ MoneyTrack แทรกชมพู)
package com.example.myfinance

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.moneytrack.R
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.Dp
import com.google.firebase.auth.FirebaseAuth

/* ---------------------------------------------------------------
 * ชิ้นส่วนที่ใช้ร่วมกับ RegisterScreen
 * --------------------------------------------------------------- */

/** ชื่อแอป: "Money" เขียว + "Track" ชมพู + ประกายชมพู */
@Composable
fun MoneyTrackTitle(
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 36.sp
) {
    Row(modifier = modifier, verticalAlignment = Alignment.Top) {
        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(color = Viridian)) { append("Money") }
                withStyle(SpanStyle(color = Cyclamen)) { append("Track") }
            },
            fontSize = fontSize,
            fontWeight = FontWeight.ExtraBold
        )
        Text(
            text = "✦",
            color = Cyclamen,
            fontSize = fontSize * 0.45f,
            modifier = Modifier.padding(start = 2.dp, top = 2.dp)
        )
    }
}

/** ช่องกรอกสไตล์เดิมของ Login/Register (เปลี่ยนสีเน้นได้) */
@Composable
fun ClassicField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    leading: ImageVector,
    accent: Color,
    isPassword: Boolean = false
) {
    var visible by remember { mutableStateOf(false) }

    val trailing: (@Composable () -> Unit)? = if (isPassword) {
        {
            IconButton(onClick = { visible = !visible }) {
                Icon(
                    imageVector = if (visible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                    contentDescription = null,
                    tint = TextMuted
                )
            }
        }
    } else null

    Text(
        text = label,
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        color = TextDarkPrimary
    )

    Spacer(Modifier.height(4.dp))

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text(placeholder, color = TextMuted, fontSize = 14.sp) },
        leadingIcon = { Icon(leading, contentDescription = null, tint = accent) },
        trailingIcon = trailing,
        visualTransformation = if (isPassword && !visible)
            PasswordVisualTransformation() else VisualTransformation.None,
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = TextDarkPrimary,
            unfocusedTextColor = TextDarkPrimary,
            focusedBorderColor = accent,
            unfocusedBorderColor = BorderSoft,
            focusedContainerColor = AppBackground,
            unfocusedContainerColor = AppBackground
        )
    )
}

/** ปุ่มไล่สีสไตล์เดิม */
@Composable
fun ClassicGradientButton(
    text: String,
    loading: Boolean,
    colors: List<Color>,
    shadowColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .shadow(6.dp, RoundedCornerShape(16.dp), spotColor = shadowColor)
            .clip(RoundedCornerShape(16.dp))
            .background(Brush.horizontalGradient(colors))
            .clickable(enabled = !loading, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (loading) {
            CircularProgressIndicator(color = CardWhite, modifier = Modifier.size(24.dp))
        } else {
            Text(
                text = text,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = CardWhite
            )
        }
    }
}

/* ---------------------------------------------------------------
 * ดอกไม้/เหรียญลอยตกแต่ง (ลอยขึ้นลงเบา ๆ เหมือนหน้า Welcome)
 * --------------------------------------------------------------- */
@Composable
private fun FloatingBubble(
    emoji: String,
    size: Dp,
    modifier: Modifier = Modifier,
    reverse: Boolean = false
) {
    val transition = rememberInfiniteTransition(label = "bubble")
    val dy by transition.animateFloat(
        initialValue = if (reverse) 7f else -7f,
        targetValue = if (reverse) -7f else 7f,
        animationSpec = infiniteRepeatable(
            tween(2400, easing = FastOutSlowInEasing),
            RepeatMode.Reverse
        ),
        label = "dy"
    )

    Surface(
        modifier = modifier
            .size(size)
            .offset(y = dy.dp)
            .shadow(6.dp, CircleShape, spotColor = Cyclamen),
        shape = CircleShape,
        color = Color.White.copy(alpha = 0.90f),
        border = BorderStroke(1.5.dp, FairyTale)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(text = emoji, fontSize = (size.value * 0.5f).sp)
        }
    }
}

/* ---------------------------------------------------------------
 * LoginScreen (เป็นหน้าที่ 2 ใน WelcomeScreen)
 * ตกแต่งแบบเดียวกับหน้า Welcome แต่คงโทนเขียว
 * ช่องกรอก ปุ่ม และตรรกะ Firebase เหมือนเดิมทุกอย่าง
 * --------------------------------------------------------------- */
@Composable
fun LoginScreen(
    onRegisterClick: () -> Unit,
    onLoginSuccess: () -> Unit = {}
) {
    val context = LocalContext.current
    val auth = remember { FirebaseAuth.getInstance() }

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }

    CuteBackground(
        // ไล่โทน: เขียวอ่อนด้านบน -> ครีม -> ชมพูจาง ๆ ด้านล่าง
        gradient = listOf(Color(0xFFD9EAB8), Color(0xFFF0F6E2), Color(0xFFFFF1F6)),
        sparkleColor = Cyclamen.copy(alpha = 0.55f)
    ) {
        // ดอกไม้ลอยรอบ ๆ หน้าจอ
        FloatingBubble("🌸", 40.dp, Modifier.align(Alignment.TopStart).padding(start = 14.dp, top = 76.dp))
        FloatingBubble("🌼", 34.dp, Modifier.align(Alignment.TopEnd).padding(end = 16.dp, top = 120.dp), reverse = true)
        FloatingBubble("🌿", 30.dp, Modifier.align(Alignment.CenterStart).padding(start = 6.dp, top = 150.dp), reverse = true)
        FloatingBubble("🌷", 36.dp, Modifier.align(Alignment.CenterEnd).padding(end = 8.dp, top = 40.dp))

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(start = 24.dp, end = 24.dp, top = 28.dp, bottom = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // การ์ดกระจกด้านบน (คล้ายการ์ด Hero ของหน้า Welcome)
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(190.dp)
                ) {
                    Box(
                        Modifier
                            .matchParentSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color(0x00FFFFFF), TeaGreen.copy(alpha = 0.55f))
                                )
                            )
                    )

                    Image(
                        painter = painterResource(id = R.drawable.airplane),
                        contentDescription = "Paper airplane",
                        contentScale = ContentScale.FillBounds,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .fillMaxWidth()
                            .height(150.dp)
                            .padding(horizontal = 8.dp)
                    )

                    Text(
                        text = "Welcome\nback ♡",
                        fontFamily = FontFamily.Cursive,
                        fontSize = 18.sp,
                        color = Viridian,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(start = 18.dp, top = 14.dp)
                            .rotate(-6f)
                    )

                    FloatingBubble(
                        "🌸", 30.dp,
                        Modifier.align(Alignment.TopEnd).padding(end = 16.dp, top = 14.dp)
                    )
                    FloatingBubble(
                        "🌼", 26.dp,
                        Modifier.align(Alignment.BottomEnd).padding(end = 30.dp, bottom = 10.dp),
                        reverse = true
                    )

                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(start = 14.dp, bottom = 12.dp),
                        shape = RoundedCornerShape(50),
                        color = Color.White.copy(alpha = 0.92f),
                        shadowElevation = 4.dp,
                        border = BorderStroke(1.dp, FairyTale)
                    ) {
                        Text(
                            text = "🌿 เริ่มดูแลเงินกันเลย",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Viridian,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(22.dp))

            // ไอคอนกระเป๋าเงิน + น้องแมวโผล่มุม
            Box(contentAlignment = Alignment.BottomEnd) {
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .shadow(8.dp, CircleShape, spotColor = TeaGreen)
                        .background(
                            Brush.radialGradient(listOf(TeaGreen, AppBackground)),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountBalanceWallet,
                        contentDescription = "Wallet icon",
                        tint = Viridian,
                        modifier = Modifier.size(40.dp)
                    )
                }

                CatFace(
                    size = 42.dp,
                    mood = CatMood.WINK,
                    modifier = Modifier.offset(x = 22.dp, y = 6.dp)
                )
            }

            Spacer(Modifier.height(18.dp))

            MoneyTrackTitle()

            Spacer(Modifier.height(4.dp))

            Text(
                text = "จัดการเงินง่าย ๆ ไปกับเรา ♡",
                fontSize = 14.sp,
                color = TextMuted,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(Modifier.height(24.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(6.dp, RoundedCornerShape(24.dp), spotColor = FairyTale),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                border = BorderStroke(1.dp, FairyTale)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "ยินดีต้อนรับกลับมา",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDarkPrimary
                    )

                    Text(
                        text = "เข้าสู่ระบบเพื่อจัดการการเงินของคุณ",
                        fontSize = 13.sp,
                        color = TextMuted,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(Modifier.height(16.dp))

                    ClassicField(
                        label = "อีเมล",
                        value = email,
                        onValueChange = {
                            email = it
                            message = ""
                        },
                        placeholder = "กรอกอีเมลของคุณ",
                        leading = Icons.Default.Email,
                        accent = Viridian
                    )

                    Spacer(Modifier.height(12.dp))

                    ClassicField(
                        label = "รหัสผ่าน",
                        value = password,
                        onValueChange = {
                            password = it
                            message = ""
                        },
                        placeholder = "กรอกรหัสผ่าน",
                        leading = Icons.Default.Lock,
                        accent = Viridian,
                        isPassword = true
                    )

                    if (message.isNotEmpty()) {
                        Spacer(Modifier.height(10.dp))
                        Text(
                            text = message,
                            color = QuinacridoneMagenta,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(Modifier.height(20.dp))

                    ClassicGradientButton(
                        text = "เข้าสู่ระบบ",
                        loading = isLoading,
                        colors = listOf(Matcha, Viridian),
                        shadowColor = Viridian,
                        onClick = {
                            val cleanEmail = email.trim()
                            val cleanPass = password.trim()

                            when {
                                cleanEmail.isEmpty() -> message = "กรุณากรอกอีเมล"
                                cleanPass.isEmpty() -> message = "กรุณากรอกรหัสผ่าน"
                                else -> {
                                    isLoading = true
                                    message = ""

                                    auth.signInWithEmailAndPassword(cleanEmail, cleanPass)
                                        .addOnCompleteListener { task ->
                                            isLoading = false

                                            if (task.isSuccessful) {
                                                Toast.makeText(
                                                    context,
                                                    "เข้าสู่ระบบสำเร็จ!",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                onLoginSuccess()
                                            } else {
                                                message = task.exception
                                                    ?.localizedMessage
                                                    ?: "อีเมลหรือรหัสผ่านไม่ถูกต้อง"
                                            }
                                        }
                                }
                            }
                        }
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "ยังไม่มีบัญชี? ",
                    color = TextMuted,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )

                Text(
                    text = "สมัครสมาชิก",
                    color = Cyclamen,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.clickable { onRegisterClick() }
                )
            }

            // เว้นที่ให้พ้นหญ้าด้านล่าง
            Spacer(Modifier.height(120.dp))
        }

        // น้องแมวโผล่จากกอหญ้า (วางก่อนรูปหญ้า หญ้าจะบังครึ่งล่าง)
        CatFace(
            size = 76.dp,
            mood = CatMood.HAPPY,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 10.dp, bottom = 128.dp)
        )

        Image(
            painter = painterResource(id = R.drawable.flower),
            contentDescription = "Flowers and grass",
            contentScale = ContentScale.FillBounds,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(190.dp)
        )
    }
}