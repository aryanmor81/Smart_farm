package com.example.agritwin.navigation


import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.agritwin.ui.screens.HomeScreen
import com.example.agritwin.ui.screens.MapScreen
import com.example.agritwin.ui.screens.InsightsScreen

@Composable
fun Navigation(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(route = Screen.Home.route) {
            HomeScreen()
        }
        composable(route = Screen.MapTwin.route) {
            MapScreen()
        }
        composable(route = Screen.Insights.route) {
            InsightsScreen()
        }
    }
}