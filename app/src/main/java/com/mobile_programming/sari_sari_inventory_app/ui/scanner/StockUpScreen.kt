package com.mobile_programming.sari_sari_inventory_app.ui.scanner

import android.widget.Toast
import androidx.annotation.StringRes
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mobile_programming.sari_sari_inventory_app.R
import com.mobile_programming.sari_sari_inventory_app.data.entity.Product
import com.mobile_programming.sari_sari_inventory_app.ui.AppViewModelProvider
import com.mobile_programming.sari_sari_inventory_app.ui.product.toProductDetails
import com.mobile_programming.sari_sari_inventory_app.ui.theme.SariSariInventoryAppTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockUpScannerScreen(
    modifier: Modifier = Modifier,
    viewModel: StockUpViewModel = viewModel(factory = AppViewModelProvider.Factory),
    navigateToProductEntry: (String) -> Unit,
) {
    val uiState = viewModel.uiState.collectAsState()

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState()

    Box(modifier = modifier.fillMaxSize()) {
        if (!uiState.value.searchBarState.isActive) {
            BarcodeScanner(
                scannerState = uiState.value.scannerState,
                onSwitchCamera = viewModel::switchCamera,
                onPermissionResult = viewModel::onPermissionResult,
                onBarcodeScanned = viewModel::onBarcodeScanned,
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .align(Alignment.BottomCenter)
        ) {
            if(!uiState.value.searchBarState.isActive) {
                Text(
                    text = stringResource(R.string.search_manually),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            }

            BarcodeScannerSearchBar(
                searchBarState = uiState.value.searchBarState,
                onResultClick = {
                    viewModel.updateProductWithAmount(
                        productDetails = it.toProductDetails()
                    )
                    viewModel.toggleBottomSheet(true)
                    viewModel.toggleSearchBar(false)
                },
                navigateToProductEntry = { navigateToProductEntry("") },
                modifier = Modifier.then(
                    if(!uiState.value.searchBarState.isActive) {
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
                onDismissRequest = { viewModel.toggleBottomSheet(false) },
                onValueChange = {
                    viewModel.updateProductWithAmount(
                        amount = it.toIntOrNull() ?: 0
                    )
                },
                onButtonClick = {
                    scope.launch {
                        viewModel.increaseProductStock()
                        viewModel.toggleBottomSheet(false)
                        Toast.makeText(
                            context,
                            "Product stock updated!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            )
        }

        if (uiState.value.isNewProduct) {
            NewProductDialog(
                onDismissRequest = viewModel::clearBarcodeScanned,
                navigateToProductEntry = {
                    navigateToProductEntry(uiState.value.scannedBarcode)
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun StockUpBottomSheet(
    modifier: Modifier = Modifier,
    productWithAmount: ProductWithAmount,
    sheetState: SheetState,
    onDismissRequest: () -> Unit,
    onValueChange: (String) -> Unit,
    onButtonClick: () -> Unit,
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = dimensionResource(
                                R.dimen.padding_extra_large
                            )
                        )
                )

                BottomSheetProductDetailsRow(
                    labelResId = R.string.product_name,
                    productDetail = productWithAmount.productDetails.productName,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = dimensionResource(
                                R.dimen.padding_extra_large
                            )
                        )
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = dimensionResource(
                            R.dimen.padding_extra_large
                        )
                    )
            ) {
                BottomSheetProductDetailsRow(
                    labelResId = R.string.product_stock,
                    productDetail = productWithAmount.productDetails.stock,
                )

                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.padding(
                        horizontal = dimensionResource(
                            R.dimen.padding_large
                        )
                    )
                )

                OutlinedTextField(
                    value = productWithAmount.amount.toString(),
                    onValueChange = { onValueChange(it) },
                    label = {
                        Text(
                            stringResource(
                                R.string.amount_to_add
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
            }

            Button(
                onClick = onButtonClick,
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
                    text = stringResource(R.string.add_to_stock)
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

@Composable
fun BottomSheetProductDetailsRow(
    @StringRes labelResId: Int,
    productDetail: String,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.Start,
        modifier = modifier
    ) {
        Text(
            text = stringResource(labelResId),
            style = MaterialTheme.typography.labelSmall
        )
        Text(
            text = productDetail,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            overflow = TextOverflow.Ellipsis,
            maxLines = 2,
        )
    }
}

@Composable
fun NewProductDialog(
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit,
    navigateToProductEntry: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismissRequest,
    ) {
        Card{
            Column(
                modifier = modifier
                    .padding(
                        dimensionResource(R.dimen.padding_large)
                    ),
            ) {
                Text(
                    text = stringResource(R.string.new_product_dialog_message),
                    style = MaterialTheme.typography.bodyMedium,
                )

                Spacer(
                    Modifier.padding(
                        bottom = dimensionResource(R.dimen.padding_large)
                    )
                )

                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {

                    TextButton(
                        onClick = onDismissRequest
                    ) {
                        Text(text = stringResource(R.string.cancel))
                    }

                    TextButton(
                        onClick = navigateToProductEntry
                    ) {
                        Text(text = stringResource(R.string.confirm))
                    }
                }

            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BarcodeSearchResultListPreview() {
    val products = listOf(
        Product(
            id = 0,
            productNumber = "123131312",
            productName = "Young's Town Sardines in Tomato Sauce w/ Hot Chili 155g",
            price = 75.0,
            stock = 100
        ),
        Product(
            id = 0,
            productNumber = "123131312",
            productName = "Young's Town Sardines in Tomato Sauce w/ Hot Chili 155g",
            price = 175.0,
            stock = 10
        )
    )

    SariSariInventoryAppTheme {
        BarcodeSearchResultsList(
            products = products,
            onResultClick = { },
        )
    }
}