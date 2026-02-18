package com.example.agritwin.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.agritwin.navigation.Screen
import com.example.agritwin.ui.theme.Green600
import com.example.agritwin.ui.theme.Neutral500
import com.example.agritwin.ui.theme.Neutral50

data class BottomNavItem(
    val screen: Screen,
    val label: String,
    val icon: ImageVector
)

@Composable
fun BottomNavigationBar(navController: NavController) {
    val items = listOf(
        BottomNavItem(Screen.Home, "Home", Icons.Default.Home),
        BottomNavItem(Screen.MapTwin, "Map", Icons.Default.Map),
        BottomNavItem(Screen.Insights, "Insights", Icons.Default.Analytics),
        BottomNavItem(Screen.Settings, "Settings", Icons.Default.Settings)
    )

    val currentBackStackEntry = navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry.value?.destination?.route

    NavigationBar(
        containerColor = Neutral50,
        tonalElevation = 8.dp
    ) {
        items.forEach { item ->
            val isSelected = currentRoute == item.screen.route

            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label,
                        tint = if (isSelected) Green600 else Neutral500
                    )
                },
                label = {
                    Text(
                        text = item.label,
                        color = if (isSelected) Green600 else Neutral500
                    )
                },
                selected = isSelected,
                onClick = {
                    if (currentRoute != item.screen.route) {
                        navController.navigate(item.screen.route) {
                            popUpTo(navController.graph.startDestinationId)
                            launchSingleTop = true
                        }
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Green600,
                    selectedTextColor = Green600,
                    unselectedIconColor = Neutral500,
                    unselectedTextColor = Neutral500,
                    indicatorColor = Green600.copy(alpha = 0.1f)
                )
            )
        }
    }
}