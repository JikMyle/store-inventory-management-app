package com.mobile_programming.sari_sari_inventory_app.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mobile_programming.sari_sari_inventory_app.data.entity.Receipt
import com.mobile_programming.sari_sari_inventory_app.data.relation.ProductsPerReceipt
import com.mobile_programming.sari_sari_inventory_app.data.relation.RevenueOnDate
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface ReceiptDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertReceipt(receipt: Receipt) : Long

    @Delete
    suspend fun deleteReceipt(receipt: Receipt)

    @Query("SELECT * FROM receipts ")
    fun getAllReceipts() : Flow<List<Receipt>>

    @Query("SELECT * FROM receipts WHERE id = :id ")
    fun getReceipt(id: Long) : Flow<Receipt>

    /**
     * Attaches a product to the receipt via an associative entity
     */
    @Query(
        "INSERT INTO productsperreceipt (productId, receiptID, amount) " +
        "VALUES (:productId, :receiptId, :amount)"
    )
    suspend fun insertProductToReceipt(productId: Long, receiptId: Long, amount: Int)

    /**
     * Retrieves a list of ProductsPerReceipt objects containing the receipt Id.
     */
    @Query("SELECT * FROM productsperreceipt WHERE receiptId = :id")
    fun getListOfProducts(id: Long) : List<ProductsPerReceipt>

    @Query("SELECT SUM(ppr.amount * p.price) FROM productsperreceipt ppr " +
            "INNER JOIN products p ON ppr.productId = p.id " +
            "WHERE ppr.receiptId = :id ")
    fun getTotalCost(id: Long) : Double

    @Query("SELECT r.dateCreated as date, SUM(ppr.amount * p.price) as totalRevenue " +
            "FROM receipts r " +
            "INNER JOIN productsperreceipt ppr ON r.id = ppr.receiptId " +
            "INNER JOIN products p ON ppr.productId = p.id " +
            "WHERE r.dateCreated >= :dateFrom " +
            "AND r.dateCreated < :dateTo " +
            "GROUP BY r.dateCreated")
    fun getRevenueFromDates(dateFrom: Date, dateTo: Date) : List<RevenueOnDate>
}