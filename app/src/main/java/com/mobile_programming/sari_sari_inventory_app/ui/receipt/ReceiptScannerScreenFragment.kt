package com.mobile_programming.sari_sari_inventory_app.ui.receipt

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.mobile_programming.sari_sari_inventory_app.R
import com.mobile_programming.sari_sari_inventory_app.databinding.FragmentReceiptScannerBinding
import com.mobile_programming.sari_sari_inventory_app.ui.AppViewModelProvider
import com.mobile_programming.sari_sari_inventory_app.ui.theme.SariSariInventoryAppTheme

class ReceiptScannerScreenFragment : Fragment() {
    private lateinit var binding: FragmentReceiptScannerBinding
    private lateinit var scannerViewModel: ReceiptScannerViewModel
    private lateinit var receiptViewModel: ReceiptViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = DataBindingUtil.inflate(
            layoutInflater,
            R.layout.fragment_receipt_scanner,
            container,
            false
        )

        val factory = AppViewModelProvider.Factory
        val viewModelProvider = ViewModelProvider(requireActivity(), factory)
        scannerViewModel = viewModelProvider[ReceiptScannerViewModel::class.java]
        receiptViewModel = viewModelProvider[ReceiptViewModel::class.java]

        binding.receiptScannerScreenComposeView.setContent {
            SariSariInventoryAppTheme {
                ReceiptScannerScreen(
                    scannerViewModel = scannerViewModel,
                    receiptViewModel = receiptViewModel,
                    navigateToProductEntry = { }
                )
            }
        }

        return binding.root
    }
}