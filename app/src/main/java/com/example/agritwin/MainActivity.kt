package com.example.agritwin


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.agritwin.ai.PlantDiseaseService
import com.example.agritwin.navigation.Navigation
import com.example.agritwin.ui.components.BottomNavigationBar
import com.example.agritwin.ui.theme.AgriTwinTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        PlantDiseaseService.initializeModel(this)

        setContent {
            AgriTwinTheme {
                AgriTwinApp()
            }
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        PlantDiseaseService.releaseModel()
    }
}

@Composable
fun AgriTwinApp() {
    val navController = rememberNavController()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            BottomNavigationBar(navController = navController)
        }
    ) { paddingValues ->
        Navigation(
            navController = navController
        )
    }
}