//MainActivity
package com.example.myfinance

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import com.example.moneytrack.ui.AddTransactionScreen
import com.example.moneytrack.ui.CategoryScreen
import com.example.moneytrack.ui.theme.AppTypography
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MaterialTheme(typography = AppTypography) {
                MoneyMateApp()
            }
        }
    }
}

// ยิ่งเลขมาก ยิ่งเป็นหน้าที่ "ลึกกว่า" ใช้กำหนดทิศทางแอนิเมชัน
private fun screenDepth(screen: String): Int = when (screen) {
    "welcome", "login", "register" -> 0
    "home" -> 1
    "profile", "add_transaction", "history", "dashboard" -> 2
    "categories" -> 3
    else -> 0
}

@Composable
fun MoneyMateApp() {
    var currentScreen by rememberSaveable {
        mutableStateOf(
            if (FirebaseAuth.getInstance().currentUser != null) "home" else "welcome"
        )
    }

    BackHandler(enabled = currentScreen == "categories") {
        currentScreen = "add_transaction"
    }
    BackHandler(
        enabled = currentScreen == "add_transaction" ||
                currentScreen == "profile" ||
                currentScreen == "history" ||
                currentScreen == "dashboard"
    ) {
        currentScreen = "home"
    }

    AnimatedContent(
        targetState = currentScreen,
        modifier = Modifier
            .fillMaxSize()
            .background(HomeCreamBackground),
        transitionSpec = {
            val direction = if (screenDepth(targetState) > screenDepth(initialState)) 1 else -1
            (fadeIn(tween(300)) + slideInHorizontally(tween(300)) { it / 8 * direction })
                .togetherWith(
                    fadeOut(tween(200)) + slideOutHorizontally(tween(300)) { -it / 8 * direction }
                )
        },
        label = "screen"
    ) { screen ->
        when (screen) {
            // Welcome / Login / Register อยู่ใน Pager เดียวกัน เลื่อนซ้าย-ขวาได้
            "welcome" -> WelcomeScreen(
                initialPage = 0,
                onAuthSuccess = { currentScreen = "home" }
            )

            "login" -> WelcomeScreen(
                initialPage = 1,
                onAuthSuccess = { currentScreen = "home" }
            )

            "register" -> WelcomeScreen(
                initialPage = 2,
                onAuthSuccess = { currentScreen = "home" }
            )

            "home" -> HomeScreen(
                onProfileClick = { currentScreen = "profile" },
                onAddTransactionClick = { currentScreen = "add_transaction" },
                onHistoryClick = { currentScreen = "history" },
                onDashboardClick = { currentScreen = "dashboard" }
            )

            "history" -> HistoryScreen(
                onHomeClick = { currentScreen = "home" },
                onTransactionClick = { currentScreen = "add_transaction" },
                onProfileClick = { currentScreen = "profile" },
                onDashboardClick = { currentScreen = "dashboard" }
            )

            "dashboard" -> DashboardScreen(
                onHomeClick = { currentScreen = "home" },
                onTransactionClick = { currentScreen = "add_transaction" },
                onHistoryClick = { currentScreen = "history" },
                onProfileClick = { currentScreen = "profile" }
            )

            "profile" -> ProfileScreen(
                onBackToHome = { currentScreen = "home" },
                onLogout = { currentScreen = "login" }
            )

            "add_transaction" -> AddTransactionScreen(
                onBack = { currentScreen = "home" },
                onSaved = { currentScreen = "home" },
                onOpenCategories = { currentScreen = "categories" }
            )

            "categories" -> CategoryScreen(
                onBack = { currentScreen = "add_transaction" }
            )
        }
    }
}