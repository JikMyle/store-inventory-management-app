package com.mobile_programming.sari_sari_inventory_app.ui.scanner

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.mobile_programming.sari_sari_inventory_app.R
import com.mobile_programming.sari_sari_inventory_app.databinding.FragmentBarcodeScannerBinding
import com.mobile_programming.sari_sari_inventory_app.ui.AppViewModelProvider
import com.mobile_programming.sari_sari_inventory_app.ui.theme.SariSariInventoryAppTheme

class StockUpScreenFragment : Fragment() {
    private lateinit var binding: FragmentBarcodeScannerBinding
    private lateinit var viewModel: StockUpViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = DataBindingUtil.inflate(
            layoutInflater,
            R.layout.fragment_barcode_scanner,
            container,
            false
        )

        val factory = AppViewModelProvider.Factory
        val viewModelProvider = ViewModelProvider(requireActivity(), factory)

        viewModel = viewModelProvider[StockUpViewModel::class.java]

        binding.barcodeScannerComposeView.setContent {
            SariSariInventoryAppTheme {
                StockUpScannerScreen(
                    viewModel = viewModel,
                )
            }
        }

        return binding.root
    }
}