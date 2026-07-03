package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.GameViewModel
import com.example.viewmodel.Screen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val gameViewModel: GameViewModel = viewModel()
                MainAppContent(viewModel = gameViewModel)
            }
        }
    }
}

@Composable
fun MainAppContent(viewModel: GameViewModel) {
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        // Render current screen based on active State
        val unusedPadding = innerPadding // Handled internally in edge-to-edge screens
        when (viewModel.currentScreen) {
            Screen.Login -> AuthScreens(viewModel)
            Screen.SlotSelection -> SlotSelectionScreen(viewModel)
            Screen.FranchiseCreation -> FranchiseCreationScreen(viewModel)
            Screen.Dashboard -> DashboardScreen(viewModel)
            Screen.MatchDay -> MatchDayScreen(viewModel)
            else -> AuthScreens(viewModel)
        }
    }
}
