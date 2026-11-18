package com.example.moneytrack.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myfinance.HomeCardWhite
import com.example.myfinance.HomeDarkText
import com.example.myfinance.HomeDeepBerry
import com.example.myfinance.HomeSubText

@Composable
fun ScreenHeader(title: String, subtitle: String, onBack: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(44.dp),
            shape = CircleShape,
            color = HomeCardWhite,
            shadowElevation = 5.dp,
            onClick = onBack
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = "‹",
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Medium,
                    color = HomeDeepBerry
                )
            }
        }

        Spacer(Modifier.width(14.dp))

        Column {
            Text(
                text = title,
                fontSize = 27.sp,
                fontWeight = FontWeight.SemiBold,
                color = HomeDarkText
            )
            Text(
                text = subtitle,
                fontSize = 13.sp,
                color = HomeSubText
            )
        }
    }
}