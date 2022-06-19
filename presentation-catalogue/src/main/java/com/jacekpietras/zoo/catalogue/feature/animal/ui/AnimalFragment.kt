package com.jacekpietras.zoo.catalogue.feature.animal.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.accompanist.pager.ExperimentalPagerApi
import com.jacekpietras.mapview.model.ComposablePaint
import com.jacekpietras.mapview.ui.ComposablePaintBaker
import com.jacekpietras.mapview.ui.MapViewLogic
import com.jacekpietras.zoo.catalogue.feature.animal.model.AnimalViewState
import com.jacekpietras.zoo.catalogue.feature.animal.router.AnimalRouterImpl
import com.jacekpietras.zoo.catalogue.feature.animal.viewmodel.AnimalViewModel
import com.jacekpietras.zoo.core.theme.ZooTheme
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

@ExperimentalPagerApi
class AnimalFragment : Fragment() {

    private val args: AnimalFragmentArgs by navArgs()
    private val viewModel by viewModel<AnimalViewModel> {
        parametersOf(args.animalId)
    }
    private val router by lazy { AnimalRouterImpl(::requireActivity, findNavController()) }

    private val paintBaker by lazy { ComposablePaintBaker(requireActivity()) }
    private val mapLogic = MapViewLogic(
        invalidate = { mapList.value = it },
        bakeCanvasPaint = { paintBaker.bakeCanvasPaint(it) },
        bakeBorderCanvasPaint = { paintBaker.bakeBorderCanvasPaint(it) },
        bakeDimension = { paintBaker.bakeDimension(it) },
    )
    private val mapList = MutableLiveData<List<MapViewLogic.RenderItem<ComposablePaint>>>()

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View = ComposeView(requireContext()).apply {
        setContent {
            val viewState by viewModel.viewState.observeAsState()
            viewState?.updateMap()


            ZooTheme {
                AnimalFragmentView(
                    viewState = viewState,
                    mapList = mapList.observeAsState().value,
                    onWebClicked = { viewModel.onWebClicked(router) },
                    onWikiClicked = { viewModel.onWikiClicked(router) },
                    onNavClicked = { viewModel.onNavClicked(router) },
                    onFavoriteClicked = { viewModel.onFavoriteClicked() },
                    onMapSizeChanged = mapLogic::onSizeChanged,
                )
            }
        }
    }

    private fun AnimalViewState.updateMap() {
        mapLogic.worldData = MapViewLogic.WorldData(
            bounds = worldBounds,
            objectList = mapData,
        )
        mapLogic.setRotate(-23f)
        mapLogic.onScale(0f, 0f, Float.MAX_VALUE)
    }
}
