package com.mobile_programming.sari_sari_inventory_app.ui.scanner

import androidx.lifecycle.ViewModel

abstract class BarcodeScannerViewModel : ViewModel()  {

    protected var cameraState = CameraState()
    open fun onPermissionResult(isGranted: Boolean) {
        cameraState = cameraState.copy(hasCameraAccess = isGranted)
    }
    open fun switchCamera() {
        cameraState = cameraState.copy(
            isCameraFacingBack = !cameraState.isCameraFacingBack
        )
    }

    abstract fun onBarcodeScanned(productNumber: String)
}

data class CameraState(
    val hasCameraAccess: Boolean = false,
    val isCameraFacingBack : Boolean = true,
)