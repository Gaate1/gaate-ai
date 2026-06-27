package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.*
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        val viewModel: GaateViewModel = viewModel()
        val currentScreen by viewModel.currentScreen.collectAsState()

        when (currentScreen) {
          is Screen.Splash -> SplashScreen()
          is Screen.PinLogin -> PinLoginScreen(viewModel = viewModel)
          is Screen.MainDashboard -> MainDashboardScreen(viewModel = viewModel)
        }
      }
    }
  }
}
