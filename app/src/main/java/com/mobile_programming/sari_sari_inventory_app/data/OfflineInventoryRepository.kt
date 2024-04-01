package com.mobile_programming.sari_sari_inventory_app.data

import com.mobile_programming.sari_sari_inventory_app.data.dao.ProductDao
import com.mobile_programming.sari_sari_inventory_app.data.dao.ReceiptDao
import com.mobile_programming.sari_sari_inventory_app.data.entity.Product
import com.mobile_programming.sari_sari_inventory_app.data.entity.Receipt
import com.mobile_programming.sari_sari_inventory_app.data.relation.ProductSale
import com.mobile_programming.sari_sari_inventory_app.data.relation.RevenueOnDate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import java.util.Date

class OfflineInventoryRepository(
    private val productDao: ProductDao,
    private val receiptDao: ReceiptDao
) : InventoryRepository {
    override suspend fun insertProduct(product: Product) : Long {
        return productDao.insertProduct(product)
    }

    override suspend fun updateProduct(product: Product) {
        productDao.updateProduct(product)
    }

    override suspend fun deleteProduct(product: Product) {
        productDao.deleteProduct(product)
    }

    override suspend fun getAllProducts(): Flow<List<Product>> {
        return productDao.getAllProducts()
    }

    override suspend fun getProduct(id: Long): Flow<Product> {
        return productDao.getProduct(id)
    }

    override suspend fun getProduct(nameOrNumber: String): Flow<List<Product>> {
        return productDao.getProduct(nameOrNumber)
    }

    override suspend fun insertReceipt(receipt: Receipt, products: Map<Product, Int>) : Long {
        val id = receiptDao.insertReceipt(receipt)

        products.forEach {
            receiptDao.insertProductToReceipt(it.key.id, id, it.value)
        }

        return id
    }

    override suspend fun deleteReceipt(receipt: Receipt) {
        receiptDao.deleteReceipt(receipt)
    }

    override suspend fun getAllReceipts(): Flow<List<Receipt>> {
        return receiptDao.getAllReceipts()
    }

    override suspend fun getReceipt(id: Long): Flow<Receipt> {
        return receiptDao.getReceipt(id)
    }

    // Is it better to return a map of <Object(Product), Int> or <Long(ProductId), Int)>?
    override suspend fun getProductsInReceipt(id: Long): Map<Product, Int> {
        return receiptDao.getListOfProducts(id).associateBy (
            { productDao.getProduct(it.productId).filterNotNull().first() },
            { it.amount }
        )
    }

    override suspend fun getReceiptTotalCost(id: Long): Double {
        return receiptDao.getTotalCost(id)
    }

    override suspend fun getProductSalesFromDates(
        id: Long?,
        dateFrom: Date,
        dateTo: Date
    ): List<ProductSale> {
        return productDao.getSalesFromDates(id, dateFrom, dateTo)
    }

    override suspend fun getRevenueFromDates(
        dateFrom: Date,
        dateTo: Date
    ): List<RevenueOnDate> {
        return receiptDao.getRevenueFromDates(dateFrom, dateTo)
    }

}