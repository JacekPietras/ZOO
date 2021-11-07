package com.jacekpietras.zoo.map.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.google.android.material.composethemeadapter.MdcTheme
import com.jacekpietras.mapview.ui.ComposableMapView
import com.jacekpietras.mapview.ui.ComposablePaintBaker
import com.jacekpietras.mapview.ui.MapViewLogic
import com.jacekpietras.zoo.map.viewmodel.MapViewModel
import com.jacekpietras.zoo.tracking.GpsPermissionRequester
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import timber.log.Timber

class ComposableMapFragment : Fragment() {

    private val args: ComposableMapFragmentArgs? by navArgs()
    private val viewModel by viewModel<MapViewModel> {
        parametersOf(args?.animalId, args?.regionId)
    }
    private val paintBaker by lazy { ComposablePaintBaker(requireActivity()) }
    private val permissionChecker = GpsPermissionRequester(fragment = this)
    private val mapLogic = MapViewLogic(
        doAnimation = { it(1f, 0f) },
        invalidate = { },
        bakeCanvasPaint = { paintBaker.bakeCanvasPaint(it) },
        bakeBorderCanvasPaint = { paintBaker.bakeBorderCanvasPaint(it) },
        setOnPointPlacedListener = { point -> viewModel.onPointPlaced(point) },
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setObservers()
    }

    private fun setObservers() = with(viewModel) {
        mapViewState.observe(viewLifecycleOwner) {
            Timber.e("dupa observe whole map")
            with(it) {
                mapLogic.worldBounds = worldBounds
                mapLogic.objectList = mapData
                mapLogic.terminalPoints = terminalPoints
            }
        }
        volatileViewState.observe(viewLifecycleOwner) {
            Timber.e("dupa observe volatile map")
            with(it) {
                mapLogic.userPosition = userPosition
                mapLogic.compass = compass
                mapLogic.clickOnWorld = snappedPoint
                mapLogic.shortestPath = shortestPath
            }
        }

//        effect.observe(viewLifecycleOwner) {
//            when (it) {
//                is MapEffect.ShowToast -> toast(it.text.toString(requireContext()))
//                is MapEffect.CenterAtUser -> mapData.centerAtUserPosition()
//                is MapEffect.CenterAtPoint -> mapData.centerAtPoint(it.point)
//            }
//        }
    }

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View = ComposeView(requireContext()).apply {
        setContent {
            MdcTheme {
                ComposableMapView(
                    mapData = mapLogic,
                )
            }
        }
    }
}
