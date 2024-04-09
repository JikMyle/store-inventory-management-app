package com.mobile_programming.sari_sari_inventory_app.ui.scanner

import androidx.lifecycle.ViewModel

abstract class BarcodeScannerViewModel : ViewModel()  {

    protected var scannerState = ScannerState()
    open fun onPermissionResult(isGranted: Boolean) {
        scannerState = scannerState.copy(hasCameraAccess = isGranted)
    }
    open fun switchCamera() {
        scannerState = scannerState.copy(
            isCameraFacingBack = !scannerState.isCameraFacingBack
        )
    }

    abstract fun onBarcodeScanned(productNumber: String)
}

data class ScannerState(
    val hasCameraAccess: Boolean = false,
    val isCameraFacingBack : Boolean = true,
)