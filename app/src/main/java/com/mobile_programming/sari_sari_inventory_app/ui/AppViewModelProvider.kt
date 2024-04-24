package com.mobile_programming.sari_sari_inventory_app.ui

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.mobile_programming.sari_sari_inventory_app.InventoryApplication
import com.mobile_programming.sari_sari_inventory_app.ui.home.HomeViewModel
import com.mobile_programming.sari_sari_inventory_app.ui.inventory.InventoryViewModel
import com.mobile_programming.sari_sari_inventory_app.ui.product.ProductDetailsViewModel
import com.mobile_programming.sari_sari_inventory_app.ui.product.ProductEntryViewModel
import com.mobile_programming.sari_sari_inventory_app.ui.receipt.ReceiptScannerViewModel
import com.mobile_programming.sari_sari_inventory_app.ui.receipt.ReceiptViewModel
import com.mobile_programming.sari_sari_inventory_app.ui.scanner.StockUpScannerViewModel

object AppViewModelProvider {
    val Factory = viewModelFactory {

        initializer {
            HomeViewModel(
                inventoryApplication().container.inventoryRepository
            )
        }

        initializer {
            ProductEntryViewModel(
                this.createSavedStateHandle(),
                inventoryApplication().container.inventoryRepository
            )
        }

        initializer {
            ProductDetailsViewModel(
                this.createSavedStateHandle(),
                inventoryApplication().container.inventoryRepository
            )
        }

        initializer {
            InventoryViewModel(inventoryApplication().container.inventoryRepository)
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