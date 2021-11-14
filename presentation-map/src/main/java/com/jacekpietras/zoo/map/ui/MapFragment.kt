package com.jacekpietras.zoo.map.ui

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.jacekpietras.mapview.ui.MapViewLogic
import com.jacekpietras.zoo.core.binding.viewBinding
import com.jacekpietras.zoo.core.extensions.observe
import com.jacekpietras.zoo.map.R
import com.jacekpietras.zoo.map.databinding.FragmentMapBinding
import com.jacekpietras.zoo.map.model.MapEffect
import com.jacekpietras.zoo.map.viewmodel.MapViewModel
import com.jacekpietras.zoo.tracking.GpsPermissionRequester
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class MapFragment : Fragment(R.layout.fragment_map) {

    private val args: MapFragmentArgs? by navArgs()
    private val viewModel by viewModel<MapViewModel> {
        parametersOf(args?.animalId, args?.regionId)
    }
    private val binding: FragmentMapBinding by viewBinding(FragmentMapBinding::bind)
    private val permissionChecker = GpsPermissionRequester(fragment = this)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setObservers()
        setListeners()
    }

    private fun setObservers() = with(viewModel) {
        mapWorldViewState.observe(viewLifecycleOwner) {
            with(it) {
                binding.mapView.logic.worldData = MapViewLogic.WorldData(
                    bounds = worldBounds,
                    objectList = mapData,
                )
            }
        }
        volatileViewState.observe(viewLifecycleOwner) {
            with(it) {
                binding.mapView.logic.userData = MapViewLogic.UserData(
                    userPosition = userPosition,
                    compass = compass,
                    objectList = mapData,
                )
            }
        }
        viewState.observe(viewLifecycleOwner) {
            with(it) {
                binding.topCardTitle.text = title.toCharSeq(requireContext())
                binding.topCardContent.text = content.toCharSeq(requireContext())
            }
        }

        effect.observe(viewLifecycleOwner) {
            when (it) {
                is MapEffect.ShowToast -> toast(it.text.toString(requireContext()))
                is MapEffect.CenterAtUser -> binding.mapView.centerAtUserPosition()
                is MapEffect.CenterAtPoint -> binding.mapView.centerAtPoint(it.point)
            }
        }
    }

    override fun onStop() {
        viewModel.onStopEvent()
        super.onStop()
    }

    private fun setListeners() = with(binding) {
        uploadButton.setOnClickListener { viewModel.onUploadClicked() }
        myLocationButton.setOnClickListener { viewModel.onLocationButtonClicked(permissionChecker) }
        mapView.logic.setOnPointPlacedListener = { point -> viewModel.onPointPlaced(point) }
    }

    private fun toast(text: String) {
        Toast.makeText(requireContext(), text, Toast.LENGTH_SHORT).show()
    }
}
