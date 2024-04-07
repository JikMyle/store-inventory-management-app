package com.mobile_programming.sari_sari_inventory_app.ui.scanner

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.mobile_programming.sari_sari_inventory_app.data.InventoryRepository
import com.mobile_programming.sari_sari_inventory_app.data.entity.Product
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class StockUpViewModel(
    private val inventoryRepository: InventoryRepository
) : BarcodeScannerViewModel() {

    private var _uiState = MutableStateFlow(StockUpUiState())
    val uiState: StateFlow<StockUpUiState> = _uiState.asStateFlow()

    init {
        _uiState.update { stockUpUiState ->
            stockUpUiState.copy(
                cameraState = cameraState,
                searchBarState = SearchBarState(
                    onQueryChange = { updateQuery(it) },
                    onActiveChange = { toggleSearchBar(it) }
                )
            )
        }
    }

    override fun onPermissionResult(isGranted: Boolean) {
        super.onPermissionResult(isGranted)

        _uiState.update {
            it.copy(
                cameraState = cameraState
            )
        }
    }

    override fun switchCamera() {
        super.switchCamera()

        _uiState.update {
            it.copy(
                cameraState = cameraState
            )
        }
    }

    override fun onBarcodeScanned(productNumber: String) {
        viewModelScope.launch {
            val productList = getProduct(productNumber)

            if (productList.isNotEmpty()) {
                Log.d("BarcodeAnalyzer", productList.first().toString())
            } else if (productNumber.isNotBlank()) {
                Log.d("BarcodeAnalyzer", "Barcode has no existing entry.")
            }
        }
    }

    private suspend fun getProduct(nameOrNumber: String): List<Product> {

        return if (nameOrNumber.isNotEmpty()) {
            inventoryRepository.getProduct(nameOrNumber)
                .filterNotNull()
                .first()
        } else {
            listOf()
        }
    }

    private fun toggleSearchBar(isActive: Boolean) {

        val searchBarState = _uiState.value.searchBarState.copy(
            isActive = isActive
        )

        _uiState.update {
            it.copy(
                searchBarState = searchBarState
            )
        }

        if (!isActive) {
            updateQuery("")
        }
    }

    private fun updateQuery(query: String) {
        viewModelScope.launch {
            val productList = getProduct(query)

            val searchBarState = _uiState.value.searchBarState.copy(
                searchQuery = query,
                result = productList
            )

            _uiState.update {
                it.copy(
                    searchBarState = searchBarState
                )
            }

//            _uiState.update {
//                it.copy(
//                    searchQuery = query,
//                    searchResult = productList
//                )
//            }
        }
    }
}

data class StockUpUiState(
    val cameraState: CameraState = CameraState(),
    val searchBarState: SearchBarState<Product> = SearchBarState()
//    val hasCameraAccess: Boolean = false,
//    val isCameraFacingBack: Boolean = true,
//
//    val isSearchActive: Boolean = false,
//    val searchQuery: String = "",
//    val searchResult: List<Product> = listOf(),
)

data class SearchBarState<T>(
    val isActive: Boolean = false,
    val searchQuery: String = "",
    val result: List<T> = emptyList(),
    val onQueryChange: (String) -> Unit = { },
    val onSearch: (String) -> Unit = onQueryChange,
    val onActiveChange: (Boolean) -> Unit = { },
)