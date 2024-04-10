package com.mobile_programming.sari_sari_inventory_app.ui.receipt

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.mobile_programming.sari_sari_inventory_app.data.InventoryRepository
import com.mobile_programming.sari_sari_inventory_app.data.entity.Product
import com.mobile_programming.sari_sari_inventory_app.ui.product.ProductDetails
import com.mobile_programming.sari_sari_inventory_app.ui.product.toProductDetails
import com.mobile_programming.sari_sari_inventory_app.ui.scanner.BarcodeScannerViewModel
import com.mobile_programming.sari_sari_inventory_app.ui.scanner.ProductWithAmount
import com.mobile_programming.sari_sari_inventory_app.ui.scanner.ScannerCameraState
import com.mobile_programming.sari_sari_inventory_app.ui.scanner.ScannerUiState
import com.mobile_programming.sari_sari_inventory_app.ui.scanner.SearchBarState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ReceiptScannerViewModel(
    inventoryRepository: InventoryRepository
) : BarcodeScannerViewModel(inventoryRepository) {

    private var _uiState = MutableStateFlow(ScannerUiState())
    val uiState: StateFlow<ScannerUiState> = _uiState.asStateFlow()

    init {
        val scannerCameraState = ScannerCameraState(
            onPermissionResult = ::onPermissionResult,
            onBarcodeScanned = ::onBarcodeScanned,
            switchCamera = ::switchCamera
        )

        val searchBarState = SearchBarState<Product>(
            onQueryChange = ::updateQuery,
            onActiveChange = ::toggleSearchBar
        )

        _uiState.update {
            it.copy(
                scannerCameraState = scannerCameraState,
                searchBarState = searchBarState
            )
        }
    }

    override fun onPermissionResult(isGranted: Boolean) {
        super.onPermissionResult(isGranted)

        _uiState.update {
            it.copy(
                scannerCameraState = scannerCameraState
            )
        }
    }

    override fun switchCamera() {
        super.switchCamera()

        _uiState.update {
            it.copy(
                scannerCameraState = scannerCameraState
            )
        }
    }

    override fun onBarcodeScanned(productNumber: String) {
        if (_uiState.value.scannedBarcode.isNotEmpty()) return

        _uiState.update {
            it.copy(scannedBarcode = productNumber)
        }

        viewModelScope.launch {
            val productList = getProduct(productNumber)

            if (productList.isNotEmpty()) {
                Log.d("BarcodeAnalyzer", productList.first().toString())

                updateProductWithAmount(productList.first().toProductDetails())
                toggleBottomSheet(true)

            } else if (productNumber.isNotBlank()) {
                Log.d("BarcodeAnalyzer", "Barcode has no existing entry.")
                _uiState.update {
                    it.copy(
                        isNewProduct = true
                    )
                }
            }
        }
    }

    fun clearBarcodeScanned() {
        _uiState.update {
            it.copy(
                scannedBarcode = "",
                isNewProduct = false,
                productWithAmount = ProductWithAmount()
            )
        }
    }

    fun toggleSearchBar(isActive: Boolean) {

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

    fun toggleBottomSheet(isVisible: Boolean) {
        _uiState.update {
            it.copy(isBottomSheetVisible = isVisible)
        }

        if (!isVisible) {
            clearBarcodeScanned()
        }
    }

    private fun updateQuery(query: String) {
        val searchBarState = _uiState.value.searchBarState.copy(
            searchQuery = query
        )

        _uiState.update {
            it.copy(
                searchBarState = searchBarState
            )
        }

        updateSearchResults(query)
    }

    private fun updateSearchResults(query: String) {
        viewModelScope.launch {
            val productList = getProduct(query)

            val searchBarState = _uiState.value.searchBarState.copy(
                result = productList
            )

            _uiState.update {
                it.copy(
                    searchBarState = searchBarState
                )
            }
        }
    }

    fun updateProductWithAmount(
        productDetails: ProductDetails = _uiState.value.productWithAmount.productDetails,
        amount: Int = 0
    ) {
        _uiState.update {
            it.copy(
                productWithAmount = it.productWithAmount.copy(
                    productDetails = productDetails,
                    amount = amount
                )
            )
        }
    }
}