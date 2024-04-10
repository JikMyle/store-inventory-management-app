package com.mobile_programming.sari_sari_inventory_app.ui.receipt

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBarsIgnoringVisibility
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mobile_programming.sari_sari_inventory_app.R
import com.mobile_programming.sari_sari_inventory_app.data.entity.Product
import com.mobile_programming.sari_sari_inventory_app.ui.AppViewModelProvider
import com.mobile_programming.sari_sari_inventory_app.ui.product.formattedPrice
import com.mobile_programming.sari_sari_inventory_app.ui.product.toProduct
import com.mobile_programming.sari_sari_inventory_app.ui.product.toProductDetails
import com.mobile_programming.sari_sari_inventory_app.ui.scanner.BarcodeScanner
import com.mobile_programming.sari_sari_inventory_app.ui.scanner.BarcodeScannerSearchBar
import com.mobile_programming.sari_sari_inventory_app.ui.scanner.BottomSheetProductDetailsRow
import com.mobile_programming.sari_sari_inventory_app.ui.scanner.NewProductDialog
import com.mobile_programming.sari_sari_inventory_app.ui.scanner.ProductWithAmount
import com.mobile_programming.sari_sari_inventory_app.ui.scanner.ScannerUiState
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
                    uiState.value.productWithAmount
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
            ReceiptScannerBottomSheet(
                productWithAmount = uiState.value.productWithAmount,
                sheetState = sheetState,
                onDismissRequest = onBottomSheetDismiss,
                onValueChange = onBottomSheetValueChange,
                onButtonClick = onBottomSheetButtonClick,
                isInputValid = uiState.value.isInputValid
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ReceiptScannerBottomSheet(
    modifier: Modifier = Modifier,
    productWithAmount: ProductWithAmount,
    sheetState: SheetState,
    onDismissRequest: () -> Unit,
    onValueChange: (String) -> Unit,
    onButtonClick: () -> Unit,
    isInputValid: Boolean,
) {
    ModalBottomSheet(
        onDismissRequest = { onDismissRequest() },
        sheetState = sheetState,
        windowInsets = WindowInsets.ime,
        modifier = modifier
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(
                dimensionResource(R.dimen.padding_medium)
            ),
            modifier = Modifier.padding(
                horizontal = dimensionResource(R.dimen.padding_extra_large)
            )
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(
                    dimensionResource(R.dimen.padding_small)
                )
            ) {
                BottomSheetProductDetailsRow(
                    labelResId = R.string.product_number,
                    productDetail = productWithAmount.productDetails.productNumber,
                    modifier = Modifier.fillMaxWidth()
                )

                BottomSheetProductDetailsRow(
                    labelResId = R.string.product_name,
                    productDetail = productWithAmount.productDetails.productName,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(
                        dimensionResource(R.dimen.padding_large)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    BottomSheetProductDetailsRow(
                        labelResId = R.string.product_price,
                        productDetail = productWithAmount.productDetails
                            .toProduct()
                            .formattedPrice()
                    )

                    BottomSheetProductDetailsRow(
                        labelResId = R.string.product_stock,
                        productDetail = productWithAmount.productDetails.stock,
                    )
                }
            }

            OutlinedTextField(
                value = productWithAmount.amount.toString(),
                onValueChange = { onValueChange(it) },
                label = {
                    Text(
                        stringResource(
                            R.string.amount
                        )
                    )
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                ),
                modifier = Modifier
                    .fillMaxWidth()
            )

            Button(
                onClick = onButtonClick,
                enabled = isInputValid,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = dimensionResource(
                            R.dimen.padding_extra_large
                        )
                    )
                    .clip(MaterialTheme.shapes.large)
            ) {
                Text(
                    text = stringResource(R.string.add_to_receipt)
                )
            }

            Spacer(
                modifier = Modifier.windowInsetsBottomHeight(
                    WindowInsets.navigationBarsIgnoringVisibility
                )
            )
        }
    }
}