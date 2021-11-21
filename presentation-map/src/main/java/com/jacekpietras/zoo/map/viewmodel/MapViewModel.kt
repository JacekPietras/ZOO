package com.jacekpietras.zoo.map.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import com.jacekpietras.core.*
import com.jacekpietras.zoo.core.dispatcher.launchInBackground
import com.jacekpietras.zoo.core.dispatcher.launchInMain
import com.jacekpietras.zoo.core.dispatcher.onMain
import com.jacekpietras.zoo.core.extensions.mapInBackground
import com.jacekpietras.zoo.core.extensions.reduceOnMain
import com.jacekpietras.zoo.core.text.Text
import com.jacekpietras.zoo.domain.interactor.*
import com.jacekpietras.zoo.domain.model.AnimalEntity
import com.jacekpietras.zoo.domain.model.AnimalId
import com.jacekpietras.zoo.domain.model.Region
import com.jacekpietras.zoo.domain.model.RegionId
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

internal class MapViewModel(
    animalId: AnimalId?,
    regionId: RegionId?,
    mapper: MapViewStateMapper,
    observeCompassUseCase: ObserveCompassUseCase,
    observeTakenRouteUseCase: ObserveTakenRouteUseCase,
    observeOldTakenRouteUseCase: ObserveOldTakenRouteUseCase,
    private val getAnimalsInRegionUseCase: GetAnimalsInRegionUseCase,
    private val getRegionsContainingPointUseCase: GetRegionsContainingPointUseCase,
    getRegionsWithAnimalsInUserPositionUseCase: GetRegionsWithAnimalsInUserPositionUseCase,
    getUserPositionUseCase: GetUserPositionUseCase,
    observeWorldBoundsUseCase: ObserveWorldBoundsUseCase,
    getBuildingsUseCase: GetBuildingsUseCase,
    getAviaryUseCase: GetAviaryUseCase,
    getRoadsUseCase: GetRoadsUseCase,
    getTechnicalRoadsUseCase: GetTechnicalRoadsUseCase,
    getTerminalNodesUseCase: GetTerminalNodesUseCase,
    getLinesUseCase: GetLinesUseCase,
    loadAnimalsUseCase: LoadAnimalsUseCase,
    private val stopCompassUseCase: StopCompassUseCase,
    private val startCompassUseCase: StartCompassUseCase,
    private val getAnimalUseCase: GetAnimalUseCase,
    private val findRegionUseCase: FindRegionUseCase,
    private val getRegionCenterPointUseCase: GetRegionCenterPointUseCase,
    private val uploadHistoryUseCase: UploadHistoryUseCase,
    private val getShortestPathUseCase: GetShortestPathFromUserUseCase,
    private val getSnapPathToRoadUseCase: GetSnapPathToRoadUseCase,
) : ViewModel() {

    private val state = NullSafeMutableLiveData(MapState())
    private val currentState get() = checkNotNull(state.value)
    val viewState: LiveData<MapViewState> = state.map(mapper::from)

    private val volatileState = NullSafeMutableLiveData(MapVolatileState())
    val volatileViewState: LiveData<MapVolatileViewState> = volatileState.map(mapper::from)

    private val mapWorldState = NullSafeMutableLiveData(MapWorldState())
    var mapWorldViewState: LiveData<MapWorldViewState> = mapWorldState.mapInBackground(mapper::from)

    private val _effect = Channel<MapEffect>()
    val effect: Flow<MapEffect> = _effect.receiveAsFlow()

    init {
        launchInBackground {
            loadAnimalsUseCase.run()

            observeCompassUseCase.run()
                .onEach { volatileState.reduceOnMain { copy(compass = it) } }
                .launchIn(this)
            getUserPositionUseCase.run()
                .onEach {
                    volatileState.reduceOnMain { copy(userPosition = it) }
                    with(currentState) {
                        if (isToolbarOpened) {
                            when (toolbarMode) {
                                is MapToolbarMode.NavigableMapActionMode -> startNavigationToNearestRegion(toolbarMode.mapAction)
                                is MapToolbarMode.SelectedAnimalMode -> navigationToAnimal(toolbarMode.animal, toolbarMode.regionId)
                                else -> Unit
                            }
                        }
                    }
                }
                .launchIn(this)
            observeTakenRouteUseCase.run()
                .onEach {
                    volatileState.reduceOnMain { copy(takenRoute = it) }
                }
                .launchIn(this)
            getRegionsWithAnimalsInUserPositionUseCase.run()
                .onEach { state.reduceOnMain { copy(regionsWithAnimalsInUserPosition = it) } }
                .launchIn(this)

            combine(
                observeWorldBoundsUseCase.run(),
                getBuildingsUseCase.run(),
                getAviaryUseCase.run(),
                getRoadsUseCase.run(),
                getLinesUseCase.run(),
                observeOldTakenRouteUseCase.run(),
                getTechnicalRoadsUseCase.run(),
                getTerminalNodesUseCase.run(),
            ) { worldBounds, buildings, aviary, roads, lines, taken, technicalRoute, terminalPoints ->
                mapWorldState.reduceOnMain {
                    copy(
                        worldBounds = worldBounds,
                        buildings = buildings,
                        aviary = aviary,
                        roads = roads,
                        lines = lines,
                        oldTakenRoute = taken,
                        technicalRoute = technicalRoute,
                        terminalPoints = terminalPoints,
                    )
                }
                launchInBackground {
                    val snappedTaken = getSnapPathToRoadUseCase.run(taken)
                    mapWorldState.reduceOnMain {
                        copy(oldTakenRoute = snappedTaken)
                    }
                }
            }.launchIn(this)

            if (animalId != null) {
                onMyLocationClicked()
                navigationToAnimal(getAnimalUseCase.run(animalId), regionId)
            }
        }
    }

    private suspend fun navigationToAnimal(animal: AnimalEntity, regionId: RegionId?) {
        val regionIds = if (regionId != null) {
            listOf(regionId)
        } else {
            animal.regionInZoo
        }

        val pathToNearestWithDistance = findNearRegionWithDistance { it.id in regionIds } ?: return

        val shortestPath = pathToNearestWithDistance.first
        val distance = pathToNearestWithDistance.second

        state.reduceOnMain {
            copy(
                toolbarMode = MapToolbarMode.SelectedAnimalMode(
                    animal = animal,
                    distance = distance,
                    regionId = regionId,
                ),
                isToolbarOpened = true,
            )
        }
        volatileState.reduceOnMain {
            copy(
                snappedPoint = shortestPath.last(),
                shortestPath = shortestPath,
            )
        }
    }

    private fun onUploadClicked() {
        try {
            uploadHistoryUseCase.run()
        } catch (ignored: UploadHistoryUseCase.UploadFailed) {
            launchInMain {
                _effect.send(MapEffect.ShowToast(Text("Upload failed")))
            }
        }
    }

    fun onStopCentering() {
        stopCompassUseCase.run()
    }

    fun onStartCentering() {
        startCompassUseCase.run()
    }

    fun onPointPlaced(point: PointD) {
        launchInBackground {
            val regionsAndAnimals = getRegionsContainingPointUseCase.run(point)
                .map { region -> region to getAnimalsInRegionUseCase.run(region.id) }
                .filter { (_, animals) -> animals.isNotEmpty() }

            if (regionsAndAnimals.isEmpty()) {
                state.reduceOnMain {
                    copy(isToolbarOpened = false)
                }
                return@launchInBackground
            } else {
                state.reduceOnMain {
                    copy(
                        toolbarMode = MapToolbarMode.SelectedRegionMode(regionsAndAnimals),
                        isToolbarOpened = true,
                    )
                }
            }
        }
        volatileState.reduce { copy(snappedPoint = point) }
        launchInBackground {
            val shortestPath = getShortestPathUseCase.run(point)
            volatileState.reduceOnMain { copy(shortestPath = shortestPath) }
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

    fun onRegionClicked(router: MapRouter, regionId: RegionId) {
        router.navigateToAnimalList(regionId)
    }

    fun onCloseClicked() {
        state.reduce {
            copy(
                isToolbarOpened = false,
            )
        }
        volatileState.reduce {
            copy(
                snappedPoint = null,
                shortestPath = emptyList(),
            )
        }
    }

    fun onBackClicked(router: MapRouter) {
        router.goBack()
    }

    fun onMapActionClicked(mapAction: MapAction) {
        onMyLocationClicked()
        val toolbarMode = when (mapAction) {
            MapAction.AROUND_YOU -> MapToolbarMode.AroundYouMapActionMode(mapAction)
            MapAction.UPLOAD -> {
                onUploadClicked()
                null
            }
            else -> MapToolbarMode.NavigableMapActionMode(mapAction)
        }
        state.reduce {
            copy(
                toolbarMode = toolbarMode,
                isToolbarOpened = toolbarMode != null,
            )
        }
        if (toolbarMode is MapToolbarMode.NavigableMapActionMode) startNavigationToNearestRegion(mapAction)
    }

    private fun startNavigationToNearestRegion(mapAction: MapAction) {
        launchInBackground {
            val nearWithDistance =
                when (mapAction) {
                    MapAction.WC -> findNearRegionWithDistance<Region.WcRegion>()
                    MapAction.RESTAURANT -> findNearRegionWithDistance<Region.RestaurantRegion>()
                    MapAction.EXIT -> findNearRegionWithDistance<Region.ExitRegion>()
                    else -> throw IllegalStateException("Don't expect navigation to $mapAction")
                }
            onMain {
                if (nearWithDistance != null) {
                    val path = nearWithDistance.first
                    val distance = nearWithDistance.second

                    (currentState.toolbarMode as? MapToolbarMode.NavigableMapActionMode)?.let { currentMode ->
                        state.reduce {
                            copy(
                                toolbarMode = currentMode.copy(
                                    path = path,
                                    distance = distance,
                                ),
                            )
                        }
                        volatileState.reduce {
                            copy(
                                snappedPoint = path.last(),
                                shortestPath = path,
                            )
                        }
                    }
                } else {
                    state.reduce { copy(isToolbarOpened = false) }
                    _effect.send(MapEffect.ShowToast(Text.Res(R.string.cannot_find_near, Text(mapAction.title))))
                }
            }
        }
    }

    private suspend fun findNearRegionWithDistance(condition: (Region) -> Boolean): Pair<List<PointD>, Double>? =
        findRegionUseCase.run(condition)
            .map { getRegionCenterPointUseCase.run(regionId = it.id) }
            .map { getShortestPathUseCase.run(it) }
            .map { it to it.toLengthInMeters() }
            .minByOrNull { (_, length) -> length }

    private suspend inline fun <reified T> findNearRegionWithDistance(): Pair<List<PointD>, Double>? =
        findNearRegionWithDistance { it is T }

    private fun List<PointD>.toLengthInMeters(): Double =
        this
            .zipWithNext()
            .map { (p1, p2) -> haversine(p1.x, p1.y, p2.x, p2.y) }
            .sum()

    fun onAnimalClicked(router: MapRouter, animalId: AnimalId) {
        router.navigateToAnimal(animalId)
    }

    fun onStopEvent() {
        stopCompassUseCase.run()
    }
}