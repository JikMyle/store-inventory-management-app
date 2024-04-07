package com.mobile_programming.sari_sari_inventory_app.ui.scanner

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.mobile_programming.sari_sari_inventory_app.data.InventoryRepository
import com.mobile_programming.sari_sari_inventory_app.data.entity.Product
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class StockUpViewModel(
    private val inventoryRepository: InventoryRepository
) : BarcodeScannerViewModel(inventoryRepository) {

    private var _uiState = MutableStateFlow(StockUpUiState())
    val uiState: StateFlow<StockUpUiState> = _uiState

    override fun onPermissionResult(isGranted: Boolean) {
        _uiState.value = _uiState.value.copy(hasCameraAccess = isGranted)
    }
    override fun onBarcodeScanned(productNumber: String) {
        viewModelScope.launch {
            val product = if(productNumber.isNotBlank()) {
                getProductByNameOrNumber(productNumber).let {
                    if(it.isEmpty()) null
                    else it.first()
                }
            } else {
                null
            }

            if(product != null) {
                Log.d("BarcodeAnalyzer", product.toString())
            } else if(productNumber.isNotBlank()) {
                Log.d("BarcodeAnalyzer", "Barcode has no existing entry.")
            }
        }
    }

    private suspend fun getProductByNameOrNumber(nameOrNumber: String): List<Product> {
        return inventoryRepository.getProduct(nameOrNumber)
            .filterNotNull()
            .first()
    }
}

data class StockUpUiState(
    val hasCameraAccess: Boolean = true,
    val isCameraBack: Boolean = true,
)