package com.jacekpietras.zoo.map.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jacekpietras.zoo.core.dispatcher.DefaultDispatcherProvider
import com.jacekpietras.zoo.core.dispatcher.DispatcherProvider
import com.jacekpietras.zoo.domain.interactor.*
import com.jacekpietras.zoo.map.mapper.MapViewStateMapper
import com.jacekpietras.zoo.map.model.MapEffect
import com.jacekpietras.zoo.map.model.MapState
import kotlinx.coroutines.launch

internal class MapViewModel(
    viewStateMapper: MapViewStateMapper,
    getBuildingsUseCase: GetBuildingsUseCase,
    private val uploadHistoryUseCase: UploadHistoryUseCase,
    private val getRegionsInUserPositionUseCase: GetRegionsInUserPositionUseCase,
    getTakenRouteUseCase: GetTakenRouteUseCase,
    getRoadsUseCase: GetRoadsUseCase,
    getUserPositionUseCase: GetUserPositionUseCase,
    getWorldBoundsUseCase: GetWorldBoundsUseCase,
    private val dispatcherProvider: DispatcherProvider = DefaultDispatcherProvider(),
) : ViewModel() {

    private val state = MapState(
        regionsInUserPosition = getRegionsInUserPositionUseCase(),
        worldBounds = getWorldBoundsUseCase(),
        userPosition = getUserPositionUseCase(),
        buildings = getBuildingsUseCase(),
        roads = getRoadsUseCase(),
        takenRoute = getTakenRouteUseCase(),
    )
    var viewState = viewStateMapper.from(state)

    fun onUploadClicked() {
        try {
            uploadHistoryUseCase()
        } catch (ignored: UploadHistoryUseCase.UploadFailed) {
            viewModelScope.launch(dispatcherProvider.main) {
                viewState.effect.send(MapEffect.ShowToast("Upload failed"))
            }
        }
    }

    fun onMyLocationClicked() {
        viewModelScope.launch(dispatcherProvider.main) {
            viewState.effect.send(MapEffect.CenterAtUser)
        }
    }
}