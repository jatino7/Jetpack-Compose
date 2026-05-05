package com.o7solutions.android_compose

import android.Manifest
import android.bluetooth.BluetoothManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.o7solutions.android_compose.BluetoothHid.BluetoothController
import com.o7solutions.android_compose.BluetoothHid.BluetoothViewModel
import com.o7solutions.android_compose.BluetoothHid.ESP32ControlScreen
import com.o7solutions.android_compose.BluetoothHid.MainNavigation
import com.o7solutions.android_compose.BluetoothHid.VoiceControlScreen

class MainActivity : ComponentActivity() {

    private val bluetoothController by lazy {
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        BluetoothController(applicationContext, bluetoothManager.adapter)
    }

    val viewModel by viewModels<BluetoothViewModel> {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return BluetoothViewModel(bluetoothController) as T
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val permissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            // You can check if permissions were granted here if needed
            val isScanGranted = permissions[Manifest.permission.BLUETOOTH_SCAN] ?: false
            val isConnectGranted = permissions[Manifest.permission.BLUETOOTH_CONNECT] ?: false
        }

        setContent {
            MaterialTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    LaunchedEffect(Unit) {
                        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            arrayOf(
                                Manifest.permission.BLUETOOTH_SCAN,
                                Manifest.permission.BLUETOOTH_CONNECT,
                                Manifest.permission.ACCESS_FINE_LOCATION
                            )
                        } else {
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.BLUETOOTH,
                                Manifest.permission.BLUETOOTH_ADMIN
                            )
                        }
                        permissionLauncher.launch(permissions)
                    }

                    MainNavigation(viewModel)

//                    VoiceControlScreen()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Controller release handles closing the socket
        bluetoothController.release()
    }
}