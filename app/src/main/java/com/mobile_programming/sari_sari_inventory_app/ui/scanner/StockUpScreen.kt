package com.mobile_programming.sari_sari_inventory_app.ui.scanner

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mobile_programming.sari_sari_inventory_app.ui.AppViewModelProvider

@Composable
fun StockUpScannerScreen(
    modifier: Modifier = Modifier,
    viewModel: StockUpViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val uiState = viewModel.uiState.collectAsState()

    BarcodeScanner(
        hasCameraAccess = uiState.value.hasCameraAccess,
        isCameraFacingBack = uiState.value.isCameraBack,
        hasFoundBarcode = uiState.value.hasFoundBarcode,
        onPermissionResult = viewModel::onPermissionResult,
        onBarcodeScanned = viewModel::onBarcodeScanned
    )
}