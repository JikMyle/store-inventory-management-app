package com.mobile_programming.sari_sari_inventory_app.ui.scanner

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.mobile_programming.sari_sari_inventory_app.MainActivity
import com.mobile_programming.sari_sari_inventory_app.R
import com.mobile_programming.sari_sari_inventory_app.databinding.FragmentStockUpScannerBinding
import com.mobile_programming.sari_sari_inventory_app.ui.AppViewModelProvider
import com.mobile_programming.sari_sari_inventory_app.ui.theme.SariSariInventoryAppTheme

class StockUpScannerScreenFragment : Fragment() {
    private lateinit var binding: FragmentStockUpScannerBinding
    private lateinit var viewModel: StockUpScannerViewModel
    private lateinit var navController: NavController

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = DataBindingUtil.inflate(
            layoutInflater,
            R.layout.fragment_stock_up_scanner,
            container,
            false
        )

        navController = (activity as MainActivity).findNavController(R.id.main_nav_host_fragment)

        val factory = AppViewModelProvider.Factory
        val viewModelProvider = ViewModelProvider(requireActivity(), factory)

        viewModel = viewModelProvider[StockUpScannerViewModel::class.java]

        binding.stockUpScannerComposeView.setContent {
            SariSariInventoryAppTheme {
                StockUpScannerScreen(
                    viewModel = viewModel,
                    canNavigateBack = true,
                    onNavigateUp = { navController.popBackStack() },
                    navigateToProductEntry = {
                        viewModel.toggleSearchBar(false)
                        navController.navigate(
                            StockUpScannerScreenFragmentDirections
                                .actionStockUpScannerScreenFragmentToProductEntryFragment(
                                    it
                                )
                        )
                    }
                )
            }
        }

        return binding.root
    }
}