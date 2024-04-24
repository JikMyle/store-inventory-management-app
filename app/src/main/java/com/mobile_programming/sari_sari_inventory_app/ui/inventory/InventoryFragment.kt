package com.mobile_programming.sari_sari_inventory_app.ui.inventory

import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import androidx.annotation.MenuRes
import androidx.appcompat.widget.PopupMenu
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.mobile_programming.sari_sari_inventory_app.MainActivity
import com.mobile_programming.sari_sari_inventory_app.R
import com.mobile_programming.sari_sari_inventory_app.databinding.FragmentInventoryBinding
import com.mobile_programming.sari_sari_inventory_app.ui.AppViewModelProvider
import com.mobile_programming.sari_sari_inventory_app.ui.product.toProductDetails
import com.mobile_programming.sari_sari_inventory_app.utils.SortingType
import kotlinx.coroutines.launch

class InventoryFragment : Fragment() {
    private lateinit var binding: FragmentInventoryBinding
    private lateinit var navController: NavController
    private val viewModel: InventoryViewModel by viewModels { AppViewModelProvider.Factory }

    private var originalSoftInputMode: Int? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentInventoryBinding.inflate(layoutInflater)
        navController = (activity as MainActivity).findNavController(R.id.main_nav_host_fragment)

        val adapter = InventoryListAdapter(
            onItemClick = { productId ->
                navController.navigate(
                    InventoryFragmentDirections.inventoryToProductDetails(
                        productId
                    )
                )
            },
            onDeleteClick = { product ->
                lifecycleScope.launch {
                    viewModel.deleteProduct(product)
                    product.toProductDetails().imageUri?.let {
                        requireContext().contentResolver.delete(
                            it,
                            null,
                            null
                        )
                    }
                }
            }
        )

        binding.inventoryRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.inventoryRecyclerView.adapter = adapter
        binding.inventoryRecyclerView.itemAnimator = null
        adapter.submitList(viewModel.uiState.value.productList)

        viewModel.uiState.asLiveData().observe(
            viewLifecycleOwner
        ) {
            adapter.submitList(viewModel.uiState.value.productList)
        }

        binding.topAppToolbar.setNavigationOnClickListener {
            navController.popBackStack()
        }

        binding.botNavBar.selectedItemId = R.id.navigate_inventory
        binding.botNavBar.setOnItemSelectedListener { onNavItemClick(it) }

        binding.sortMenuButton.setOnClickListener { view: View ->
            showPopupMenu(view, R.menu.sorting_type_menu)
        }

        val searchBar = binding.searchBar.editText

        searchBar?.doAfterTextChanged {
            viewModel.uiState.value.searchBarState.onQueryChange(it.toString())
        }

        searchBar?.setOnKeyListener { _, keyCode, event ->
            if(event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                viewModel.uiState.value.searchBarState.onSearch(
                    viewModel.uiState.value.searchBarState.searchQuery
                )
                true
            } else {
                false
            }
        }

        binding.addProductFAB.setOnClickListener {
            navController.navigate(
                InventoryFragmentDirections.inventoryToStockUpScanner()
            )
        }

        return binding.root
    }

    override fun onResume() {
        originalSoftInputMode = activity?.window?.getSoftInputMode()
        activity?.window?.attributes?.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN
        super.onResume()
    }

    override fun onPause() {
        activity?.window?.attributes?.softInputMode = originalSoftInputMode
        super.onPause()
    }

    private fun onNavItemClick(navItem: MenuItem) : Boolean {
        return when(navItem.itemId) {
            R.id.navigate_home -> {
                navController.navigate(InventoryFragmentDirections.inventoryToHome())
                false
            }

            R.id.navigate_inventory -> true

            R.id.navigate_receipt -> {
                navController.navigate(InventoryFragmentDirections.inventoryToReceipt())
                false
            }

            R.id.navigate_settings -> false

            else -> false
        }
    }

    private fun showPopupMenu(view: View, @MenuRes menuRes: Int) {
        val popup = PopupMenu(requireContext(), view)
        popup.menuInflater.inflate(menuRes, popup.menu)

        popup.setOnMenuItemClickListener { menuItem: MenuItem ->
            when(menuRes) {
                R.menu.sorting_type_menu -> changeSortingType(menuItem)
                else -> false
            }
        }
        popup.setOnDismissListener {
            popup.dismiss()
        }
        // Show the popup menu.
        popup.show()
    }

    private fun changeSortingType(menuItem: MenuItem) : Boolean {
        val ascending = !viewModel.uiState.value.isSortAscending

        return when(menuItem.itemId) {
            R.id.sortByProductName -> {
                viewModel.changeSortingType(
                    sortingType = SortingType.ByProductName,
                    ascending = ascending
                )
                true
            }

            R.id.sortByProductNumber -> {
                viewModel.changeSortingType(
                    sortingType = SortingType.ByProductNumber,
                    ascending = ascending
                )
                true
            }

            R.id.sortByProductPrice -> {
                viewModel.changeSortingType(
                    sortingType = SortingType.ByProductName,
                    ascending = ascending
                )
                true
            }

            R.id.sortByProductStock -> {
                viewModel.changeSortingType(
                    sortingType = SortingType.ByStock,
                    ascending = ascending
                )
                true
            }

            else -> false
        }
    }
}

fun Window.getSoftInputMode() : Int {
    return attributes.softInputMode
}