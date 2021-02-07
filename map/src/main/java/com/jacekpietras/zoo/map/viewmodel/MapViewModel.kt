package com.jacekpietras.zoo.map.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jacekpietras.zoo.core.dispatcher.DefaultDispatcherProvider
import com.jacekpietras.zoo.core.dispatcher.DispatcherProvider
import com.jacekpietras.zoo.domain.interactor.GetBuildingsUseCase
import com.jacekpietras.zoo.domain.interactor.GetRoadsUseCase
import com.jacekpietras.zoo.domain.interactor.GetUserPosition
import com.jacekpietras.zoo.domain.interactor.UploadHistoryUseCase
import com.jacekpietras.zoo.map.mapper.MapViewStateMapper
import com.jacekpietras.zoo.map.model.MapEffect
import com.jacekpietras.zoo.map.model.MapState
import kotlinx.coroutines.launch

internal class MapViewModel(
    viewStateMapper: MapViewStateMapper,
    getBuildingsUseCase: GetBuildingsUseCase,
    private val uploadHistoryUseCase: UploadHistoryUseCase,
    getRoadsUseCase: GetRoadsUseCase,
    getUserPosition: GetUserPosition,
    private val dispatcherProvider: DispatcherProvider = DefaultDispatcherProvider(),
) : ViewModel() {

    private val state = MapState(
        userPosition = getUserPosition(),
        buildings = getBuildingsUseCase(),
        roads = getRoadsUseCase()
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
        //TODO("Not yet implemented")
    }
}