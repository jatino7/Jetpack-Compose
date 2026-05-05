package com.o7solutions.android_compose.BluetoothHid

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.io.OutputStream
import java.util.*

// Standard Serial Port Profile (SPP) UUID for ESP32
private val MY_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("MissingPermission")
@Composable
fun VoiceControlScreen(
    viewModel: BluetoothViewModel,
    navController: NavHostController
) {
    val context = LocalContext.current

    // --- OBSERVE SHARED STATE FROM VIEWMODEL ---
    val isConnected by viewModel.isConnected.collectAsState()
    val deviceName by viewModel.pairedDevice.collectAsState()

    var isListening by remember { mutableStateOf(false) }
    var lastHeard by remember { mutableStateOf("Waiting for command...") }

    // --- SPEECH RECOGNIZER SETUP ---
    val speechRecognizer = remember { SpeechRecognizer.createSpeechRecognizer(context) }
    val speechIntent = remember {
        Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        }
    }

    // --- SPEECH LISTENER LOGIC ---
    DisposableEffect(Unit) {
        val listener = object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) { isListening = true }
            override fun onEndOfSpeech() { isListening = false }
            override fun onError(error: Int) { isListening = false }
            override fun onResults(results: Bundle?) {
                val result = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.get(0) ?: ""
                val input = result.lowercase()
                lastHeard = result

                // Map voice to ESP32 commands
                when {
                    input.contains("forward") -> viewModel.sendMessage("F")
                    input.contains("back")    -> viewModel.sendMessage("B")
                    input.contains("left")    -> viewModel.sendMessage("L")
                    input.contains("right")   -> viewModel.sendMessage("R")
                    input.contains("stop")    -> viewModel.sendMessage("S")
                }
            }
            // ... other callbacks (onBeginningOfSpeech, etc.) can be empty
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        }
        speechRecognizer.setRecognitionListener(listener)
        onDispose { speechRecognizer.destroy() }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { if (it) speechRecognizer.startListening(speechIntent) }

    // --- UI ---
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Voice Control: $deviceName") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Status Indicator
            StatusBadge(isConnected)

            Spacer(Modifier.height(40.dp))

            // Last Command Display
            Text("Last Heard:", style = MaterialTheme.typography.labelLarge)
            Text(
                text = "\"$lastHeard\"",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(60.dp))

            // Big Voice Button
            Button(
                onClick = { permissionLauncher.launch(Manifest.permission.RECORD_AUDIO) },
                modifier = Modifier.size(160.dp),
                shape = CircleShape,
                enabled = isConnected,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isListening) Color.Red else MaterialTheme.colorScheme.primary
                )
            ) {
                if (isListening) {
                    Text("LISTENING")
                } else {
                    Icon(Icons.Default.Mic, contentDescription = null, modifier = Modifier.size(48.dp))
                }
            }

            if (!isConnected) {
                Text(
                    "Bluetooth Disconnected",
                    color = Color.Red,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
        }
    }
}

@Composable
fun StatusBadge(connected: Boolean) {
    Surface(
        color = if (connected) Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
        shape = RoundedCornerShape(16.dp)
    ) {
        Text(
            text = if (connected) "● DEVICE CONNECTED" else "○ DEVICE DISCONNECTED",
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            color = if (connected) Color(0xFF2E7D32) else Color(0xFFC62828),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.ExtraBold
        )
    }
}