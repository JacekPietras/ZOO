package com.jacekpietras.zoo.map.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jacekpietras.zoo.core.dispatcher.DefaultDispatcherProvider
import com.jacekpietras.zoo.core.dispatcher.DispatcherProvider
import com.jacekpietras.zoo.domain.interactor.GetBuildingsUseCase
import com.jacekpietras.zoo.domain.interactor.GetRoadsUseCase
import com.jacekpietras.zoo.domain.interactor.GetUserPosition
import com.jacekpietras.zoo.map.mapper.MapViewStateMapper
import com.jacekpietras.zoo.map.model.MapState
import com.jacekpietras.zoo.map.model.MapViewState
import kotlinx.coroutines.launch

internal class MapViewModel(
    viewStateMapper: MapViewStateMapper,
    getBuildingsUseCase: GetBuildingsUseCase,
    getRoadsUseCase: GetRoadsUseCase,
    getUserPosition: GetUserPosition,
    dispatcherProvider: DispatcherProvider = DefaultDispatcherProvider(),
) : ViewModel() {

    private val state = MapState(
        userPosition = getUserPosition(),
        buildings = getBuildingsUseCase(),
        roads = getRoadsUseCase()
    )

    val viewState: MapViewState = MapViewState()

    init {
        viewModelScope.launch(dispatcherProvider.main) {
            viewStateMapper.from(state, viewState)
        }
    }
}