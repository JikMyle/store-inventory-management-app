package com.mobile_programming.sari_sari_inventory_app.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.mobile_programming.sari_sari_inventory_app.MainActivity
import com.mobile_programming.sari_sari_inventory_app.R
import com.mobile_programming.sari_sari_inventory_app.databinding.FragmentHomeBinding
import com.mobile_programming.sari_sari_inventory_app.ui.AppViewModelProvider

class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding
    private lateinit var navController: NavController
    private val viewModel: HomeViewModel by viewModels { AppViewModelProvider.Factory }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentHomeBinding.inflate(layoutInflater)
        binding.viewModel = viewModel

        navController = (activity as MainActivity).findNavController(R.id.main_nav_host_fragment)
        binding.botNavBar.setOnItemSelectedListener { onNavItemClick(it) }

        return binding.root
    }

    private fun onNavItemClick(navItem: MenuItem) : Boolean {
        return when(navItem.itemId) {
            R.id.navigate_home -> return true

            R.id.navigate_inventory -> {
                navController.navigate(HomeFragmentDirections.homeToInventory())
                false
            }

            R.id.navigate_receipt -> {
                navController.navigate(HomeFragmentDirections.homeToReceipt())
                false
            }

            R.id.navigate_settings -> false

            else -> false
        }
    }
}
