package com.jacekpietras.zoo.map.viewmodel

import androidx.lifecycle.ViewModel
import com.jacekpietras.zoo.domain.interactor.GetBuildingsUseCase
import com.jacekpietras.zoo.domain.interactor.GetRoadsUseCase
import com.jacekpietras.zoo.domain.interactor.GetUserPosition
import com.jacekpietras.zoo.map.mapper.MapViewStateMapper
import com.jacekpietras.zoo.map.model.MapState

internal class MapViewModel(
    viewStateMapper: MapViewStateMapper,
    getBuildingsUseCase: GetBuildingsUseCase,
    getRoadsUseCase: GetRoadsUseCase,
    getUserPosition: GetUserPosition,
//    dispatcherProvider: DispatcherProvider = DefaultDispatcherProvider(),
) : ViewModel() {

    private val state = MapState(
        userPosition = getUserPosition(),
        buildings = getBuildingsUseCase(),
        roads = getRoadsUseCase()
    )
    var viewState = viewStateMapper.from(state)
}