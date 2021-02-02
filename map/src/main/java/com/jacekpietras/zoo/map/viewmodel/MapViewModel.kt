package com.jacekpietras.zoo.map.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jacekpietras.zoo.core.dispatcher.DefaultDispatcherProvider
import com.jacekpietras.zoo.core.dispatcher.DispatcherProvider
import com.jacekpietras.zoo.core.extensions.catchAndLog
import com.jacekpietras.zoo.core.extensions.reduce
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
) : ViewModel() {

    private val state: MutableLiveData<MapState> = MutableLiveData(MapState())
    val viewState: MapViewState = MapViewState()

    init {
        state.observeForever {
            viewStateMapper.from(it, viewState)
        }

        viewModelScope.launch(dispatcherProvider.main) {
            getMapDataUseCase()
                .onEach { data ->
                    state.reduce {
                        copy(
                            buildings = data.filterIsInstance(PolygonEntity::class.java),
                            roads = data.filterIsInstance(PathEntity::class.java),
                        )
                    }
                }
                .catchAndLog()
                .launchIn(this)

            getUserPosition()
                .onEach { state.reduce { copy(userPosition = it) } }
                .catchAndLog()
                .launchIn(this)
        }
    }
}