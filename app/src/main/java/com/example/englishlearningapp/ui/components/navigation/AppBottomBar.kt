package com.example.englishlearningapp.ui.components.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.englishlearningapp.ui.navigation.Routes

data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val route: String
)

@Composable
fun AppBottomBar(navController: NavController) {
    val items = listOf(
        BottomNavItem("Trang chủ", Icons.Default.Home, Routes.HOME),
        BottomNavItem("Luyện tập", Icons.Default.Edit, "practice/general"),
        BottomNavItem("Kiểm tra", Icons.Default.Quiz, "quiz_part_selection/general"),
        BottomNavItem("Tiến trình", Icons.AutoMirrored.Filled.TrendingUp, Routes.PROGRESS)
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Các màn hình chính sẽ hiển thị BottomBar
    val mainRoutes = listOf(
        Routes.HOME,
        Routes.PROGRESS,
        "practice/general",
        "quiz_part_selection/general"
    )

    if (currentRoute in mainRoutes) {
        NavigationBar(
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            items.forEach { item ->
                val selected = currentRoute == item.route
                NavigationBarItem(
                    selected = selected,
                    onClick = {
                        if (currentRoute != item.route) {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    },
                    label = {
                        Text(
                            text = item.label,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    icon = {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.label
                        )
                    }
                )
            }
        }
    }
}
