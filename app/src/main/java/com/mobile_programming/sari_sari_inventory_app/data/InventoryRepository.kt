package com.mobile_programming.sari_sari_inventory_app.data

import com.mobile_programming.sari_sari_inventory_app.data.entity.Product
import com.mobile_programming.sari_sari_inventory_app.data.entity.Receipt
import com.mobile_programming.sari_sari_inventory_app.data.relation.ProductSale
import com.mobile_programming.sari_sari_inventory_app.data.relation.RevenueOnDate
import kotlinx.coroutines.flow.Flow
import java.util.Date

interface InventoryRepository {
    // Product Functions
    suspend fun insertProduct(product: Product) : Long

    suspend fun updateProduct(product: Product)

    suspend fun deleteProduct(product: Product)

    suspend fun getAllProducts() : Flow<List<Product>>

    suspend fun getProduct(id: Long) : Flow<Product>

    suspend fun getProduct(nameOrNumber: String) : Flow<List<Product>>

    // Receipt Functions
    suspend fun insertReceipt(receipt: Receipt, products: Map<Product, Int>) : Long

    suspend fun deleteReceipt(receipt: Receipt)

    suspend fun getAllReceipts() : Flow<List<Receipt>>

    suspend fun getReceipt(id: Long) : Flow<Receipt>

    suspend fun getProductsInReceipt(id: Long) : Map<Product, Int>

    suspend fun getReceiptTotalCost(id: Long) : Double

    // Statistics-Related Functions
    suspend fun getProductSalesFromDates(
        id: Long?,
        dateFrom: Date,
        dateTo: Date
    ) : List<ProductSale>

    suspend fun getRevenueFromDates(
        dateFrom: Date,
        dateTo: Date
    ) : List<RevenueOnDate>
}