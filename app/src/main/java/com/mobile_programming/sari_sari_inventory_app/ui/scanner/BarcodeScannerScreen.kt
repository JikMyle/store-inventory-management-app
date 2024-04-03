package com.mobile_programming.sari_sari_inventory_app.ui.scanner

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mobile_programming.sari_sari_inventory_app.ui.AppViewModelProvider

@Composable
fun BarcodeScannerScreen(
    modifier: Modifier = Modifier,
    viewModel: BarCodeScannerViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    // Find a way to attach this to a view model
    // Or just stay as is
    val cameraPermissionResultLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        viewModel.onPermissionResult(isGranted)
    }

    Box(modifier = modifier.fillMaxSize()) {
        // Try putting the camera here
        // ISSUE: App crashes when immediately requesting permission mid-composition
    }
}