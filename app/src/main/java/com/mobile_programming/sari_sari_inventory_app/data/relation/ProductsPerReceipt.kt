package com.mobile_programming.sari_sari_inventory_app.data.relation

import androidx.room.Entity
import androidx.room.ForeignKey
import com.mobile_programming.sari_sari_inventory_app.data.entity.Product
import com.mobile_programming.sari_sari_inventory_app.data.entity.Receipt
import java.util.Date

@Entity(
    tableName = "ProductsPerReceipt",
    primaryKeys = ["productId", "receiptId"],
    foreignKeys = [
        ForeignKey(
            entity = Receipt::class,
            parentColumns = ["id"],
            childColumns = ["receiptId"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Product::class,
            parentColumns = ["id"],
            childColumns = ["productId"],
            onDelete = ForeignKey.NO_ACTION,
            onUpdate = ForeignKey.CASCADE
        )
    ]
)
data class ProductsPerReceipt(
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