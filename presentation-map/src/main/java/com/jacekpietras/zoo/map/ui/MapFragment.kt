package com.jacekpietras.zoo.map.ui

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.jacekpietras.zoo.core.binding.viewBinding
import com.jacekpietras.zoo.core.extensions.observe
import com.jacekpietras.zoo.map.R
import com.jacekpietras.zoo.map.databinding.FragmentMapBinding
import com.jacekpietras.zoo.map.model.MapEffect
import com.jacekpietras.zoo.map.viewmodel.MapViewModel
import com.jacekpietras.zoo.tracking.GpsPermissionRequester
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import timber.log.Timber

class MapFragment : Fragment(R.layout.fragment_map) {

    private val args: MapFragmentArgs? by navArgs()
    private val viewModel by viewModel<MapViewModel> {
        parametersOf(args?.animalId)
    }
    private val binding: FragmentMapBinding by viewBinding(FragmentMapBinding::bind)
    private val permissionChecker = GpsPermissionRequester(fragment = this)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setObservers()
        setListeners()
    }

    private fun setObservers() = with(viewModel) {
        mapViewState.observe(viewLifecycleOwner) {
            Timber.e("dupa observe whole map")
            with(it) {
                binding.mapView.worldBounds = worldBounds
                binding.mapView.objectList = mapData
                binding.mapView.terminalPoints = terminalPoints
            }
        }
        volatileViewState.observe(viewLifecycleOwner) {
            Timber.e("dupa observe volatile map")
            with(it) {
                binding.mapView.userPosition = userPosition
                binding.mapView.compass = compass
                binding.regionIds.text = currentRegionIds
                binding.animals.text = currentAnimals
                binding.mapView.clickOnWorld = snappedPoint
                binding.mapView.shortestPath = shortestPath
            }
        }

        effect.observe(viewLifecycleOwner) {
            when (it) {
                is MapEffect.ShowToast -> toast(it.text)
                is MapEffect.CenterAtUser -> binding.mapView.centerAtUserPosition()
            }
        }
    }

    private fun setListeners() = with(binding) {
        uploadButton.setOnClickListener { viewModel.onUploadClicked() }
        myLocationButton.setOnClickListener { viewModel.onLocationButtonClicked(permissionChecker) }
        mapView.setOnPointPlacedListener = { point -> viewModel.onPointPlaced(point) }
    }

    private fun toast(text: String) {
        Toast.makeText(requireContext(), text, Toast.LENGTH_SHORT).show()
    }
}
