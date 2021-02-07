package com.jacekpietras.zoo.map.ui

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.jacekpietras.zoo.core.binding.viewBinding
import com.jacekpietras.zoo.core.extensions.observe
import com.jacekpietras.zoo.map.R
import com.jacekpietras.zoo.map.databinding.FragmentMapBinding
import com.jacekpietras.zoo.map.model.MapEffect
import com.jacekpietras.zoo.map.viewmodel.MapViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class MapFragment : Fragment(R.layout.fragment_map) {

    private val viewModel by viewModel<MapViewModel>()
    private val binding: FragmentMapBinding by viewBinding(FragmentMapBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setObservers()
        setListeners()
    }

    private fun setObservers() = with(viewModel.viewState) {
        observe(userPosition) { binding.mapView.userPosition = it }
        observe(mapData) { binding.mapView.objectList = it }
        observe(effect) {
            when (it) {
                is MapEffect.ShowToast -> {
                    Toast.makeText(
                        requireContext(),
                        it.text,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun setListeners() = with(binding) {
        uploadButton.setOnClickListener { viewModel.onUploadClicked() }
        myLocationButton.setOnClickListener { viewModel.onMyLocationClicked() }
    }
}
