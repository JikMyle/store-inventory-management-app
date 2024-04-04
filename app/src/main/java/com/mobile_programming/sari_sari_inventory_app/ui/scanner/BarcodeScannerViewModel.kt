package com.mobile_programming.sari_sari_inventory_app.ui.scanner

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import com.mobile_programming.sari_sari_inventory_app.data.InventoryRepository

abstract class BarcodeScannerViewModel(
    private val inventoryRepository: InventoryRepository
) : ViewModel() {
    abstract fun onPermissionResult(isGranted: Boolean)

    abstract fun onScanSuccess(productNumber: String)
}