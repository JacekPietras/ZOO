package com.jacekpietras.zoo.map.ui

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.jacekpietras.core.observe
import com.jacekpietras.zoo.core.binding.viewBinding
import com.jacekpietras.zoo.map.R
import com.jacekpietras.zoo.map.databinding.FragmentMapBinding
import com.jacekpietras.zoo.map.model.MapEffect
import com.jacekpietras.zoo.map.viewmodel.MapViewModel
import com.jacekpietras.zoo.tracking.GpsPermissionRequester
import kotlinx.coroutines.flow.Flow
import org.koin.androidx.viewmodel.ext.android.viewModel

class MapFragment : Fragment(R.layout.fragment_map) {

    private val viewModel by viewModel<MapViewModel>()
    private val binding: FragmentMapBinding by viewBinding(FragmentMapBinding::bind)
    private val permissionChecker = GpsPermissionRequester(fragment = this)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setObservers()
        setListeners()
    }

    private fun setObservers() = with(viewModel.viewState) {
        currentRegionIds.observe { binding.regionIds.text = it }
        userPosition.observe { binding.mapView.userPosition = it }
        terminalPoints.observe { binding.mapView.terminalPoints = it }
        mapData.observe { binding.mapView.objectList = it }
        worldBounds.observe { binding.mapView.worldBounds = it }
        compass.observe { binding.mapView.compass = it }
        snappedPoint.observe { binding.mapView.clickOnWorld = it }
        shortestPath.observe { binding.mapView.shortestPath = it }

        effect.observe {
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

    private inline fun <T> Flow<T>.observe(crossinline block: (T) -> Unit) {
        observe(viewLifecycleOwner, block)
    }
}
