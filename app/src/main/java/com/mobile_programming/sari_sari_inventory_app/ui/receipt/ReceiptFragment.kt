package com.mobile_programming.sari_sari_inventory_app.ui.receipt

import android.content.Context
import android.os.Bundle
import android.text.Html
import android.text.Spanned
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.mobile_programming.sari_sari_inventory_app.MainActivity
import com.mobile_programming.sari_sari_inventory_app.R
import com.mobile_programming.sari_sari_inventory_app.databinding.FragmentReceiptBinding
import com.mobile_programming.sari_sari_inventory_app.ui.AppViewModelProvider
import com.mobile_programming.sari_sari_inventory_app.ui.scanner.ProductWithAmount
import kotlinx.coroutines.launch
import java.text.NumberFormat

class ReceiptFragment : Fragment() {
    private lateinit var binding: FragmentReceiptBinding
    private lateinit var navController: NavController

    private val viewModel: ReceiptViewModel by viewModels(
        ownerProducer = { requireActivity() },
        factoryProducer = { AppViewModelProvider.Factory }
    )

    companion object {
        const val MODAL_BOTTOM_SHEET_TAG = "ReceiptModalBottomSheet"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val context = requireContext()

        binding = FragmentReceiptBinding.inflate(layoutInflater)
        navController = (activity as MainActivity).findNavController(R.id.main_nav_host_fragment)

        val adapter = ReceiptListAdapter(
            onItemClick = { productWithAmount ->
                toggleBottomSheet(
                    productWithAmount = productWithAmount,
                    isVisible = true
                )
            },
            onRemoveClick = { productDetails ->
                viewModel.removeProductFromReceipt(productDetails)
            }
        )

        binding.receiptRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.receiptRecyclerView.adapter = adapter
        binding.receiptRecyclerView.itemAnimator = null
        adapter.submitList(viewModel.uiState.value.receiptMap.toList())

        viewModel.uiState.asLiveData().observe(
            viewLifecycleOwner
        ) {
            adapter.submitList(viewModel.uiState.value.receiptMap.toList())

            val total: String = getString(
                R.string.receipt_total,
                formatPrice(viewModel.uiState.value.total)
            )
            val styledTotal: Spanned = Html.fromHtml(total)

            binding.receiptTotal.text = styledTotal
        }

        binding.topAppToolbar.setNavigationOnClickListener {
            navController.popBackStack()
        }

        binding.topAppToolbar.setOnMenuItemClickListener { menuItem ->
            onTopAppBarMenuItemClick(menuItem)
        }

        binding.addProductFAB.setOnClickListener {
            navController.navigate(
                ReceiptFragmentDirections.receiptToReceiptScanner()
            )
        }

        binding.confirmButton.isEnabled = viewModel.uiState.value.receiptMap.isNotEmpty()
        binding.confirmButton.setOnClickListener {
            showSaleConfirmationDialog(context)
        }

        return binding.root
    }

    private fun onTopAppBarMenuItemClick(
        menuItem: MenuItem
    ): Boolean {

        return when (menuItem.itemId) {

            R.id.clear_receipt -> {
                showClearReceiptDialog(context = requireContext())
                true
            }

            else -> false
        }
    }

    private fun formatPrice(price: Double): String {
        return NumberFormat.getCurrencyInstance().format(price)
    }

    private fun toggleBottomSheet(
        productWithAmount: ProductWithAmount,
        isVisible: Boolean
    ) {
        viewModel.toggleBottomSheet(
            productWithAmount = productWithAmount,
            isVisible = isVisible
        )

        val modalBottomSheet = ReceiptModalBottomSheetFragment()
        modalBottomSheet.show(
            childFragmentManager,
            MODAL_BOTTOM_SHEET_TAG
        )
    }

    private fun showClearReceiptDialog(
        context: Context,
    ) {
        MaterialAlertDialogBuilder(context)
            .setTitle(R.string.clear_receipt_dialog_title)
            .setMessage(R.string.clear_receipt_dialog_message)
            .setPositiveButton(R.string.clear) { _, _ ->
                viewModel.clearReceipt()
            }
            .setNeutralButton(R.string.cancel) { _, _ -> }
            .show()
    }

    private fun showSaleConfirmationDialog(
        context: Context
    ) {
        MaterialAlertDialogBuilder(context)
            .setTitle(R.string.confirm_sale_dialog_title)
            .setMessage(R.string.confirm_sale_dialog_message)
            .setPositiveButton(R.string.confirm) { _, _ ->
                lifecycleScope.launch {
                    viewModel.confirmSale()
                    showSaleConfirmedToast(context)
                }
            }
            .setNegativeButton(R.string.clear) { _, _ ->
                viewModel.clearReceipt()
            }
            .setNeutralButton(R.string.cancel) { _, _ -> }
            .show()
    }

    private fun showSaleConfirmedToast(
        context: Context
    ) {
        Toast.makeText(
            context,
            context.getString(R.string.sale_confirmed),
            Toast.LENGTH_LONG
        ).show()
    }
}