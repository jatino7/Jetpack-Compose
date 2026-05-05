package com.o7solutions.android_compose.Topics

import android.Manifest
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.FragmentActivity
import com.zegocloud.uikit.prebuilt.call.ZegoUIKitPrebuiltCallConfig
import com.zegocloud.uikit.prebuilt.call.ZegoUIKitPrebuiltCallFragment

@Composable
fun Navigation(appID: Long, appSign: String) {
    val userID = remember { "user_${(100..999).random()}" }
    val userName = remember { "User_$userID" }
    var activeCallId by remember { mutableStateOf<String?>(null) }

    // Track permission state to prevent white screen from missing hardware access
    var permissionsGranted by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        permissionsGranted = permissions.values.all { it }
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        if (activeCallId == null || !permissionsGranted) {
            LobbyScreen(onJoinCall = { id ->
                activeCallId = id
                permissionLauncher.launch(
                    arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
                )
            })
        } else {
            BackHandler {
                activeCallId = null
                permissionsGranted = false
            }

            VideoCallScreen(
                appID = appID,
                appSign = appSign,
                callID = activeCallId!!,
                userID = userID,
                userName = userName
            )
        }
    }
}

@Composable
fun VideoCallScreen(
    appID: Long,
    appSign: String,
    callID: String,
    userID: String,
    userName: String
) {
    val context = LocalContext.current
    val fragmentManager = (context as? FragmentActivity)?.supportFragmentManager

    // CRITICAL: Stable ID prevents the fragment from losing its host container
    val containerId = remember { View.generateViewId() }

    AndroidView(
        factory = { ctx ->
            FrameLayout(ctx).apply {
                id = containerId
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            }
        },
        update = { _ ->
            // Check if fragment is already attached to avoid "Fragment already added" error
            val existingFragment = fragmentManager?.findFragmentByTag("ZEGO_CALL_TAG")
            if (existingFragment == null) {
                val config = ZegoUIKitPrebuiltCallConfig.oneOnOneVideoCall()
                val fragment = ZegoUIKitPrebuiltCallFragment.newInstance(
                    appID, appSign, userID, userName, callID, config
                )

                fragmentManager?.beginTransaction()
                    ?.replace(containerId, fragment, "ZEGO_CALL_TAG")
                    ?.commitNowAllowingStateLoss()
            }
        },
        modifier = Modifier.fillMaxSize()
    )

    // Cleanup when leaving the Composable
    DisposableEffect(Unit) {
        onDispose {
            fragmentManager?.findFragmentByTag("ZEGO_CALL_TAG")?.let {
                fragmentManager.beginTransaction().remove(it).commitAllowingStateLoss()
            }
        }
    }
}

@Composable
fun LobbyScreen(onJoinCall: (String) -> Unit) {
    var textState by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Video Calling", style = MaterialTheme.typography.headlineLarge)
        Spacer(modifier = Modifier.height(20.dp))
        OutlinedTextField(
            value = textState,
            onValueChange = { textState = it },
            label = { Text("Call ID") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(20.dp))
        Button(
            onClick = { if (textState.isNotBlank()) onJoinCall(textState) },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(10.dp)
        ) {
            Text("Join Class")
        }
    }
}