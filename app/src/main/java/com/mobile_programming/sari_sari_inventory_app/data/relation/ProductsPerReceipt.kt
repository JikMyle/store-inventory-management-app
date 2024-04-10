package com.mobile_programming.sari_sari_inventory_app.data.relation

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.mobile_programming.sari_sari_inventory_app.data.entity.Receipt
import java.util.Date

@Entity(
    tableName = "ProductsPerReceipt",
    indices = [Index(value = ["productId", "receiptId"])],
    foreignKeys = [
        ForeignKey(
            entity = Receipt::class,
            parentColumns = ["id"],
            childColumns = ["receiptId"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
//        ForeignKey(
//            entity = Product::class,
//            parentColumns = ["id"],
//            childColumns = ["productId"],
//            onDelete = ForeignKey.NO_ACTION,
//            onUpdate = ForeignKey.CASCADE
//        )

        /*
        * I wanted the row to not be deleted when the reference
        * product is deleted but throws foreign key constraint
        * failed. Not setting it as a foreign key works, but
        * may result in future problems.
        * */
    ]
)
data class ProductsPerReceipt(
    @PrimaryKey val id: Long = 0,
    val productId: Long,
    val receiptId: Long,
    val amount: Int,
)

data class RevenueOnDate(
    val date: Date,
    val totalRevenue: Double
)

data class ProductSale(
    val productId: Long,
    val amountSold: Int,
    val totalRevenue: Double,
)