@file:Suppress("EXPERIMENTAL_API_USAGE")

package com.jacekpietras.zoo.map.viewmodel

import androidx.lifecycle.viewModelScope
import com.jacekpietras.zoo.core.dispatcher.DefaultDispatcherProvider
import com.jacekpietras.zoo.core.dispatcher.DispatcherProvider
import com.jacekpietras.zoo.core.extensions.catchAndLog
import com.jacekpietras.zoo.core.viewmodel.BaseViewModel
import com.jacekpietras.zoo.domain.interactor.GetMapDataUseCase
import com.jacekpietras.zoo.domain.interactor.GetUserPosition
import com.jacekpietras.zoo.domain.model.MapItemEntity.PathEntity
import com.jacekpietras.zoo.domain.model.MapItemEntity.PolygonEntity
import com.jacekpietras.zoo.map.mapper.MapViewStateMapper
import com.jacekpietras.zoo.map.model.MapState
import com.jacekpietras.zoo.map.model.MapViewState
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

internal class MapViewModel(
    viewStateMapper: MapViewStateMapper,
    getMapDataUseCase: GetMapDataUseCase,
    getUserPosition: GetUserPosition,
    dispatcherProvider: DispatcherProvider = DefaultDispatcherProvider(),
) : BaseViewModel<MapState, MapViewState>(
    initialState = MapState(),
    mapper = viewStateMapper::from
) {

    init {
        viewModelScope.launch(dispatcherProvider.main) {
            getMapDataUseCase()
                .onEach { data ->
                    updateState {
                        copy(
                            buildings = data.filterIsInstance(PolygonEntity::class.java),
                            roads = data.filterIsInstance(PathEntity::class.java),
                        )
                    }
                }
                .catchAndLog()
                .launchIn(this)

            getUserPosition()
                .onEach { updateState { copy(userPosition = it) } }
                .catchAndLog()
                .launchIn(this)
        }
    }
}