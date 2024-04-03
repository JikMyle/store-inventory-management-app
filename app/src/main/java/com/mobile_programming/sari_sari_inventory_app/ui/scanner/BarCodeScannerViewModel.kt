package com.mobile_programming.sari_sari_inventory_app.ui.scanner

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import com.mobile_programming.sari_sari_inventory_app.data.InventoryRepository

class BarCodeScannerViewModel(
    private val inventoryRepository: InventoryRepository
) : ViewModel() {

    private var _uiState = BarcodeScannerUiState()
    val uiState = _uiState

    fun onPermissionResult(isGranted: Boolean) {
        _uiState = _uiState.copy(hasCameraAccess = isGranted)
    }

    fun checkCameraPermission(context: Context) : Boolean {
        val status = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
        return status == PackageManager.PERMISSION_GRANTED
    }
}

data class BarcodeScannerUiState(
    val hasCameraAccess: Boolean = false,
)