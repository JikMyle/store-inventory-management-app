package com.mobile_programming.sari_sari_inventory_app.ui.scanner

import android.util.Log
import com.mobile_programming.sari_sari_inventory_app.data.InventoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class StockUpViewModel(
    private val inventoryRepository: InventoryRepository
) : BarcodeScannerViewModel(inventoryRepository) {

    private var _uiState = MutableStateFlow(StockUpUiState())
    val uiState: StateFlow<StockUpUiState> = _uiState

    override fun onPermissionResult(isGranted: Boolean) {
        _uiState.value = _uiState.value.copy(hasCameraAccess = isGranted)
    }

    override fun onScanSuccess(productNumber: String) {
        if(productNumber.isNotBlank()) {
            Log.d("TEST", productNumber)
        } else {
            Log.d("TEST", "NO CODE SCANNED")
        }
    }

}

data class StockUpUiState(
    val hasCameraAccess: Boolean = false,
    val isCameraBack: Boolean = true,
)