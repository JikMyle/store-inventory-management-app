package com.mobile_programming.sari_sari_inventory_app.ui

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.mobile_programming.sari_sari_inventory_app.InventoryApplication
import com.mobile_programming.sari_sari_inventory_app.ui.product.ProductEntryViewModel
import com.mobile_programming.sari_sari_inventory_app.ui.receipt.ReceiptScannerViewModel
import com.mobile_programming.sari_sari_inventory_app.ui.receipt.ReceiptViewModel
import com.mobile_programming.sari_sari_inventory_app.ui.scanner.StockUpScannerViewModel

object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer {
            ProductEntryViewModel(inventoryApplication().container.inventoryRepository)
        }

        initializer {
            StockUpScannerViewModel(inventoryApplication().container.inventoryRepository)
        }

        initializer {
            ReceiptViewModel(inventoryApplication().container.inventoryRepository)
        }

        initializer {
            ReceiptScannerViewModel(
                inventoryApplication().container.inventoryRepository
            )
        }
    }
}

fun CreationExtras.inventoryApplication() : InventoryApplication =
    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as InventoryApplication)