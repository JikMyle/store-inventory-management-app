package com.mobile_programming.sari_sari_inventory_app.ui.receipt

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobile_programming.sari_sari_inventory_app.data.InventoryRepository
import com.mobile_programming.sari_sari_inventory_app.data.entity.Product
import com.mobile_programming.sari_sari_inventory_app.data.entity.Receipt
import com.mobile_programming.sari_sari_inventory_app.ui.product.ProductDetails
import com.mobile_programming.sari_sari_inventory_app.ui.product.toProduct
import com.mobile_programming.sari_sari_inventory_app.ui.scanner.ProductWithAmount
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Calendar

class ReceiptViewModel(
    private val inventoryRepository: InventoryRepository
) : ViewModel() {

    private var _uiState = MutableStateFlow(ReceiptUiState())
    val uiState: StateFlow<ReceiptUiState> = _uiState.asStateFlow()

    fun addProductToReceipt(productWithAmount: ProductWithAmount) {
        val receiptMap = _uiState.value.receiptMap.toMutableMap()
        val productDetails = productWithAmount.productDetails
        val amount = productWithAmount.amount

        if (amount == 0) {
            return
        } else {
            receiptMap[productDetails] = (receiptMap[productDetails] ?: 0) + amount
        }

        val total = getReceiptTotal(receiptMap)

        _uiState.update {
            it.copy(
                receiptMap = receiptMap,
                total = total
            )
        }
    }

    fun removeProductFromReceipt(productDetails: ProductDetails) {
        val receiptMap = _uiState.value.receiptMap.toMutableMap()
        val productTotal = (productDetails.price.toDoubleOrNull() ?: 0.0)
                .times(receiptMap[productDetails] ?: 0)

        _uiState.update {
            it.copy(
                receiptMap = receiptMap.apply {
                    remove(productDetails)
                },
                total = it.total - productTotal
            )
        }
    }

    private fun getReceiptTotal(receiptMap: Map<ProductDetails, Int>): Double {
        var total = 0.0

        receiptMap.forEach {
            total += it.key.price.toDouble() * it.value
        }

        return total
    }

    fun confirmSale() {
        viewModelScope.launch {
            val receiptMap = _uiState.value.receiptMap
            insertReceipt(receiptMap)

            receiptMap.forEach {
                subtractProductStock(it.key.toProduct(), it.value)
            }

            clearReceipt()
        }
    }

    private suspend fun insertReceipt(receiptMap: Map<ProductDetails, Int>): Long {

        val productsWithAmounts = receiptMap.mapKeys {
            it.key.toProduct()
        }
        val total = productsWithAmounts.map {
            it.key.price * it.value
        }.sum()

        val receipt = Receipt(
            id = 0,
            dateCreated = Calendar.getInstance().time,
            total = total
        )

        return inventoryRepository.insertReceipt(receipt, productsWithAmounts)
    }

    private suspend fun subtractProductStock(product: Product, amount: Int) {
        inventoryRepository.updateProduct(
            product.copy(stock = product.stock - amount)
        )
    }

    fun clearReceipt() {
        _uiState.update {
            ReceiptUiState()
        }
    }
}

data class ReceiptUiState(
    val receiptMap: Map<ProductDetails, Int> = mapOf(),
    val total: Double = 0.0
)

fun ProductWithAmount.getTotalPrice() : Double {
    return (productDetails.price.toDoubleOrNull() ?: 0.0) * amount
}

fun ProductWithAmount.formattedTotalPrice() : String {
    return NumberFormat.getCurrencyInstance().format(getTotalPrice())
}

fun Map<ProductDetails, Int>.toList() : List<ProductWithAmount> {
    return this.map {
        ProductWithAmount(it.key, it.value)
    }
}