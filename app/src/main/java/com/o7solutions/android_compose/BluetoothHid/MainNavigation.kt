package com.o7solutions.android_compose.BluetoothHid

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

@Composable
fun MainNavigation(viewModel: BluetoothViewModel) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "scanner") {

        // Screen 1: The Scanner (Your existing code)
        composable("scanner") {
            ESP32ControlScreen(viewModel = viewModel,navController = navController)
        }

        // Screen 2: The Voice Control (The screen we built earlier)
        composable(
            route = "voice_control/{deviceAddress}",
            arguments = listOf(navArgument("deviceAddress") { type = NavType.StringType })
        ) { backStackEntry ->
            val address = backStackEntry.arguments?.getString("deviceAddress") ?: ""
            // Call the specialized Voice Screen that initializes its own socket
            VoiceControlScreen(viewModel,navController)
        }
    }
}