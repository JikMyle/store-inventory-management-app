package com.mobile_programming.sari_sari_inventory_app.data

import com.mobile_programming.sari_sari_inventory_app.data.dao.ProductDao
import com.mobile_programming.sari_sari_inventory_app.data.dao.ReceiptDao
import com.mobile_programming.sari_sari_inventory_app.data.entity.Product
import com.mobile_programming.sari_sari_inventory_app.data.entity.Receipt
import com.mobile_programming.sari_sari_inventory_app.data.relation.ProductSale
import com.mobile_programming.sari_sari_inventory_app.data.relation.RevenueOnDate
import kotlinx.coroutines.flow.Flow
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

    override fun getAllProducts(): Flow<List<Product>> {
        return productDao.getAllProducts()
    }

    override fun getProduct(id: Long): Flow<Product?> {
        return productDao.getProduct(id)
    }

    override fun getProductByNumber(productNumber: String): Flow<Product?> {
        return productDao.getProductByNumber(productNumber)
    }

    override fun searchForProduct(nameOrNumber: String): Flow<List<Product>> {
        return productDao.searchForProduct(nameOrNumber)
    }

    override fun checkIfProductNumberExists(productNumber: String): Boolean {
        return productDao.checkIfProductNumberExists(productNumber)
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

    override fun getAllReceipts(): Flow<List<Receipt>> {
        return receiptDao.getAllReceipts()
    }

    override fun getReceipt(id: Long): Flow<Receipt?> {
        return receiptDao.getReceipt(id)
    }

    // Is it better to return a map of <Object(Product), Int> or <Long(ProductId), Int)>?
    // May have problems and break
    override suspend fun getProductsInReceipt(id: Long): Map<Long, Int> {
        return receiptDao.getListOfProducts(id).associateBy (
            { it.productId },
            { it.amount }
        )
    }

    override fun getReceiptTotalCost(id: Long): Double {
        return receiptDao.getTotalCost(id)
    }

    override suspend fun getProductSalesFromDates(
        id: Long?,
        dateFrom: Date,
        dateTo: Date
    ): Flow<List<ProductSale>> {
        return productDao.getSalesFromDates(id, dateFrom, dateTo)
    }

    override suspend fun getRevenueFromDates(
        dateFrom: Date,
        dateTo: Date
    ): Flow<List<RevenueOnDate>> {
        return receiptDao.getRevenueFromDates(dateFrom, dateTo)
    }

}