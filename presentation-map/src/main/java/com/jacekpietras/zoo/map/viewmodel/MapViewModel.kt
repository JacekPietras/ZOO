package com.jacekpietras.zoo.map.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jacekpietras.core.PointD
import com.jacekpietras.zoo.core.dispatcher.DefaultDispatcherProvider
import com.jacekpietras.zoo.core.dispatcher.DispatcherProvider
import com.jacekpietras.zoo.domain.interactor.*
import com.jacekpietras.zoo.map.BuildConfig
import com.jacekpietras.zoo.map.R
import com.jacekpietras.zoo.map.mapper.MapViewStateMapper
import com.jacekpietras.zoo.map.model.MapEffect
import com.jacekpietras.zoo.map.model.MapState
import com.jacekpietras.zoo.tracking.GpsPermissionRequester
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch

internal class MapViewModel(
    viewStateMapper: MapViewStateMapper,
    private val uploadHistoryUseCase: UploadHistoryUseCase,
    observeCompassUseCase: ObserveCompassUseCase,
    observeTakenRouteUseCase: ObserveTakenRouteUseCase,
    getRegionsInUserPositionUseCase: GetRegionsInUserPositionUseCase,
    getAnimalsInUserPositionUseCase: GetAnimalsInUserPositionUseCase,
    private val getRegionsContainingPointUseCase: GetRegionsContainingPointUseCase,
    getUserPositionUseCase: GetUserPositionUseCase,
    observeWorldBoundsUseCase: ObserveWorldBoundsUseCase,
    getBuildingsUseCase: GetBuildingsUseCase,
    getAviaryUseCase: GetAviaryUseCase,
    getRoadsUseCase: GetRoadsUseCase,
    getTechnicalRoadsUseCase: GetTechnicalRoadsUseCase,
    getTerminalNodesUseCase: GetTerminalNodesUseCase,
    getLinesUseCase: GetLinesUseCase,
    loadAnimalsUseCase: LoadAnimalsUseCase,
    private val getShortestPathUseCase: GetShortestPathUseCase,
    private val dispatcherProvider: DispatcherProvider = DefaultDispatcherProvider(),
) : ViewModel() {

    private val state = MapState(
        regionsInUserPosition = getRegionsInUserPositionUseCase(),
        animalsInUserPosition = getAnimalsInUserPositionUseCase(),
        worldBounds = observeWorldBoundsUseCase(),
        userPosition = getUserPositionUseCase(),
        buildings = getBuildingsUseCase(),
        aviary = getAviaryUseCase(),
        roads = getRoadsUseCase(),
        technicalRoute = getTechnicalRoadsUseCase(),
        terminalPoints = getTerminalNodesUseCase(),
        lines = getLinesUseCase(),
        takenRoute = observeTakenRouteUseCase(),
        compass = observeCompassUseCase(),
    )
    private val effect: Channel<MapEffect> = Channel()
    var viewState = viewStateMapper.from(state, effect)

    init {
        viewModelScope.launch(dispatcherProvider.default) {
            loadAnimalsUseCase()
        }
    }

    fun onUploadClicked() {
        try {
            uploadHistoryUseCase()
        } catch (ignored: UploadHistoryUseCase.UploadFailed) {
            viewModelScope.launch(dispatcherProvider.main) {
                effect.send(MapEffect.ShowToast("Upload failed"))
            }
        }
    }

    fun onPointPlaced(point: PointD) {
        viewModelScope.launch(dispatcherProvider.default) {
            val regions = getRegionsContainingPointUseCase(point)

            if (regions.isNotEmpty())
                effect.send(MapEffect.ShowToast(regions.toString()))

            state.snappedPoint.emit(point)

            val shortestPath = getShortestPathUseCase(point)
            state.shortestPath.emit(shortestPath)
        }
    }

    fun onLocationButtonClicked(permissionChecker: GpsPermissionRequester) {
        permissionChecker.checkPermissions(
            rationaleTitle = R.string.gps_permission_rationale_title,
            rationaleContent = R.string.gps_permission_rationale_content,
            deniedTitle = R.string.gps_permission_denied_title,
            deniedContent = R.string.gps_permission_denied_content,
            onFailed = { onLocationDenied() },
            onPermission = { onMyLocationClicked() },
        )
    }

    private fun onMyLocationClicked() {
        viewModelScope.launch(dispatcherProvider.main) {
            effect.send(MapEffect.CenterAtUser)
        }
    }

    private fun onLocationDenied() {
        if (BuildConfig.DEBUG) {
            viewModelScope.launch(dispatcherProvider.main) {
                effect.send(MapEffect.ShowToast("Location denied"))
            }
        }
    }
}