package com.mobile_programming.sari_sari_inventory_app.ui.scanner

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mobile_programming.sari_sari_inventory_app.ui.AppViewModelProvider

@Composable
fun StockUpScannerScreen(
    modifier: Modifier = Modifier,
    viewModel: StockUpViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val uiState = viewModel.uiState.collectAsState()

    val cameraPermissionResultLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        viewModel.onPermissionResult(isGranted)
    }

    if(uiState.value.hasCameraAccess) {
        BarcodeScanner(
            isCameraFacingBack = uiState.value.isCameraBack,
            onScanSuccess = { viewModel.onScanSuccess(it) },
        )
    } else {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(text = "NO CAMERA")
        }
    }

    SideEffect {
        cameraPermissionResultLauncher.launch(
            Manifest.permission.CAMERA
        )
    }
}