//ProfileScreen
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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.moneytrack.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest

@Composable
fun ProfileScreen(
    onBackToHome: () -> Unit,
    onLogout: () -> Unit
) {
    var notificationEnabled by remember { mutableStateOf(true) }

    val auth = remember { FirebaseAuth.getInstance() }
    val user = auth.currentUser
    val context = LocalContext.current

    var userName by remember(user?.uid) {
        mutableStateOf(user?.displayName ?: "User")
    }

    val userEmail = user?.email ?: "ไม่มีอีเมล"

    var editDialogVisible by remember { mutableStateOf(false) }
    var logoutDialogVisible by remember { mutableStateOf(false) }

    CuteBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(18.dp))

            CuteScreenHeader(
                title = "Profile",
                subtitle = "จัดการบัญชีของคุณ",
                onBack = onBackToHome
            )

            Spacer(Modifier.height(18.dp))

            CatSays(message = "ขอบคุณที่ดูแลเงินกับเรานะ เมี๊ยว ♡")

            Spacer(Modifier.height(16.dp))

            // Profile card
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 25.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(contentAlignment = Alignment.BottomEnd) {
                        Box(
                            modifier = Modifier
                                .size(104.dp)
                                .shadow(10.dp, CircleShape, spotColor = HomeCyclamen)
                                .background(
                                    Brush.linearGradient(listOf(HomeCyclamen, HomeDeepBerry)),
                                    CircleShape
                                )
                                .padding(4.dp),
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
                                        .padding(4.dp)
                                        .clip(CircleShape)
                                )
                            }
                        }

                        CatFace(
                            size = 38.dp,
                            mood = CatMood.WINK,
                            modifier = Modifier.offset(x = 8.dp, y = 6.dp)
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    Text(
                        text = userName,
                        fontSize = 25.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = HomeDarkText
                    )

                    Spacer(Modifier.height(4.dp))

                    Text(text = userEmail, fontSize = 14.sp, color = HomeSubText)

                    Spacer(Modifier.height(21.dp))

                    Button(
                        onClick = { editDialogVisible = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(27.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = HomeDeepBerry)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_edit),
                            contentDescription = "Edit Profile",
                            modifier = Modifier.size(21.dp),
                            tint = Color.White
                        )

                        Spacer(Modifier.width(9.dp))

                        Text(
                            text = "แก้ไขโปรไฟล์",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(Modifier.height(25.dp))

            Text(
                text = "Settings",
                fontSize = 19.sp,
                fontWeight = FontWeight.SemiBold,
                color = HomeDarkText
            )

            Spacer(Modifier.height(12.dp))

            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                elevation = 6.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier.size(46.dp),
                        shape = CircleShape,
                        color = HomeFairyTale
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_notifications),
                                contentDescription = "Notifications",
                                modifier = Modifier.size(22.dp),
                                tint = HomeDeepBerry
                            )
                        }
                    }

                    Spacer(Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "การแจ้งเตือน",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = HomeDarkText
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = "เปิดใช้งานการแจ้งเตือนของแอป",
                            fontSize = 12.sp,
                            color = HomeSubText
                        )
                    }

                    Switch(
                        checked = notificationEnabled,
                        onCheckedChange = { notificationEnabled = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = HomeDeepBerry
                        )
                    )
                }
            }

            Spacer(Modifier.height(25.dp))

            Text(
                text = "Account",
                fontSize = 19.sp,
                fontWeight = FontWeight.SemiBold,
                color = HomeDarkText
            )

            Spacer(Modifier.height(12.dp))

            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                elevation = 6.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { logoutDialogVisible = true }
                        .padding(horizontal = 16.dp, vertical = 15.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier.size(46.dp),
                        shape = CircleShape,
                        color = Color(0xFFFFE2E7)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_logout),
                                contentDescription = "Logout",
                                modifier = Modifier.size(22.dp),
                                tint = HomeCyclamen
                            )
                        }
                    }

                    Spacer(Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "ออกจากระบบ",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = HomeDarkText
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = "ออกจากบัญชีผู้ใช้",
                            fontSize = 12.sp,
                            color = HomeSubText
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            Text(
                text = "Version 1.0.0",
                fontSize = 12.sp,
                color = HomeSubText.copy(alpha = 0.7f),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(Modifier.height(24.dp))
        }
    }

    // Edit Profile Dialog
    if (editDialogVisible) {
        var editedName by remember(userName) { mutableStateOf(userName) }

        AlertDialog(
            onDismissRequest = { editDialogVisible = false },
            containerColor = HomeCardWhite,
            shape = RoundedCornerShape(24.dp),
            title = { Text("แก้ไขโปรไฟล์") },
            text = {
                Column {
                    OutlinedTextField(
                        value = editedName,
                        onValueChange = { editedName = it },
                        label = { Text("ชื่อผู้ใช้") },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp)
                    )

                    Spacer(Modifier.height(8.dp))

                    Text(text = "อีเมล: $userEmail", fontSize = 13.sp)
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val newName = editedName.trim()

                        if (newName.isBlank()) {
                            Toast.makeText(context, "กรุณากรอกชื่อผู้ใช้", Toast.LENGTH_SHORT).show()
                        } else if (user == null) {
                            Toast.makeText(context, "กรุณาเข้าสู่ระบบใหม่", Toast.LENGTH_SHORT).show()
                        } else {
                            val updates = UserProfileChangeRequest.Builder()
                                .setDisplayName(newName)
                                .build()

                            user.updateProfile(updates)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        userName = newName
                                        editDialogVisible = false

                                        Toast.makeText(
                                            context,
                                            "บันทึกโปรไฟล์สำเร็จ",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    } else {
                                        Toast.makeText(
                                            context,
                                            "บันทึกไม่สำเร็จ กรุณาลองอีกครั้ง",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                        }
                    }
                ) {
                    Text("บันทึก", color = HomeDeepBerry)
                }
            },
            dismissButton = {
                TextButton(onClick = { editDialogVisible = false }) {
                    Text("ยกเลิก", color = HomeSubText)
                }
            }
        )
    }

    // Logout Confirmation Dialog
    if (logoutDialogVisible) {
        AlertDialog(
            onDismissRequest = { logoutDialogVisible = false },
            containerColor = HomeCardWhite,
            shape = RoundedCornerShape(24.dp),
            title = { Text("ออกจากระบบ") },
            text = { Text("คุณต้องการออกจากระบบใช่ไหม?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        auth.signOut()
                        logoutDialogVisible = false
                        onLogout()
                    }
                ) {
                    Text("ออกจากระบบ", color = HomeDeepBerry)
                }
            },
            dismissButton = {
                TextButton(onClick = { logoutDialogVisible = false }) {
                    Text("ยกเลิก", color = HomeSubText)
                }
            }
        )
    }
}