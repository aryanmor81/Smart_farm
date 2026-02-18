package com.example.agritwin.navigation


sealed class Screen(val route: String) {
    object Home : Screen(route = "home")
    object MapTwin : Screen(route = "map")
    object Insights : Screen(route = "insights")
    object Settings : Screen(route = "settings")
}