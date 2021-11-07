package com.jacekpietras.zoo.map.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.google.android.material.composethemeadapter.MdcTheme
import com.jacekpietras.zoo.map.viewmodel.MapViewModel
import com.jacekpietras.zoo.tracking.GpsPermissionRequester
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class ComposableMapFragment : Fragment() {

    private val args: MapFragmentArgs? by navArgs()
    private val viewModel by viewModel<MapViewModel> {
        parametersOf(args?.animalId, args?.regionId)
    }
    private val permissionChecker = GpsPermissionRequester(fragment = this)

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View = ComposeView(requireContext()).apply {
        setContent {
            MdcTheme {
                LinearTransactionsChart()
            }
        }
    }

    @Composable
    fun LinearTransactionsChart(
        modifier: Modifier = Modifier,
    ) {
        Canvas(modifier = modifier) {
            drawLine(
                start = Offset(
                    x = 0f,
                    y = 0f,
                ),
                end = Offset(
                    x = size.width,
                    y = size.height,
                ),
                color = Color(40, 193, 218),
                strokeWidth = Stroke.DefaultMiter
            )
        }
    }

//    private fun setObservers() = with(viewModel) {
//        mapViewState.observe(viewLifecycleOwner) {
//            Timber.e("dupa observe whole map")
//            with(it) {
//                binding.mapView.worldBounds = worldBounds
//                binding.mapView.objectList = mapData
//                binding.mapView.terminalPoints = terminalPoints
//            }
//        }
//        volatileViewState.observe(viewLifecycleOwner) {
//            Timber.e("dupa observe volatile map")
//            with(it) {
//                binding.mapView.userPosition = userPosition
//                binding.mapView.compass = compass
//                binding.topCardTitle.text = title.toCharSeq(requireContext())
//                binding.topCardContent.text = content.toCharSeq(requireContext())
//                binding.mapView.clickOnWorld = snappedPoint
//                binding.mapView.shortestPath = shortestPath
//            }
//        }
//
//        effect.observe(viewLifecycleOwner) {
//            when (it) {
//                is MapEffect.ShowToast -> toast(it.text.toString(requireContext()))
//                is MapEffect.CenterAtUser -> binding.mapView.centerAtUserPosition()
//                is MapEffect.CenterAtPoint -> binding.mapView.centerAtPoint(it.point)
//            }
//        }
//    }
//
//    private fun setListeners() = with(binding) {
//        uploadButton.setOnClickListener { viewModel.onUploadClicked() }
//        myLocationButton.setOnClickListener { viewModel.onLocationButtonClicked(permissionChecker) }
//        mapView.setOnPointPlacedListener = { point -> viewModel.onPointPlaced(point) }
//    }
//
//    private fun toast(text: String) {
//        Toast.makeText(requireContext(), text, Toast.LENGTH_SHORT).show()
//    }
}
