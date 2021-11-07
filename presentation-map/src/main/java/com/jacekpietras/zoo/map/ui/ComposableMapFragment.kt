package com.jacekpietras.zoo.map.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.google.android.material.composethemeadapter.MdcTheme
import com.jacekpietras.mapview.ui.ComposableMapView
import com.jacekpietras.zoo.map.model.MapViewState
import com.jacekpietras.zoo.map.model.MapVolatileViewState
import com.jacekpietras.zoo.map.viewmodel.MapViewModel
import com.jacekpietras.zoo.tracking.GpsPermissionRequester
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class ComposableMapFragment : Fragment() {

    private val args: ComposableMapFragmentArgs? by navArgs()
    private val viewModel by viewModel<MapViewModel> {
        parametersOf(args?.animalId, args?.regionId)
    }
    private val permissionChecker = GpsPermissionRequester(fragment = this)

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View = ComposeView(requireContext()).apply {
        setContent {
            val mapViewState by viewModel.mapViewState.observeAsState(MapViewState())
            val volatileViewState by viewModel.volatileViewState.observeAsState(MapVolatileViewState())

            with(mapViewState) {
                with(volatileViewState) {
                    MdcTheme {
                        ComposableMapView(
                            worldBounds = worldBounds,
                            objectList = mapData,
                            terminalPoints = terminalPoints,

                            userPosition = userPosition,
                            shortestPath = shortestPath,
                            clickOnWorld = snappedPoint,
                            compass = compass,
                            setOnPointPlacedListener = { point -> viewModel.onPointPlaced(point) },
                        )
                    }
                }
            }
        }
    }
}
