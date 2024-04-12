package com.mobile_programming.sari_sari_inventory_app.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.mobile_programming.sari_sari_inventory_app.data.entity.Product
import com.mobile_programming.sari_sari_inventory_app.data.relation.ProductSale
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface ProductDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertProduct(product: Product) : Long

    @Delete
    suspend fun deleteProduct(product: Product)

    @Update
    suspend fun updateProduct(product: Product)

    @Query("SELECT * FROM products")
    fun getAllProducts() : Flow<List<Product>>

    @Query("SELECT * FROM products WHERE id = :id")
    fun getProduct(id: Long) : Flow<Product?>

    @Query("SELECT * FROM products WHERE productNumber = :productNumber")
    fun getProductByNumber(productNumber: String) : Flow<Product?>

    @Query("SELECT * FROM products WHERE productName = :productName")
    fun getProductByName(productName: String) : Flow<Product?>

    // I used a RawQuery here because Rooms do not take column names as parameters
    // Re: Instead of RawQuery, I just used a normal query, have the list be sorted in memory
    @Query("SELECT * FROM products " +
            "WHERE productName LIKE '%' || :nameOrNumber || '%' " +
            "OR productNumber LIKE '%' || :nameOrNumber || '%' ")
    fun searchForProduct(nameOrNumber: String) : Flow<List<Product>>

    @Query("SELECT p.id as productId, SUM(ppr.amount) as amountSold, " +
            "SUM(p.price * ppr.amount) as totalRevenue " +
            "FROM products p " +
            "INNER JOIN productsperreceipt ppr ON p.id = ppr.productId " +
            "INNER JOIN receipts r ON ppr.receiptId = r.id " +
            "WHERE r.dateCreated >= :dateFrom " +
            "AND r.dateCreated < :dateTo " +
            "AND ifnull(p.id = :id, 1) " +
            "GROUP BY p.id")
    fun getSalesFromDates(id: Long?, dateFrom: Date, dateTo: Date) : Flow<List<ProductSale>>
}