package com.o7solutions.android_compose

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.o7solutions.android_compose.Drawer.NavDrawerWithNavigation
import com.o7solutions.android_compose.Navigation.MainNavigation
import com.o7solutions.android_compose.ui.theme.Android_ComposeTheme

val LocalNavController = staticCompositionLocalOf<NavHostController> {
    error("No NavController found!")
}

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Android_ComposeTheme {


                val navController = rememberNavController()
                CompositionLocalProvider(LocalNavController provides navController) {
                    MainNavigation(navController)
//                    NavDrawerWithNavigation()
                }
            }
        }
    }
}