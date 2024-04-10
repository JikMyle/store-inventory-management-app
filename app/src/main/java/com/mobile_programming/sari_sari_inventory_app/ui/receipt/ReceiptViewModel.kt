package com.mobile_programming.sari_sari_inventory_app.ui.receipt

import androidx.lifecycle.ViewModel
import com.mobile_programming.sari_sari_inventory_app.data.InventoryRepository
import com.mobile_programming.sari_sari_inventory_app.data.entity.Product
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class ReceiptViewModel(
    inventoryRepository: InventoryRepository
) : ViewModel() {

    private var _uiState = MutableStateFlow(ReceiptUiState())
    val uiState: StateFlow<ReceiptUiState> = _uiState.asStateFlow()

    fun addProductToReceipt(product: Product) {
        _uiState.update {
            it.copy(
                productList = _uiState.value.productList.plus(product)
            )
        }
    }
}

data class ReceiptUiState(
    val productList: List<Product> = listOf()
)