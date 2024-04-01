package com.mobile_programming.sari_sari_inventory_app.data.relation

import androidx.room.Entity
import java.util.Date

@Entity(
    primaryKeys = ["productId", "receiptId"]
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