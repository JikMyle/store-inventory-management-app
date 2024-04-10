package com.mobile_programming.sari_sari_inventory_app.ui.receipt

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mobile_programming.sari_sari_inventory_app.R
import com.mobile_programming.sari_sari_inventory_app.data.entity.Product
import com.mobile_programming.sari_sari_inventory_app.ui.AppViewModelProvider
import com.mobile_programming.sari_sari_inventory_app.ui.product.toProduct
import com.mobile_programming.sari_sari_inventory_app.ui.product.toProductDetails
import com.mobile_programming.sari_sari_inventory_app.ui.scanner.BarcodeScanner
import com.mobile_programming.sari_sari_inventory_app.ui.scanner.BarcodeScannerSearchBar
import com.mobile_programming.sari_sari_inventory_app.ui.scanner.NewProductDialog
import com.mobile_programming.sari_sari_inventory_app.ui.scanner.ScannerUiState
import com.mobile_programming.sari_sari_inventory_app.ui.scanner.StockUpBottomSheet
import kotlinx.coroutines.launch

@Composable
fun ReceiptScannerScreen(
    modifier: Modifier = Modifier,
    scannerViewModel: ReceiptScannerViewModel = viewModel(factory = AppViewModelProvider.Factory),
    receiptViewModel: ReceiptViewModel = viewModel(factory = AppViewModelProvider.Factory),
    navigateToProductEntry: (String) -> Unit,
) {
    val uiState = scannerViewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    ReceiptScannerBody(
        uiState = uiState,
        onResultClick = {
            scannerViewModel.updateProductWithAmount(
                productDetails = it.toProductDetails()
            )
            scannerViewModel.toggleBottomSheet(true)
            scannerViewModel.toggleSearchBar(false)
        },
        onBottomSheetDismiss = {
            scannerViewModel.toggleBottomSheet(false)
        },
        onBottomSheetValueChange = {
            scannerViewModel.updateProductWithAmount(
                amount = it.toIntOrNull() ?: 0
            )
        },
        onBottomSheetButtonClick = {
            scope.launch {
                receiptViewModel.addProductToReceipt(
                    uiState.value.productWithAmount.productDetails.toProduct()
                )
                scannerViewModel.toggleBottomSheet(false)
                Toast.makeText(
                    context,
                    "Product added to receipt!",
                    Toast.LENGTH_SHORT
                ).show()
            }
        },
        onDialogDismiss = scannerViewModel::clearBarcodeScanned,
        navigateToProductEntry = navigateToProductEntry,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiptScannerBody(
    modifier: Modifier = Modifier,
    uiState: State<ScannerUiState>,
    onResultClick: (Product) -> Unit,
    onBottomSheetDismiss: () -> Unit,
    onBottomSheetValueChange: (String) -> Unit,
    onBottomSheetButtonClick: () -> Unit,
    onDialogDismiss: () -> Unit,
    navigateToProductEntry: (String) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState()

    Box(modifier = modifier.fillMaxSize()) {
        if (!uiState.value.searchBarState.isActive) {
            BarcodeScanner(
                scannerCameraState = uiState.value.scannerCameraState
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .align(Alignment.BottomCenter)
        ) {
            if (!uiState.value.searchBarState.isActive) {
                Text(
                    text = stringResource(R.string.search_manually),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            }

            BarcodeScannerSearchBar(
                searchBarState = uiState.value.searchBarState,
                onResultClick = onResultClick,
                navigateToProductEntry = { navigateToProductEntry("") },
                modifier = Modifier.then(
                    if (!uiState.value.searchBarState.isActive) {
                        Modifier.padding(
                            bottom = dimensionResource(R.dimen.padding_small),
                            start = dimensionResource(R.dimen.padding_small),
                            end = dimensionResource(R.dimen.padding_small)
                        )
                    } else {
                        Modifier
                    }
                )
            )
        }

        if (uiState.value.isBottomSheetVisible) {
            StockUpBottomSheet(
                productWithAmount = uiState.value.productWithAmount,
                sheetState = sheetState,
                onDismissRequest = onBottomSheetDismiss,
                onValueChange = onBottomSheetValueChange,
                onButtonClick = onBottomSheetButtonClick
            )
        }

        if (uiState.value.isNewProduct) {
            NewProductDialog(
                onDismissRequest = onDialogDismiss,
                navigateToProductEntry = {
                    navigateToProductEntry(
                        uiState.value.scannedBarcode
                    )
                }
            )
        }
    }
}