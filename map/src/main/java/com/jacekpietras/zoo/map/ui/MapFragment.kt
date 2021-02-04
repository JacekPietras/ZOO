package com.jacekpietras.zoo.map.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.jacekpietras.zoo.core.binding.viewBinding
import com.jacekpietras.zoo.map.R
import com.jacekpietras.zoo.map.databinding.FragmentMapBinding
import com.jacekpietras.zoo.map.viewmodel.MapViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class MapFragment : Fragment(R.layout.fragment_map) {

    private val viewModel by viewModel<MapViewModel>()
    private val binding: FragmentMapBinding by viewBinding(FragmentMapBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setObservers()
    }

    private fun setObservers() {
        lifecycleScope.launch {
            viewModel.viewState.userPosition.collect {
                binding.mapView.userPosition = it
            }
        }
        lifecycleScope.launch {
            viewModel.viewState.mapData.collect {
                binding.mapView.objectList = it
            }
        }
    }
}