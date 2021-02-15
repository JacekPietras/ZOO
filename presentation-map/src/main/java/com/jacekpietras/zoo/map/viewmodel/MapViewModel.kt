package com.jacekpietras.zoo.map.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jacekpietras.zoo.core.dispatcher.DefaultDispatcherProvider
import com.jacekpietras.zoo.core.dispatcher.DispatcherProvider
import com.jacekpietras.zoo.domain.interactor.*
import com.jacekpietras.zoo.map.BuildConfig
import com.jacekpietras.zoo.map.mapper.MapViewStateMapper
import com.jacekpietras.zoo.map.model.MapEffect
import com.jacekpietras.zoo.map.model.MapState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch

internal class MapViewModel(
    viewStateMapper: MapViewStateMapper,
    private val uploadHistoryUseCase: UploadHistoryUseCase,
    observeTakenRouteUseCase: ObserveTakenRouteUseCase,
    getRegionsInUserPositionUseCase: GetRegionsInUserPositionUseCase,
    getUserPositionUseCase: GetUserPositionUseCase,
    observeWorldBoundsUseCase: ObserveWorldBoundsUseCase,
    getBuildingsUseCase: GetBuildingsUseCase,
    getAviaryUseCase: GetAviaryUseCase,
    getRoadsUseCase: GetRoadsUseCase,
    getTechnicalRoadsUseCase: GetTechnicalRoadsUseCase,
    getLinesUseCase: GetLinesUseCase,
    private val dispatcherProvider: DispatcherProvider = DefaultDispatcherProvider(),
) : ViewModel() {

    private val state = MapState(
        regionsInUserPosition = getRegionsInUserPositionUseCase(),
        worldBounds = observeWorldBoundsUseCase(),
        userPosition = getUserPositionUseCase(),
        buildings = getBuildingsUseCase(),
        aviary = getAviaryUseCase(),
        roads = getRoadsUseCase(),
        technicalRoute = getTechnicalRoadsUseCase(),
        lines = getLinesUseCase(),
        takenRoute = observeTakenRouteUseCase(),
    )
    private val effect: Channel<MapEffect> = Channel()
    var viewState = viewStateMapper.from(state, effect)

    fun onUploadClicked() {
        try {
            uploadHistoryUseCase()
        } catch (ignored: UploadHistoryUseCase.UploadFailed) {
            viewModelScope.launch(dispatcherProvider.main) {
                effect.send(MapEffect.ShowToast("Upload failed"))
            }
        }
    }

    fun onMyLocationClicked() {
        viewModelScope.launch(dispatcherProvider.main) {
            effect.send(MapEffect.CenterAtUser)
        }
    }

    fun onLocationDenied() {
        if (BuildConfig.DEBUG) {
            viewModelScope.launch(dispatcherProvider.main) {
                effect.send(MapEffect.ShowToast("Location denied"))
            }
        }
    }
}