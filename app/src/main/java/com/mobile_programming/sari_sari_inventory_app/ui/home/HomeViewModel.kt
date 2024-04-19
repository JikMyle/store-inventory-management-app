package com.mobile_programming.sari_sari_inventory_app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobile_programming.sari_sari_inventory_app.data.InventoryRepository
import com.mobile_programming.sari_sari_inventory_app.data.relation.ProductSale
import com.mobile_programming.sari_sari_inventory_app.ui.product.ProductDetails
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar

class HomeViewModel(
    private val inventoryRepository: InventoryRepository
) : ViewModel() {

    private var _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            inventoryRepository.countOutOfStock()
                .filterNotNull()
                .collect { count ->
                    _uiState.update {
                        it.copy(outOfStockCount = count)
                    }
                }

            inventoryRepository.countLowOnStock(10)
                .filterNotNull()
                .collect { count ->
                    _uiState.update {
                        it.copy(lowOnStockCount = count)
                    }
                }

            inventoryRepository.getProductSalesFromDates(
                id = null,
                dateFrom = Calendar.getInstance().apply { add(Calendar.DATE, -30) }.time,
                dateTo = Calendar.getInstance().time
            )
                .filterNotNull()
                .collect { productSalesList ->
                    _uiState.update {
                        it.copy(productSales = productSalesList)
                    }
                }
        }
    }
}

data class HomeUiState(
    val outOfStockCount: Int = 0,
    val lowOnStockCount: Int = 0,
    val productSales: List<ProductSale>  = listOf()
)