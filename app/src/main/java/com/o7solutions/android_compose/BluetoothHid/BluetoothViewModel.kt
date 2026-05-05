package com.o7solutions.android_compose.BluetoothHid

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.flow.StateFlow
import android.bluetooth.BluetoothDevice
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BluetoothViewModel(
    private val controller: BluetoothController
) : ViewModel() {

    // ── Expose all controller flows ──────────────────────────────────────────────

    /** Devices already bonded with this phone */
    val pairedDevices: StateFlow<List<BluetoothDevice>> = controller.pairedDevices

    /** Nearby devices found during scan, not yet paired */
    val scannedDevices: StateFlow<List<BluetoothDevice>> = controller.scannedDevices

    /** True while Bluetooth discovery is running */
    val isScanning: StateFlow<Boolean> = controller.isScanning

    /** True when a socket connection is established */
    val isConnected: StateFlow<Boolean> = controller.isConnected
    val pairedDevice: StateFlow<String> = controller.pairedDevice
    /** Current pairing state (Idle / Pairing / Paired / Failed) */
    val pairingState: StateFlow<PairingState> = controller.pairingState

    // ── Actions ──────────────────────────────────────────────────────────────────

    fun startScanning() {
        controller.startDiscovery()
    }

    fun stopScanning() {
        controller.stopDiscovery()
    }

    /** Call this when tapping a scanned (unpaired) device */
    fun pair(address: String) {
        controller.pairDevice(address)
    }

    // In BluetoothViewModel.kt
    fun sendMessage(message: String) {
        viewModelScope.launch(Dispatchers.IO) {
            controller.write(message) // We'll add this to the controller
        }
    }

    /** Call this when tapping a paired device to open a socket connection */
    fun connect(address: String) {
        controller.connectToDevice(address)
    }

    // ── Cleanup ──────────────────────────────────────────────────────────────────

    override fun onCleared() {
        super.onCleared()
//        controller.release()
    }

    // ── Factory ──────────────────────────────────────────────────────────────────

    /**
     * Use this factory to create the ViewModel with a BluetoothController dependency.
     *
     * Usage in Activity/Fragment:
     *   val viewModel: BluetoothViewModel by viewModels {
     *       BluetoothViewModel.Factory(BluetoothController(context, BluetoothAdapter.getDefaultAdapter()))
     *   }
     */
    class Factory(private val controller: BluetoothController) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(BluetoothViewModel::class.java)) {
                return BluetoothViewModel(controller) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}