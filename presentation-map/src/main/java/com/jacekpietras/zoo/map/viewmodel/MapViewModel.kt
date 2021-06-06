package com.jacekpietras.zoo.map.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.jacekpietras.core.NullSafeMutableLiveData
import com.jacekpietras.core.PointD
import com.jacekpietras.core.combine
import com.jacekpietras.core.reduce
import com.jacekpietras.zoo.core.dispatcher.DefaultDispatcherProvider
import com.jacekpietras.zoo.core.dispatcher.DispatcherProvider
import com.jacekpietras.zoo.domain.interactor.*
import com.jacekpietras.zoo.domain.model.AnimalId
import com.jacekpietras.zoo.map.BuildConfig
import com.jacekpietras.zoo.map.R
import com.jacekpietras.zoo.map.mapper.MapViewStateMapper
import com.jacekpietras.zoo.map.model.*
import com.jacekpietras.zoo.tracking.GpsPermissionRequester
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal class MapViewModel(
    animalId: AnimalId?,
    mapper: MapViewStateMapper,
    observeCompassUseCase: ObserveCompassUseCase,
    observeTakenRouteUseCase: ObserveTakenRouteUseCase,
    getRegionsInUserPositionUseCase: GetRegionsInUserPositionUseCase,
    getAnimalsInUserPositionUseCase: GetAnimalsInUserPositionUseCase,
    getUserPositionUseCase: GetUserPositionUseCase,
    observeWorldBoundsUseCase: ObserveWorldBoundsUseCase,
    getBuildingsUseCase: GetBuildingsUseCase,
    getAviaryUseCase: GetAviaryUseCase,
    getRoadsUseCase: GetRoadsUseCase,
    getTechnicalRoadsUseCase: GetTechnicalRoadsUseCase,
    getTerminalNodesUseCase: GetTerminalNodesUseCase,
    getLinesUseCase: GetLinesUseCase,
    loadAnimalsUseCase: LoadAnimalsUseCase,
    private val uploadHistoryUseCase: UploadHistoryUseCase,
    private val getRegionsContainingPointUseCase: GetRegionsContainingPointUseCase,
    private val getShortestPathUseCase: GetShortestPathUseCase,
    private val dispatcherProvider: DispatcherProvider = DefaultDispatcherProvider(),
) : ViewModel() {

    private val volatileState = NullSafeMutableLiveData(MapVolatileState())
    val volatileViewState: LiveData<MapVolatileViewState> = volatileState.map(mapper::from)

    private val mapState = NullSafeMutableLiveData(MapState())
    var mapViewState: LiveData<MapViewState> = mapState.map(mapper::from)

    private val _effect = Channel<MapEffect>()
    val effect: Flow<MapEffect> = _effect.receiveAsFlow()

    init {
        viewModelScope.launch(dispatcherProvider.main) {
            loadAnimalsUseCase.run()

            observeCompassUseCase.run()
                .onEach { volatileState.reduce { copy(compass = it) } }
                .launchIn(this)
            getUserPositionUseCase.run()
                .onEach { volatileState.reduce { copy(userPosition = it) } }
                .launchIn(this)
            getRegionsInUserPositionUseCase.run()
                .onEach { volatileState.reduce { copy(regionsInUserPosition = it) } }
                .launchIn(this)
            getAnimalsInUserPositionUseCase.run()
                .onEach { volatileState.reduce { copy(animalsInUserPosition = it) } }
                .launchIn(this)

            combine(
                observeWorldBoundsUseCase.run(),
                getBuildingsUseCase.run(),
                getAviaryUseCase.run(),
                getRoadsUseCase.run(),
                getLinesUseCase.run(),
                observeTakenRouteUseCase.run(),
                getTechnicalRoadsUseCase.run(),
                getTerminalNodesUseCase.run(),
            ) { worldBounds, buildings, aviary, roads, lines, takenRoute, technicalRoute, terminalPoints ->
                mapState.reduce {
                    copy(
                        worldBounds = worldBounds,
                        buildings = buildings,
                        aviary = aviary,
                        roads = roads,
                        lines = lines,
                        takenRoute = takenRoute,
                        technicalRoute = technicalRoute,
                        terminalPoints = terminalPoints,
                    )
                }
            }.launchIn(this)
        }
    }

    fun onUploadClicked() {
        try {
            uploadHistoryUseCase.run()
        } catch (ignored: UploadHistoryUseCase.UploadFailed) {
            viewModelScope.launch(dispatcherProvider.main) {
                _effect.send(MapEffect.ShowToast("Upload failed"))
            }
        }
    }

    fun onPointPlaced(point: PointD) {
        viewModelScope.launch(dispatcherProvider.main) {
            val regions = withContext(dispatcherProvider.default) {
                getRegionsContainingPointUseCase.run(point)
            }

            if (regions.isNotEmpty())
                _effect.send(MapEffect.ShowToast(regions.toString()))

            volatileState.reduce { copy(snappedPoint = point) }
            volatileState.reduce { copy(shortestPath = getShortestPathUseCase.run(point)) }
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
            _effect.send(MapEffect.CenterAtUser)
        }
    }

    private fun onLocationDenied() {
        if (BuildConfig.DEBUG) {
            viewModelScope.launch(dispatcherProvider.main) {
                _effect.send(MapEffect.ShowToast("Location denied"))
            }
        }
    }
}
