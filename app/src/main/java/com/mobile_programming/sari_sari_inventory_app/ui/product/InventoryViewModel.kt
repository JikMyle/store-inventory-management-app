package com.mobile_programming.sari_sari_inventory_app.ui.product

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobile_programming.sari_sari_inventory_app.data.InventoryRepository
import com.mobile_programming.sari_sari_inventory_app.data.entity.Product
import com.mobile_programming.sari_sari_inventory_app.ui.scanner.SearchBarState
import com.mobile_programming.sari_sari_inventory_app.utils.SortingType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class InventoryViewModel(
    private val inventoryRepository: InventoryRepository
) : ViewModel() {

    private var _uiState = MutableStateFlow(
        InventoryUiState(
            searchBarState = SearchBarState(
                onQueryChange = ::updateQuery,
            )
        )
    )

    val uiState: StateFlow<InventoryUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            inventoryRepository.getAllProducts()
                .filterNotNull()
                .collect { productList ->
                    _uiState.update { oldState ->
                        oldState.copy(
                            productList = sortProductList(productList)
                        )
                    }
                }
        }
    }

    private fun updateQuery(query: String) {
        val newSearchBarState = _uiState.value.searchBarState.copy(
            searchQuery = query
        )

        _uiState.update {
            it.copy(
                searchBarState = newSearchBarState
            )
        }

        updateProductList(query)
    }

    private fun updateProductList(query: String) {
        viewModelScope.launch {
            val productList =
                if (query.isEmpty()) getAllProducts()
                else getProducts(query)

            val sortedList = sortProductList(productList)

            _uiState.update {
                it.copy(
                    productList = sortedList
                )
            }
        }
    }

    private suspend fun getAllProducts(): List<Product> {
        return inventoryRepository.getAllProducts()
            .filterNotNull()
            .first()
    }

    private suspend fun getProducts(nameOrNumber: String): List<Product> {
        return inventoryRepository.searchForProduct(nameOrNumber)
            .filterNotNull()
            .first()
    }

    suspend fun deleteProduct(product: Product) {
        inventoryRepository.deleteProduct(product)
    }

    fun changeSortingType(sortingType: SortingType, ascending: Boolean) {
        _uiState.update {
            it.copy(
                sortingType = sortingType,
                isSortAscending = ascending
            )
        }

        updateProductList(_uiState.value.searchBarState.searchQuery)
    }

    private fun sortProductList(productList: List<Product>): List<Product> {
        return if (_uiState.value.isSortAscending) {
            productList.sortedWith(
                compareBy {
                    when (_uiState.value.sortingType) {
                        SortingType.ByProductNumber -> it.productNumber
                        SortingType.ByPrice -> it.price
                        SortingType.ByStock -> it.stock
                        else -> it.productName.lowercase()
                    }
                }
            )
        } else {
            productList.sortedWith(
                compareByDescending {
                    when (_uiState.value.sortingType) {
                        SortingType.ByProductNumber -> it.productNumber
                        SortingType.ByPrice -> it.price
                        SortingType.ByStock -> it.stock
                        else -> it.productName.lowercase()
                    }
                }
            )
        }
    }
}

data class InventoryUiState(
    val productList: List<Product> = listOf(),
    val sortingType: SortingType = SortingType.ByProductName,
    val isSortAscending: Boolean = true,
    val searchBarState: SearchBarState<Product> = SearchBarState()
)