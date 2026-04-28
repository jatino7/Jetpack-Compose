package com.o7solutions.android_compose.Navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavHostController
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material3.Text
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.o7solutions.android_compose.BluetoothHid.TrackpadScreen
import com.o7solutions.android_compose.Topics.SpeechToTextAndTTS
import com.o7solutions.android_compose.Utils.DataStoreManager
import com.o7solutions.android_compose.View.CategoryProducts
import com.o7solutions.android_compose.View.LoginScreen


@RequiresApi(Build.VERSION_CODES.P)
@Composable
fun MainNavigation(navController: NavHostController) {


    NavHost(navController, startDestination = Dest.SPLASH) {

        composable(Dest.SPLASH) {
            SplashScreen(navController)
        }

        composable(Dest.LOGIN) {
            LoginScreen(navController)
        }

        composable(Dest.MAIN) {
            MainAppContainer(navController)
        }

        composable("category_products/{catId}/{catName}") { backStackEntry ->
            val catId = backStackEntry.arguments?.getString("catId")?.toInt() ?: 0
            val catName = backStackEntry.arguments?.getString("catName") ?: ""
//            CategoryProductsScreen(catId, catName)
            CategoryProducts(catId,catName)
        }

        composable(Dest.HID) {
            TrackpadScreen()

        }

        composable(Dest.TTS) {
            SpeechToTextAndTTS()
        }


    }
}
@Composable
fun SplashScreen(navController: NavHostController) {
    val context = LocalContext.current

    val dataStoreManager = remember { DataStoreManager(context) }

    LaunchedEffect(Unit) {
        dataStoreManager.accessToken.collect { token ->
            delay(2000)

//            val target = if (!token.isNullOrEmpty()) {
//                Dest.MAIN
//            } else {
//                Dest.LOGIN
//            }

            val target = Dest.NANO
            navController.navigate(target) {
                popUpTo(Dest.SPLASH) { inclusive = true }
            }
        }
    }

    // Your UI Code...
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF6200EE), Color(0xFF3700B3)))),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Platzi Fake Store", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            CircularProgressIndicator(color = Color.White)
        }
    }
}