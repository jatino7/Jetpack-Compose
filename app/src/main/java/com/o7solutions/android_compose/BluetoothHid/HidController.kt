package com.o7solutions.android_compose.BluetoothHid

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.*
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat

// --- Part 1: HID Controller Logic ---

@SuppressLint("MissingPermission")
class HidController(val context: Context) {

    private var hidDevice: BluetoothHidDevice? = null
    private var hostDevice: BluetoothDevice? = null

    private val HID_REPORT_DESC = byteArrayOf(
        0x05.toByte(), 0x01.toByte(), 0x09.toByte(), 0x02.toByte(), 0xa1.toByte(), 0x01.toByte(),
        0x09.toByte(), 0x01.toByte(), 0xa1.toByte(), 0x00.toByte(), 0x05.toByte(), 0x09.toByte(),
        0x19.toByte(), 0x01.toByte(), 0x29.toByte(), 0x03.toByte(), 0x15.toByte(), 0x00.toByte(),
        0x25.toByte(), 0x01.toByte(), 0x95.toByte(), 0x03.toByte(), 0x75.toByte(), 0x01.toByte(),
        0x81.toByte(), 0x02.toByte(), 0x95.toByte(), 0x01.toByte(), 0x75.toByte(), 0x05.toByte(),
        0x81.toByte(), 0x01.toByte(), 0x05.toByte(), 0x01.toByte(), 0x09.toByte(), 0x30.toByte(),
        0x09.toByte(), 0x31.toByte(), 0x15.toByte(), 0x81.toByte(), 0x25.toByte(), 0x7f.toByte(),
        0x75.toByte(), 0x08.toByte(), 0x95.toByte(), 0x02.toByte(), 0x81.toByte(), 0x06.toByte(),
        0xc0.toByte(), 0xc0.toByte()
    )

    init {
        val adapter = BluetoothAdapter.getDefaultAdapter()
        adapter?.getProfileProxy(context, object : BluetoothProfile.ServiceListener {
            @RequiresApi(Build.VERSION_CODES.P)
            override fun onServiceConnected(profile: Int, proxy: BluetoothProfile) {
                if (profile == BluetoothProfile.HID_DEVICE) {
                    hidDevice = proxy as BluetoothHidDevice
                    registerApp()
                }
            }
            override fun onServiceDisconnected(profile: Int) {
                hidDevice = null
            }
        }, BluetoothProfile.HID_DEVICE)
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun registerApp() {
        val mouseSubclass = 0x80.toByte()
        val sdp = BluetoothHidDeviceAppSdpSettings(
            "AndroidMouse", "Remote Mouse", "Android",
            mouseSubclass, HID_REPORT_DESC
        )

        hidDevice?.registerApp(sdp, null, null, context.mainExecutor, object : BluetoothHidDevice.Callback() {
            override fun onConnectionStateChanged(device: BluetoothDevice, state: Int) {
                super.onConnectionStateChanged(device, state)
                if (state == BluetoothProfile.STATE_CONNECTED) {
                    hostDevice = device
                    Log.d("HID_DEBUG", "Connected to: ${device.name}")
                } else {
                    hostDevice = null
                }
            }
        })
    }

    @RequiresApi(Build.VERSION_CODES.P)
    fun sendMouseMovement(dx: Float, dy: Float, leftClick: Boolean = false) {
        val buttons = if (leftClick) 0x01.toByte() else 0x00.toByte()
        val report = byteArrayOf(
            buttons,
            dx.toInt().coerceIn(-127, 127).toByte(),
            dy.toInt().coerceIn(-127, 127).toByte()
        )
        hostDevice?.let { hidDevice?.sendReport(it, 0, report) }
    }
}

// --- Part 2: Compose UI with Runtime Permission Check ---

@RequiresApi(Build.VERSION_CODES.P)
@Composable
fun TrackpadScreen() {
    val context = LocalContext.current

    // Permission state
    var hasPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
            } else true
        )
    }

    // Launcher to request permission
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasPermission = isGranted
    }

    if (!hasPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        // Show Permission Request UI
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Bluetooth Permission Required")
            Button(onClick = { launcher.launch(Manifest.permission.BLUETOOTH_CONNECT) }) {
                Text("Grant Permission")
            }
        }
    } else {
        // Show Actual Trackpad UI
        val controller = remember { HidController(context) }

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Bluetooth HID Trackpad", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(20.dp))

            Box(
                modifier = Modifier
                    .size(300.dp, 400.dp)
                    .background(Color.DarkGray, shape = MaterialTheme.shapes.medium)
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            controller.sendMouseMovement(dragAmount.x, dragAmount.y)
                        }
                    }
            ) {
                Text("Swipe Here", modifier = Modifier.align(Alignment.Center), color = Color.White)
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(onClick = { controller.sendMouseMovement(0f, 0f, true) }) {
                Text("Left Click")
            }
        }
    }
}