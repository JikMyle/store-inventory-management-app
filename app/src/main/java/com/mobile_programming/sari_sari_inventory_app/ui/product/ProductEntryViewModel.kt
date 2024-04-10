package com.mobile_programming.sari_sari_inventory_app.ui.product

import androidx.lifecycle.ViewModel
import com.mobile_programming.sari_sari_inventory_app.data.InventoryRepository
import com.mobile_programming.sari_sari_inventory_app.data.entity.Product
import java.text.NumberFormat


class ProductEntryViewModel(
    private val inventoryRepository: InventoryRepository
) : ViewModel() {

}

data class ProductDetails(
    val id: String = "",
    val productNumber : String = "",
    val productName: String = "",
    val price: String = "",
    val stock: String = "",
)

fun ProductDetails.toProduct() : Product {
    return Product(
        id = id.toLongOrNull() ?: 0,
        productNumber = productNumber,
        productName = productName,
        price = price.toDoubleOrNull() ?: 0.0,
        stock = stock.toIntOrNull() ?: 0
    )
}

fun Product.toProductDetails() : ProductDetails {
    return ProductDetails(
        id = id.toString(),
        productNumber = productNumber ?: "",
        productName = productName,
        price = price.toString(),
        stock = stock.toString()
    )
}

fun Product.formattedPrice(): String {
    return NumberFormat.getCurrencyInstance().format(price)
}