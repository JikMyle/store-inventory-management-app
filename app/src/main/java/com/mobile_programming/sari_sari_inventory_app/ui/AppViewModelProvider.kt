package com.mobile_programming.sari_sari_inventory_app.ui

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.mobile_programming.sari_sari_inventory_app.InventoryApplication
import com.mobile_programming.sari_sari_inventory_app.ui.product.ProductEntryViewModel

object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer {
            ProductEntryViewModel(inventoryApplication().container.inventoryRepository)
        }
    }
}

fun CreationExtras.inventoryApplication() : InventoryApplication =
    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as InventoryApplication)