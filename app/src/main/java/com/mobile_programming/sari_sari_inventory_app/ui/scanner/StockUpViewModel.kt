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
    override fun onBarcodeScanned(productNumber: String) {
        if(productNumber.isNotBlank()) {
            Log.d("BarcodeAnalyzer", productNumber)
            _uiState.value = _uiState.value.copy(hasFoundBarcode = true)
        } else {
            _uiState.value = _uiState.value.copy(hasFoundBarcode = false)
        }
    }

}

data class StockUpUiState(
    val hasCameraAccess: Boolean = true,
    val isCameraBack: Boolean = true,
    val hasFoundBarcode: Boolean = false
)