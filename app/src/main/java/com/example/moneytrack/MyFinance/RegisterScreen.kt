//RegisterScreen  (หน้าเดิม + น้องแมว + ชื่อ MoneyTrack แทรกชมพู)
package com.example.myfinance

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.moneytrack.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest

@Composable
fun RegisterScreen(
    onLoginClick: () -> Unit,
    onRegisterSuccess: () -> Unit = {}
) {
    val context = LocalContext.current
    val auth = remember { FirebaseAuth.getInstance() }

    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var isLoading by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
    ) {
        // รูปซากุระพื้นหลังโปร่งใส
        Image(
            painter = painterResource(id = R.drawable.sakura),
            contentDescription = "Sakura flower decoration",
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.fillMaxSize()
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(start = 24.dp, end = 24.dp, top = 28.dp, bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // ไอคอนผู้ใช้ + น้องแมวโผล่มุม
            Box(contentAlignment = Alignment.BottomEnd) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .shadow(8.dp, CircleShape, spotColor = FairyTale)
                        .background(
                            Brush.radialGradient(listOf(FairyTale, AppBackground)),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "User icon",
                        tint = QuinacridoneMagenta,
                        modifier = Modifier.size(40.dp)
                    )
                }

                CatFace(
                    size = 40.dp,
                    mood = CatMood.HAPPY,
                    modifier = Modifier.offset(x = 22.dp, y = 6.dp)
                )
            }

            Spacer(Modifier.height(10.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "สร้างบัญชี ",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextDarkPrimary,
                    textAlign = TextAlign.Center
                )
                MoneyTrackTitle(fontSize = 24.sp)
            }

            Text(
                text = "เริ่มต้นจัดการการเงินของคุณ ♡",
                fontSize = 14.sp,
                color = TextMuted,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(Modifier.height(18.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(6.dp, RoundedCornerShape(24.dp), spotColor = FairyTale),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    ClassicField(
                        label = "ชื่อผู้ใช้",
                        value = username,
                        onValueChange = {
                            username = it
                            message = ""
                        },
                        placeholder = "กรอกชื่อผู้ใช้",
                        leading = Icons.Default.Person,
                        accent = Cyclamen
                    )

                    Spacer(Modifier.height(10.dp))

                    ClassicField(
                        label = "อีเมล",
                        value = email,
                        onValueChange = {
                            email = it
                            message = ""
                        },
                        placeholder = "กรอกอีเมลของคุณ",
                        leading = Icons.Default.Email,
                        accent = Cyclamen
                    )

                    Spacer(Modifier.height(10.dp))

                    ClassicField(
                        label = "รหัสผ่าน (อย่างน้อย 6 ตัว)",
                        value = password,
                        onValueChange = {
                            password = it
                            message = ""
                        },
                        placeholder = "ตั้งรหัสผ่าน",
                        leading = Icons.Default.Lock,
                        accent = Cyclamen,
                        isPassword = true
                    )

                    Spacer(Modifier.height(10.dp))

                    ClassicField(
                        label = "ยืนยันรหัสผ่าน",
                        value = confirmPassword,
                        onValueChange = {
                            confirmPassword = it
                            message = ""
                        },
                        placeholder = "กรอกรหัสผ่านอีกครั้ง",
                        leading = Icons.Default.Lock,
                        accent = Cyclamen,
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

                    Spacer(Modifier.height(16.dp))

                    ClassicGradientButton(
                        text = "สมัครสมาชิก",
                        loading = isLoading,
                        colors = listOf(Cyclamen, QuinacridoneMagenta),
                        shadowColor = Cyclamen,
                        onClick = {
                            val cleanUser = username.trim()
                            val cleanEmail = email.trim()
                            val cleanPass = password.trim()
                            val cleanConfirm = confirmPassword.trim()

                            when {
                                cleanUser.isEmpty() -> message = "กรุณากรอกชื่อผู้ใช้"
                                cleanEmail.isEmpty() -> message = "กรุณากรอกอีเมล"
                                cleanPass.length < 6 -> message = "รหัสผ่านต้องมีอย่างน้อย 6 ตัวอักษร"
                                cleanPass != cleanConfirm -> message = "รหัสผ่านและยืนยันรหัสผ่านไม่ตรงกัน"
                                else -> {
                                    isLoading = true
                                    message = ""

                                    auth.createUserWithEmailAndPassword(cleanEmail, cleanPass)
                                        .addOnCompleteListener { task ->
                                            if (task.isSuccessful) {
                                                val user = auth.currentUser

                                                val profileUpdates = UserProfileChangeRequest
                                                    .Builder()
                                                    .setDisplayName(cleanUser)
                                                    .build()

                                                if (user == null) {
                                                    isLoading = false
                                                    message = "ไม่สามารถสร้างข้อมูลผู้ใช้ได้"
                                                } else {
                                                    user.updateProfile(profileUpdates)
                                                        .addOnCompleteListener {
                                                            isLoading = false

                                                            Toast.makeText(
                                                                context,
                                                                "สมัครสมาชิกสำเร็จ!",
                                                                Toast.LENGTH_SHORT
                                                            ).show()

                                                            onRegisterSuccess()
                                                        }
                                                }
                                            } else {
                                                isLoading = false
                                                message = task.exception
                                                    ?.localizedMessage
                                                    ?: "สมัครสมาชิกไม่สำเร็จ"
                                            }
                                        }
                                }
                            }
                        }
                    )
                }
            }

            Spacer(Modifier.height(18.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "มีบัญชีอยู่แล้ว? ",
                    color = TextMuted,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )

                Text(
                    text = "เข้าสู่ระบบ",
                    color = Viridian,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.clickable { onLoginClick() }
                )
            }
        }
    }
}