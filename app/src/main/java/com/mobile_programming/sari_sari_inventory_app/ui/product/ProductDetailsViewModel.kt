package com.mobile_programming.sari_sari_inventory_app.ui.product

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobile_programming.sari_sari_inventory_app.data.InventoryRepository
import com.mobile_programming.sari_sari_inventory_app.utils.TextInputErrorType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProductDetailsViewModel(
    savedStateHandle: SavedStateHandle,
    private val inventoryRepository: InventoryRepository
) : ViewModel() {

    private val id: Long = checkNotNull(savedStateHandle[PRODUCT_ID_NAV_ARG_KEY])

    private var _uiState = MutableStateFlow(ProductDetailsUiState())
    val uiState: StateFlow<ProductDetailsUiState> = _uiState.asStateFlow()

    companion object {
        const val PRODUCT_ID_NAV_ARG_KEY = "productId"
    }

    init {
        viewModelScope.launch {
            inventoryRepository.getProduct(id)
                .filterNotNull()
                .collect { product ->
                    _uiState.update { oldUiState ->
                        oldUiState.copy(
                            productDetails = product.toProductDetails()
                        )
                    }
                }
        }
    }

    private fun validateInput(
        productDetails: ProductDetails = _uiState.value.productDetails
    ): Map<String, TextInputErrorType?> {

        val errorMap = _uiState.value.detailsErrorMap.toMutableMap()

        checkIfProductNumberExists(productDetails.productNumber)

        errorMap[ProductEntryViewModel.PRODUCT_NAME_ERROR_KEY] =
            if (productDetails.productName.isEmpty()) {
                TextInputErrorType.FieldRequired
            } else {
                null
            }
        errorMap[ProductEntryViewModel.PRODUCT_PRICE_ERROR_KEY] =
            if (productDetails.price.isEmpty()) {
                TextInputErrorType.FieldRequired
            } else {
                null
            }

        errorMap[ProductEntryViewModel.PRODUCT_STOCK_ERROR_KEY] =
            if (productDetails.stock.isEmpty()) {
                TextInputErrorType.FieldRequired
            } else {
                null
            }

        return errorMap
    }

    // Check is the product number already exists and displays error
    // If the product number is empty or if it is unchanged then no error is displayed
    private fun checkIfProductNumberExists(productNumber: String) {
        if (
            productNumber.isEmpty()
            || productNumber == _uiState.value.productDetails.productNumber
        ) {
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            if (inventoryRepository.checkIfProductNumberExists(productNumber)) {
                val errorMap = _uiState.value.detailsErrorMap.toMutableMap()
                errorMap[ProductEntryViewModel.PRODUCT_NUMBER_ERROR_KEY] =
                    TextInputErrorType.DuplicateFound

                _uiState.update {
                    it.copy(
                        detailsErrorMap = errorMap
                    )
                }
            }
        }
    }

    fun updateProduct(productDetails: ProductDetails) {
        if (productDetails == _uiState.value.productDetails) return

        viewModelScope.launch(Dispatchers.IO) {
            inventoryRepository.updateProduct(productDetails.toProduct())
        }
    }

    fun toggleEditingMode(isEditing: Boolean) {
        val tempDetails = _uiState.value.productDetails

        _uiState.update {
            it.copy(
                isEditing = isEditing,
                isInputValid = isEditing,
                tempDetails = tempDetails
            )
        }
    }

    fun updateUiState(productDetails: ProductDetails) {
        if (_uiState.value.isEditing) {
            val errorMap = validateInput(productDetails)
            var isInputValid = true

            errorMap.values.forEach { if (it != null) isInputValid = false }

            _uiState.update {
                it.copy(
                    tempDetails = productDetails,
                    isInputValid = isInputValid,
                    detailsErrorMap = errorMap
                )
            }
        }
    }
}

data class ProductDetailsUiState(
    val productDetails: ProductDetails = ProductDetails(),
    val isEditing: Boolean = false,
    val isInputValid: Boolean = false,
    val tempDetails: ProductDetails = ProductDetails(),
    val detailsErrorMap: Map<String, TextInputErrorType?> = mapOf()
)