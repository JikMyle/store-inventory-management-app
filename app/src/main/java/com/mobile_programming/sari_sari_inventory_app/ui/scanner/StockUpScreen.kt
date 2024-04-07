package com.mobile_programming.sari_sari_inventory_app.ui.scanner

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mobile_programming.sari_sari_inventory_app.data.entity.Product
import com.mobile_programming.sari_sari_inventory_app.ui.AppViewModelProvider

@Composable
fun StockUpScannerScreen(
    modifier: Modifier = Modifier,
    viewModel: StockUpViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val uiState = viewModel.uiState.collectAsState()

    Box(modifier = modifier.fillMaxSize()) {
        if (!uiState.value.searchBarState.isActive) {
            BarcodeScanner(
                cameraState = uiState.value.cameraState,
                onSwitchCamera = viewModel::switchCamera,
                onPermissionResult = viewModel::onPermissionResult,
                onBarcodeScanned = viewModel::onBarcodeScanned,
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .align(Alignment.BottomCenter)
        ){
            if(!uiState.value.searchBarState.isActive) {
                Text(
                    text = "or search manually...",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            }

            BarcodeScannerSearchBar(
                searchBarState = uiState.value.searchBarState,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BarcodeScannerSearchBar(
    modifier: Modifier = Modifier,
    searchBarState: SearchBarState<Product>
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (!searchBarState.isActive) {
                    Modifier.padding(
                        start = 8.dp,
                        top = 0.dp,
                        end = 8.dp,
                        bottom = 8.dp
                    )
                } else {
                    Modifier
                }
            )
    ) {
        SearchBar(
            query = searchBarState.searchQuery,
            onQueryChange = { searchBarState.onQueryChange(it) },
            onSearch = { searchBarState.onQueryChange(it) },
            active = searchBarState.isActive,
            onActiveChange = { searchBarState.onActiveChange(it) },
            placeholder = { Text("Enter product number or name") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null
                )
            },
            trailingIcon = {
                if (searchBarState.isActive) {
                    IconButton(onClick = { searchBarState.onActiveChange(false) }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = null
                        )
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 4.dp),
        ) {
            BarcodeSearchResultsList(products = searchBarState.result)
        }
    }
}

@Composable
fun BarcodeSearchResultsList(
    products: List<Product>,
    modifier: Modifier = Modifier
) {
    if (products.isNotEmpty()) {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(products) {
                Text(text = it.productNumber ?: "NO PRODUCT NUMBER")
                Text(text = it.productName)
            }
        }
    } else {
        Text(text = "NO RESULTS")
    }
}