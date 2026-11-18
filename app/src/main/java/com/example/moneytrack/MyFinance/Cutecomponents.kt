//CuteComponents
package com.example.myfinance

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.moneytrack.R

val CuteOutline = Color(0xFF7A2548)
private val CutePink = Color(0xFFF9B7CC)

/* ---------------------------------------------------------------
 * โลโก้แอป (ใช้ซ้ำได้ทุกหน้า)
 * --------------------------------------------------------------- */
@Composable
fun AppLogo(modifier: Modifier = Modifier, size: Dp = 44.dp) {
    Image(
        painter = painterResource(id = R.drawable.moneytrack_logo),
        contentDescription = "MoneyTrack logo",
        contentScale = ContentScale.Crop,
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(size * 0.28f))
    )
}

fun categoryEmoji(name: String): String = when {
    name.contains("อาหาร") || name.contains("เครื่องดื่ม") -> "🍜"
    name.contains("น้ำ") || name.contains("ไฟ") -> "💡"
    name.contains("จำเป็น") -> "🏠"
    name.contains("เดินทาง") -> "🚗"
    name.contains("ช้อป") -> "🛍️"
    name.contains("เรียน") -> "📚"
    name.contains("บันเทิง") -> "🎬"
    name.contains("เงินเดือน") -> "💰"
    else -> "🏷️"
}

/* ---------------------------------------------------------------
 * พื้นหลังชมพูนุ่ม ๆ มีก้อนเมฆและประกาย
 * --------------------------------------------------------------- */
private fun DrawScope.drawCloud(origin: Offset, scale: Float, alpha: Float) {
    val c = Color.White.copy(alpha = alpha)
    val r = 26.dp.toPx() * scale
    drawCircle(c, r, origin + Offset(r, 0f))
    drawCircle(c, r * 1.35f, origin + Offset(r * 2.4f, -r * 0.35f))
    drawCircle(c, r * 0.95f, origin + Offset(r * 3.6f, r * 0.05f))
    drawRoundRect(
        color = c,
        topLeft = origin + Offset(0f, 0f),
        size = Size(r * 4.6f, r * 0.95f),
        cornerRadius = CornerRadius(r * 0.45f)
    )
}

private fun DrawScope.drawSparkle(center: Offset, r: Float, color: Color) {
    val p = Path().apply {
        moveTo(center.x, center.y - r)
        quadraticBezierTo(center.x, center.y, center.x + r, center.y)
        quadraticBezierTo(center.x, center.y, center.x, center.y + r)
        quadraticBezierTo(center.x, center.y, center.x - r, center.y)
        quadraticBezierTo(center.x, center.y, center.x, center.y - r)
        close()
    }
    drawPath(p, color)
}

@Composable
fun CuteBackground(
    modifier: Modifier = Modifier,
    gradient: List<Color> = listOf(Color(0xFFFFDDEA), Color(0xFFFFEFF5), HomeCreamBackground),
    sparkleColor: Color = HomeCyclamen.copy(alpha = 0.40f),
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    gradient
                )
            )
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            drawCloud(Offset(w * 0.03f, h * 0.06f), 0.9f, 0.60f)
            drawCloud(Offset(w * 0.60f, h * 0.14f), 0.7f, 0.45f)
            drawCloud(Offset(-w * 0.04f, h * 0.60f), 0.8f, 0.35f)
            drawCloud(Offset(w * 0.62f, h * 0.84f), 1.0f, 0.50f)

            val spark = sparkleColor
            drawSparkle(Offset(w * 0.88f, h * 0.07f), 9.dp.toPx(), spark)
            drawSparkle(Offset(w * 0.93f, h * 0.11f), 5.dp.toPx(), spark)
            drawSparkle(Offset(w * 0.07f, h * 0.36f), 7.dp.toPx(), spark)
            drawSparkle(Offset(w * 0.94f, h * 0.55f), 8.dp.toPx(), spark)
            drawSparkle(Offset(w * 0.10f, h * 0.80f), 6.dp.toPx(), spark)
        }
        content()
    }
}

/* ---------------------------------------------------------------
 * การ์ดกระจกขาวโปร่ง ขอบขาว เงาชมพู
 * --------------------------------------------------------------- */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(28.dp),
    elevation: Dp = 10.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .shadow(
                elevation,
                shape,
                ambientColor = HomeCyclamen.copy(alpha = 0.25f),
                spotColor = HomeCyclamen.copy(alpha = 0.35f)
            )
            .clip(shape)
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color.White.copy(alpha = 0.95f),
                        Color(0xFFFFF5F9).copy(alpha = 0.90f)
                    )
                )
            )
            .border(1.5.dp, Color.White, shape),
        content = content
    )
}

/* ---------------------------------------------------------------
 * น้องแมว (วาดด้วย Compose ตามโลโก้: ขนขาว หูชมพู แก้มชมพู)
 * --------------------------------------------------------------- */
enum class CatMood { HAPPY, WINK, SLEEPY }

@Composable
fun CatFace(
    modifier: Modifier = Modifier,
    size: Dp = 64.dp,
    mood: CatMood = CatMood.HAPPY
) {
    Canvas(modifier = modifier.size(size)) {
        val s = this.size.width
        val line = s * 0.028f
        val stroke = Stroke(width = line, cap = StrokeCap.Round)

        // ears
        val leftEar = Path().apply {
            moveTo(s * 0.12f, s * 0.50f)
            lineTo(s * 0.17f, s * 0.06f)
            lineTo(s * 0.46f, s * 0.26f)
            close()
        }
        val rightEar = Path().apply {
            moveTo(s * 0.88f, s * 0.50f)
            lineTo(s * 0.83f, s * 0.06f)
            lineTo(s * 0.54f, s * 0.26f)
            close()
        }
        drawPath(leftEar, Color.White)
        drawPath(rightEar, Color.White)
        drawPath(leftEar, CuteOutline, style = stroke)
        drawPath(rightEar, CuteOutline, style = stroke)

        val leftInner = Path().apply {
            moveTo(s * 0.19f, s * 0.40f)
            lineTo(s * 0.21f, s * 0.16f)
            lineTo(s * 0.38f, s * 0.28f)
            close()
        }
        val rightInner = Path().apply {
            moveTo(s * 0.81f, s * 0.40f)
            lineTo(s * 0.79f, s * 0.16f)
            lineTo(s * 0.62f, s * 0.28f)
            close()
        }
        drawPath(leftInner, CutePink)
        drawPath(rightInner, CutePink)

        // head
        val headTopLeft = Offset(s * 0.04f, s * 0.20f)
        val headSize = Size(s * 0.92f, s * 0.74f)
        drawOval(Color.White, headTopLeft, headSize)
        drawOval(CuteOutline, headTopLeft, headSize, style = stroke)

        // blush
        drawOval(CutePink.copy(alpha = 0.75f), Offset(s * 0.14f, s * 0.60f), Size(s * 0.18f, s * 0.10f))
        drawOval(CutePink.copy(alpha = 0.75f), Offset(s * 0.68f, s * 0.60f), Size(s * 0.18f, s * 0.10f))

        // eyes
        fun DrawScope.dot(cx: Float, cy: Float) {
            drawCircle(CuteOutline, s * 0.050f, Offset(cx, cy))
            drawCircle(Color.White, s * 0.016f, Offset(cx + s * 0.015f, cy - s * 0.017f))
        }

        fun DrawScope.happyArc(cx: Float, cy: Float) {
            val p = Path().apply {
                moveTo(cx - s * 0.07f, cy + s * 0.02f)
                quadraticBezierTo(cx, cy - s * 0.08f, cx + s * 0.07f, cy + s * 0.02f)
            }
            drawPath(p, CuteOutline, style = stroke)
        }

        fun DrawScope.sleepyArc(cx: Float, cy: Float) {
            val p = Path().apply {
                moveTo(cx - s * 0.07f, cy - s * 0.01f)
                quadraticBezierTo(cx, cy + s * 0.08f, cx + s * 0.07f, cy - s * 0.01f)
            }
            drawPath(p, CuteOutline, style = stroke)
        }

        when (mood) {
            CatMood.HAPPY -> {
                dot(s * 0.30f, s * 0.52f)
                dot(s * 0.70f, s * 0.52f)
            }
            CatMood.WINK -> {
                happyArc(s * 0.30f, s * 0.52f)
                dot(s * 0.70f, s * 0.52f)
            }
            CatMood.SLEEPY -> {
                sleepyArc(s * 0.30f, s * 0.52f)
                sleepyArc(s * 0.70f, s * 0.52f)
            }
        }

        // nose + mouth
        drawOval(CutePink, Offset(s * 0.46f, s * 0.57f), Size(s * 0.08f, s * 0.05f))
        val mouth = Path().apply {
            moveTo(s * 0.41f, s * 0.65f)
            quadraticBezierTo(s * 0.455f, s * 0.73f, s * 0.50f, s * 0.65f)
            quadraticBezierTo(s * 0.545f, s * 0.73f, s * 0.59f, s * 0.65f)
        }
        drawPath(mouth, CuteOutline, style = stroke)

        // whiskers
        drawLine(CuteOutline, Offset(s * 0.13f, s * 0.56f), Offset(s * 0.01f, s * 0.53f), line, StrokeCap.Round)
        drawLine(CuteOutline, Offset(s * 0.13f, s * 0.64f), Offset(s * 0.01f, s * 0.67f), line, StrokeCap.Round)
        drawLine(CuteOutline, Offset(s * 0.87f, s * 0.56f), Offset(s * 0.99f, s * 0.53f), line, StrokeCap.Round)
        drawLine(CuteOutline, Offset(s * 0.87f, s * 0.64f), Offset(s * 0.99f, s * 0.67f), line, StrokeCap.Round)
    }
}

/** น้องแมวพร้อมบับเบิลพูด */
@Composable
fun CatSays(
    message: String,
    modifier: Modifier = Modifier,
    mood: CatMood = CatMood.HAPPY
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CatFace(size = 56.dp, mood = mood)
        Spacer(Modifier.width(8.dp))
        Surface(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(
                topStart = 6.dp, topEnd = 20.dp, bottomEnd = 20.dp, bottomStart = 20.dp
            ),
            color = Color.White.copy(alpha = 0.92f),
            shadowElevation = 4.dp,
            border = BorderStroke(1.dp, HomeFairyTale)
        ) {
            Text(
                text = message,
                fontSize = 13.sp,
                color = HomeDarkText,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
            )
        }
    }
}

/* ---------------------------------------------------------------
 * ปุ่มหลัก ไล่สีชมพู ทรงแคปซูล
 * --------------------------------------------------------------- */
@Composable
fun CutePrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    loading: Boolean = false
) {
    val shape = RoundedCornerShape(28.dp)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .shadow(10.dp, shape, spotColor = HomeCyclamen)
            .clip(shape)
            .background(Brush.horizontalGradient(listOf(HomeCyclamen, HomeDeepBerry)))
            .clickable(enabled = !loading, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (loading) {
            CircularProgressIndicator(
                color = Color.White,
                strokeWidth = 3.dp,
                modifier = Modifier.size(24.dp)
            )
        } else {
            Text(
                text = text,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

/* ---------------------------------------------------------------
 * ช่องกรอกข้อมูลสไตล์น่ารัก (รองรับรหัสผ่าน)
 * --------------------------------------------------------------- */
@Composable
fun CuteField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    leading: ImageVector,
    modifier: Modifier = Modifier,
    isPassword: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    var visible by remember { mutableStateOf(false) }

    val trailing: (@Composable () -> Unit)? = if (isPassword) {
        {
            IconButton(onClick = { visible = !visible }) {
                Icon(
                    imageVector = if (visible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                    contentDescription = null,
                    tint = HomeSubText
                )
            }
        }
    } else null

    Column(modifier = modifier) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = HomeDarkText
        )
        Spacer(Modifier.height(4.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(placeholder, color = HomeSubText, fontSize = 14.sp) },
            leadingIcon = { Icon(leading, contentDescription = null, tint = HomeCyclamen) },
            trailingIcon = trailing,
            visualTransformation = if (isPassword && !visible)
                PasswordVisualTransformation() else VisualTransformation.None,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            singleLine = true,
            shape = RoundedCornerShape(20.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = HomeDarkText,
                unfocusedTextColor = HomeDarkText,
                focusedBorderColor = HomeCyclamen,
                unfocusedBorderColor = HomeFairyTale,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color(0xFFFFF8FB)
            )
        )
    }
}

/* ---------------------------------------------------------------
 * หัวข้อหน้าแบบมีปุ่มย้อนกลับและโลโก้
 * --------------------------------------------------------------- */
@Composable
fun CuteScreenHeader(
    title: String,
    subtitle: String,
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (onBack != null) {
            Surface(
                modifier = Modifier.size(44.dp),
                shape = CircleShape,
                color = HomeCardWhite,
                shadowElevation = 5.dp,
                onClick = onBack
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("‹", fontSize = 32.sp, color = HomeDeepBerry)
                }
            }
            Spacer(Modifier.width(14.dp))
        }
        Column(Modifier.weight(1f)) {
            Text(title, fontSize = 26.sp, fontWeight = FontWeight.SemiBold, color = HomeDarkText)
            Text(subtitle, fontSize = 13.sp, color = HomeSubText)
        }
        AppLogo(size = 46.dp)
    }
}