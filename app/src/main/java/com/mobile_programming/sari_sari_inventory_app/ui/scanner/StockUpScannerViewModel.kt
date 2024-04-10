package com.mobile_programming.sari_sari_inventory_app.ui.scanner

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.mobile_programming.sari_sari_inventory_app.data.InventoryRepository
import com.mobile_programming.sari_sari_inventory_app.data.entity.Product
import com.mobile_programming.sari_sari_inventory_app.ui.product.ProductDetails
import com.mobile_programming.sari_sari_inventory_app.ui.product.toProduct
import com.mobile_programming.sari_sari_inventory_app.ui.product.toProductDetails
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class StockUpScannerViewModel(
    private val inventoryRepository: InventoryRepository
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
        val productWithAmount = _uiState.value.productWithAmount.copy(
            productDetails = productDetails,
            amount = amount,
        )

        _uiState.update {
            it.copy(
                productWithAmount = productWithAmount,
                isInputValid = validateInput(productWithAmount)
            )
        }
    }

    suspend fun increaseProductStock() {
        val amount = _uiState.value.productWithAmount.amount
        val product = _uiState.value.productWithAmount.productDetails
            .toProduct()

        inventoryRepository.updateProduct(
            product.copy(stock = product.stock.plus(amount))
        )
    }

    private fun validateInput(productWithAmount: ProductWithAmount) : Boolean {
        return productWithAmount.amount != 0
    }
}

data class ScannerUiState(
    val scannerCameraState: ScannerCameraState = ScannerCameraState(),
    val searchBarState: SearchBarState<Product> = SearchBarState(),

    val scannedBarcode: String = "",
    val isNewProduct: Boolean = false,
    val isBottomSheetVisible: Boolean = false,
    val isInputValid: Boolean = false,
    val productWithAmount: ProductWithAmount = ProductWithAmount()
)

data class SearchBarState<T>(
    val isActive: Boolean = false,
    val searchQuery: String = "",
    val result: List<T> = emptyList(),
    val onQueryChange: (String) -> Unit = { },
    val onSearch: (String) -> Unit = onQueryChange,
    val onActiveChange: (Boolean) -> Unit = { }
)

data class ProductWithAmount(
    val productDetails: ProductDetails = ProductDetails(),
    val amount: Int = 0,
)