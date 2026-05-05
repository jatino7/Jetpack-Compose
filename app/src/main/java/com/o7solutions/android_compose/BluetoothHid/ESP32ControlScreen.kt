package com.o7solutions.android_compose.BluetoothHid

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresPermission
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController







@RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
fun BluetoothDevice.displayName(): String =
    name?.takeIf { it.isNotBlank() } ?: "Unknown (${address})"

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("MissingPermission")
@Composable
fun ESP32ControlScreen(
    viewModel: BluetoothViewModel,
    navController: NavHostController
) {



    val pairedDevices  by viewModel.pairedDevices.collectAsState()
    val scannedDevices by viewModel.scannedDevices.collectAsState()
    val isScanning     by viewModel.isScanning.collectAsState()
    val connected      by viewModel.isConnected.collectAsState()
    val pairingState   by viewModel.pairingState.collectAsState()
    val pairedDevice  by viewModel.pairedDevice.collectAsState()

    var connectedDevice = remember { mutableStateOf("") }
    val bluetoothAdapter: BluetoothAdapter? = remember { BluetoothAdapter.getDefaultAdapter() }


    LaunchedEffect(connected) {
        if (connected && pairedDevice != null) {
            // Navigate to voice screen and pass the address as a specialized argument
            navController.navigate("voice_control/${pairedDevice}")
        }
    }


    val enableBluetoothLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            viewModel.startScanning()
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding(),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Bluetooth Scanner") },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {

            // ── Scan button ──────────────────────────────────────────────────
            Button(
                onClick = {
                    when {
                        bluetoothAdapter == null -> { /* no BT hardware */ }
                        !bluetoothAdapter.isEnabled -> {
                            enableBluetoothLauncher.launch(
                                Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                            )
                        }
                        else -> viewModel.startScanning()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                if (isScanning) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Text("Scanning...")
                    }
                } else {
                    Text("Scan for Devices")
                }
            }

            // ── Connection status ────────────────────────────────────────────
            if (connected) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))
                ) {
                    Text(
                        "Connected Device ${pairedDevice}",
                        color = Color(0xFF2E7D32),
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }

            // ── Pairing status ───────────────────────────────────────────────
            when (val state = pairingState) {
                is PairingState.Pairing -> {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF9C4))
                    ) {
                        Text(
                            "⏳ Pairing with ${state.address}...",
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
                is PairingState.Failed -> {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
                    ) {
                        Text(
                            "❌ Pairing failed. Try again.",
                            color = Color(0xFFC62828),
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
                else -> {}
            }

            // ── Device list ──────────────────────────────────────────────────
            LazyColumn(modifier = Modifier.weight(1f)) {

                // ── Section 1: Paired devices ────────────────────────────────
                if (pairedDevices.isNotEmpty()) {
                    item {
                        Text(
                            "Paired Devices (${pairedDevices.size})",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
                        )
                    }
                    items(pairedDevices, key = { it.address }) { device ->
                        DeviceCard(
                            device = device,
                            badge = "PAIRED",
                            badgeColor = Color(0xFF2E7D32),
                            onClick = { viewModel.connect(device.address) }
                        )
                    }
                }

                // ── Section 2: Available (not yet paired) devices ────────────
                item {
                    Text(
                        if (isScanning) "Available Devices (scanning...)"
                        else "Available Devices (${scannedDevices.size})",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
                    )
                }

                if (scannedDevices.isEmpty() && !isScanning) {
                    item {
                        Text(
                            "No new devices found. Make sure the device is discoverable and tap Scan.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }

                items(scannedDevices, key = { it.address }) { device ->
                    DeviceCard(
                        device = device,
                        badge = "TAP TO PAIR",
                        badgeColor = Color(0xFF1565C0),
                        onClick = { viewModel.connect(device.address) }
                    )
                }
            }
        }
    }
}

// ── Reusable device card ─────────────────────────────────────────────────────

@SuppressLint("MissingPermission")
@Composable
private fun DeviceCard(
    device: BluetoothDevice,
    badge: String,
    badgeColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = device.displayName(),   // ✅ uses top-level extension
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = device.address,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
            Surface(
                color = badgeColor.copy(alpha = 0.15f),
                shape = MaterialTheme.shapes.small
            ) {
                Text(
                    text = badge,
                    color = badgeColor,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}