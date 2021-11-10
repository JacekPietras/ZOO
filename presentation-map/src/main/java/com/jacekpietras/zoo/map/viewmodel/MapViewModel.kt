package com.jacekpietras.zoo.map.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import com.jacekpietras.core.NullSafeMutableLiveData
import com.jacekpietras.core.PointD
import com.jacekpietras.core.combine
import com.jacekpietras.core.reduce
import com.jacekpietras.zoo.core.dispatcher.launchInBackground
import com.jacekpietras.zoo.core.dispatcher.launchInMain
import com.jacekpietras.zoo.core.dispatcher.onBackground
import com.jacekpietras.zoo.core.extensions.mapInBackground
import com.jacekpietras.zoo.core.extensions.reduceOnMain
import com.jacekpietras.zoo.core.text.Text
import com.jacekpietras.zoo.domain.interactor.*
import com.jacekpietras.zoo.domain.model.AnimalId
import com.jacekpietras.zoo.map.BuildConfig
import com.jacekpietras.zoo.map.R
import com.jacekpietras.zoo.map.mapper.MapViewStateMapper
import com.jacekpietras.zoo.map.model.*
import com.jacekpietras.zoo.map.router.MapRouter
import com.jacekpietras.zoo.tracking.GpsPermissionRequester
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

internal class MapViewModel(
    animalId: AnimalId?,
    regionId: String?,
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
    getAnimalUseCase: GetAnimalUseCase,
    private val getRegionCenterPointUseCase: GetRegionCenterPointUseCase,
    private val uploadHistoryUseCase: UploadHistoryUseCase,
    private val getShortestPathUseCase: GetShortestPathUseCase,
) : ViewModel() {

    private val state = NullSafeMutableLiveData(MapState())
    val viewState: LiveData<MapViewState> = state.map(mapper::from)

    private val volatileState = NullSafeMutableLiveData(MapVolatileState())
    val volatileViewState: LiveData<MapVolatileViewState> = volatileState.map(mapper::from)

    private val mapWorldState = NullSafeMutableLiveData(MapWorldState())
    var mapWorldViewState: LiveData<MapWorldViewState> = mapWorldState.mapInBackground(mapper::from)

    private val _effect = Channel<MapEffect>()
    val effect: Flow<MapEffect> = _effect.receiveAsFlow()

    init {
        launchInBackground {
            launch { loadAnimalsUseCase.run() }

            observeCompassUseCase.run()
                .onEach { volatileState.reduceOnMain { copy(compass = it) } }
                .launchIn(this)
            getUserPositionUseCase.run()
                .onEach { volatileState.reduceOnMain { copy(userPosition = it) } }
                .launchIn(this)
            getRegionsInUserPositionUseCase.run()
                .onEach { state.reduceOnMain { copy(regionsInUserPosition = it) } }
                .launchIn(this)
            getAnimalsInUserPositionUseCase.run()
                .onEach { state.reduceOnMain { copy(animalsInUserPosition = it) } }
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
                mapWorldState.reduceOnMain {
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

            if (animalId != null) {
                val animal = getAnimalUseCase.run(animalId)
                state.reduceOnMain { copy(selectedAnimal = animal) }
                val point = getRegionCenterPointUseCase.run(regionId ?: animal.regionInZoo)
                onPointPlaced(point)
                _effect.send(MapEffect.CenterAtPoint(point))
            }
        }
    }

    fun onUploadClicked() {
        try {
            uploadHistoryUseCase.run()
        } catch (ignored: UploadHistoryUseCase.UploadFailed) {
            launchInMain {
                _effect.send(MapEffect.ShowToast(Text("Upload failed")))
            }
        }
    }

    fun onPointPlaced(point: PointD) {
        launchInMain {
            volatileState.reduce { copy(snappedPoint = point) }
            onBackground {
                val shortestPath = getShortestPathUseCase.run(point)
                volatileState.reduceOnMain { copy(shortestPath = shortestPath) }
            }
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
        launchInMain {
            _effect.send(MapEffect.CenterAtUser)
        }
    }

    private fun onLocationDenied() {
        if (BuildConfig.DEBUG) {
            launchInMain {
                _effect.send(MapEffect.ShowToast(Text(R.string.location_denied)))
            }
        }
    }

    fun onCameraButtonClicked(router: MapRouter) {
        router.navigateToCamera()
    }

    fun onRegionClicked(region: Text) {

    }
}
