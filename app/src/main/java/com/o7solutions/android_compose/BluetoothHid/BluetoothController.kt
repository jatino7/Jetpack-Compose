package com.o7solutions.android_compose.BluetoothHid

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.IOException
import java.util.*

class BluetoothController(
    private val context: Context,
    private val adapter: BluetoothAdapter?
) {

    companion object {
        private const val TAG = "BluetoothController"
    }

    // Paired devices shown separately at top
    private val _pairedDevices = MutableStateFlow<List<BluetoothDevice>>(emptyList())
    val pairedDevices: StateFlow<List<BluetoothDevice>> = _pairedDevices.asStateFlow()

    // Newly discovered nearby devices (NOT yet paired)
    private val _scannedDevices = MutableStateFlow<List<BluetoothDevice>>(emptyList())
    val scannedDevices: StateFlow<List<BluetoothDevice>> = _scannedDevices.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private val _pairedDevice =  MutableStateFlow("")

    var pairedDevice : StateFlow<String> = _pairedDevice.asStateFlow()
    private val _pairingState = MutableStateFlow<PairingState>(PairingState.Idle)
    val pairingState: StateFlow<PairingState> = _pairingState.asStateFlow()

    private val SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    private var currentSocket: BluetoothSocket? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var receiverRegistered = false

    // ─── BroadcastReceiver ───────────────────────────────────────────────────────

    private val deviceReceiver = object : BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {

                BluetoothDevice.ACTION_FOUND -> {
                    val device: BluetoothDevice? =
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
                        } else {
                            @Suppress("DEPRECATION")
                            intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                        }

                    device?.let {
                        val pairedAddresses = _pairedDevices.value.map { d -> d.address }
                        // Only add to "available" list if NOT already paired
                        if (!pairedAddresses.contains(it.address)) {
                            val current = _scannedDevices.value.toMutableList()
                            if (!current.any { d -> d.address == it.address }) {
                                current.add(it)
                                _scannedDevices.value = current
                            }
                        }
                    }
                }

                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    _isScanning.value = false
                }

                BluetoothDevice.ACTION_BOND_STATE_CHANGED -> {
                    val device: BluetoothDevice? =
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
                        } else {
                            @Suppress("DEPRECATION")
                            intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                        }

                    val bondState = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR)

                    when (bondState) {
                        BluetoothDevice.BOND_BONDING ->
                            _pairingState.value = PairingState.Pairing(device?.address ?: "")

                        BluetoothDevice.BOND_BONDED -> {
                            _pairingState.value = PairingState.Paired(device?.address ?: "")
                            // Move device from scanned → paired list
                            device?.let { d ->
                                val scanned = _scannedDevices.value.toMutableList()
                                scanned.removeAll { it.address == d.address }
                                _scannedDevices.value = scanned

                                val paired = _pairedDevices.value.toMutableList()
                                if (!paired.any { it.address == d.address }) {
                                    paired.add(d)
                                    _pairedDevices.value = paired
                                }
                            }
                        }

                        BluetoothDevice.BOND_NONE ->
                            _pairingState.value = PairingState.Failed
                    }
                }
            }
        }
    }

    // ─── Discovery ───────────────────────────────────────────────────────────────

    @SuppressLint("MissingPermission")
    fun startDiscovery() {
        // Step 1: Load already-paired devices separately
        val paired = adapter?.bondedDevices?.toList() ?: emptyList()
        _pairedDevices.value = paired

        // Step 2: Clear old scan results
        _scannedDevices.value = emptyList()

        // Step 3: Register receiver BEFORE starting scan
        if (receiverRegistered) {
            try { context.unregisterReceiver(deviceReceiver) } catch (_: Exception) {}
        }
        val filter = IntentFilter().apply {
            addAction(BluetoothDevice.ACTION_FOUND)
            addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
            addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
        }
        context.registerReceiver(deviceReceiver, filter)
        receiverRegistered = true

        // Step 4: Cancel any ongoing scan then start fresh
        if (adapter?.isDiscovering == true) adapter.cancelDiscovery()

        _isScanning.value = true
        val started = adapter?.startDiscovery()
        if (started == false) _isScanning.value = false  // permission missing or BT off
    }

    @SuppressLint("MissingPermission")
    fun stopDiscovery() {
        adapter?.cancelDiscovery()
        _isScanning.value = false
    }

    // ─── Pair a device (result arrives via BroadcastReceiver) ────────────────────

    @SuppressLint("MissingPermission")
    fun pairDevice(address: String) {
        val device = adapter?.getRemoteDevice(address) ?: return
        if (device.bondState == BluetoothDevice.BOND_BONDED) {
            _pairingState.value = PairingState.Paired(address)

            _pairedDevice.value = device.address
            Log.d(TAG, "✅ Already bonded. Name set to: ${_pairedDevice.value}")
            return
        }
        scope.launch {
            adapter?.cancelDiscovery()
            delay(300)
            device.createBond()
        }
    }

    // ─── Connect via RFCOMM socket ───────────────────────────────────────────────


    // In BluetoothController.kt
    fun write(message: String) {
        try {
            currentSocket?.outputStream?.write(message.toByteArray())
        } catch (e: IOException) {
            Log.e("BT_TX", "Send failed", e)
        }
    }
    @SuppressLint("MissingPermission")
    fun connectToDevice(address: String) {
        Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        Log.d(TAG, "connectToDevice() called with address: $address")

        val device = adapter?.getRemoteDevice(address)
        if (device == null) {
            Log.e(TAG, "❌ getRemoteDevice() returned null for address: $address")
            return
        }

        Log.d(TAG, "✅ Device found: name=${device.name}, address=${device.address}, bondState=${bondStateLabel(device.bondState)}")

        scope.launch {
            try {
                Log.d(TAG, "⏹ Cancelling discovery before connect...")
                adapter?.cancelDiscovery()
                Log.d(TAG, "⏳ Waiting 500ms for discovery to stop...")
                delay(500)

                // ── Pairing ──────────────────────────────────────────────────────
                if (device.bondState != BluetoothDevice.BOND_BONDED) {
                    Log.w(TAG, "⚠️ Device is NOT bonded (state=${bondStateLabel(device.bondState)}). Starting pairing...")

                    pairDevice(address)
                    Log.d(TAG, "🔗 createBond() called. Waiting for BOND_BONDED (max 15s)...")


                    var waited = 0
                    while (device.bondState != BluetoothDevice.BOND_BONDED && waited < 30) {
                        delay(500)
                        waited++
                        Log.d(TAG, "   ⏳ Waiting for bond... attempt=$waited, currentState=${bondStateLabel(device.bondState)}")
                    }

                    if (device.bondState != BluetoothDevice.BOND_BONDED) {
                        Log.e(TAG, "❌ Pairing FAILED after ${waited * 500}ms. Final state=${bondStateLabel(device.bondState)}")
                        withContext(Dispatchers.Main) { _pairingState.value = PairingState.Failed }
                        return@launch
                    }

                    withContext(Dispatchers.Main) {
                        _pairedDevice.value = device.address
                    }
                    Log.d(TAG, "✅ Already bonded. Name set to: ${_pairedDevice.value}")
                    Log.d(TAG, "✅ Pairing SUCCESS. Bond state=${bondStateLabel(device.bondState)}")
                } else {
                    Log.d(TAG, "✅ Device already bonded. Skipping pairing.")
                    _pairedDevice.value = device.address ?: device.address
                    Log.d(TAG, "✅ Already bonded. Name set to: ${_pairedDevice.value}")
                }

                // ── Socket ───────────────────────────────────────────────────────
                Log.d(TAG, "🔌 Closing any existing socket...")
                currentSocket?.close()
                currentSocket = null

                Log.d(TAG, "🔌 Creating RFCOMM socket with UUID: $SPP_UUID")
                val socket: BluetoothSocket = try {
                    val s = device.createRfcommSocketToServiceRecord(SPP_UUID)
                    Log.d(TAG, "✅ Secure RFCOMM socket created")
                    s
                } catch (e: Exception) {
                    Log.w(TAG, "⚠️ Secure socket failed: ${e.message}. Trying insecure fallback...")
                    val s = device.createInsecureRfcommSocketToServiceRecord(SPP_UUID)
                    Log.d(TAG, "✅ Insecure RFCOMM socket created as fallback")
                    s
                }

                Log.d(TAG, "📡 Calling socket.connect()...")
                socket.connect()
                currentSocket = socket

                Log.d(TAG, "✅ Socket connected! isConnected=${socket.isConnected}")
                withContext(Dispatchers.Main) { _isConnected.value = true }

            } catch (e: Exception) {
                Log.e(TAG, "❌ Exception during connectToDevice(): ${e::class.simpleName}: ${e.message}")
                Log.e(TAG, "   Stack trace: ${Log.getStackTraceString(e)}")
                withContext(Dispatchers.Main) { _isConnected.value = false }
            }

            Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        }
    }

    /** Converts bond state int to a readable label for logs */
    private fun bondStateLabel(state: Int) = when (state) {
        BluetoothDevice.BOND_NONE    -> "BOND_NONE (10)"
        BluetoothDevice.BOND_BONDING -> "BOND_BONDING (11)"
        BluetoothDevice.BOND_BONDED  -> "BOND_BONDED (12)"
        else                         -> "UNKNOWN ($state)"
    }
    // ─── Cleanup ─────────────────────────────────────────────────────────────────

    fun release() {
        if (receiverRegistered) {
            try { context.unregisterReceiver(deviceReceiver) } catch (_: Exception) {}
            receiverRegistered = false
        }
        currentSocket?.close()
        scope.cancel()
    }
}

sealed class PairingState {
    object Idle   : PairingState()
    object Failed : PairingState()
    data class Pairing(val address: String) : PairingState()
    data class Paired (val address: String) : PairingState()
}