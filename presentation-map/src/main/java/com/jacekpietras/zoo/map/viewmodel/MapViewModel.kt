package com.jacekpietras.zoo.map.viewmodel

import androidx.lifecycle.*
import com.jacekpietras.core.NullSafeMutableLiveData
import com.jacekpietras.core.PointD
import com.jacekpietras.core.combine
import com.jacekpietras.core.reduce
import com.jacekpietras.zoo.core.dispatcher.*
import com.jacekpietras.zoo.core.extensions.mapInBackground
import com.jacekpietras.zoo.core.extensions.reduceOnMain
import com.jacekpietras.zoo.core.text.Text
import com.jacekpietras.zoo.domain.feature.animal.interactor.GetAnimalUseCase
import com.jacekpietras.zoo.domain.feature.animal.interactor.LoadAnimalsUseCase
import com.jacekpietras.zoo.domain.feature.map.interactor.*
import com.jacekpietras.zoo.domain.feature.pathfinder.interactor.GetShortestPathFromUserUseCase
import com.jacekpietras.zoo.domain.feature.planner.interactor.ObserveCurrentPlanPathWithOptimizationUseCase
import com.jacekpietras.zoo.domain.feature.sensors.interactor.ObserveCompassUseCase
import com.jacekpietras.zoo.domain.feature.sensors.interactor.StartCompassUseCase
import com.jacekpietras.zoo.domain.feature.sensors.interactor.StartNavigationUseCase
import com.jacekpietras.zoo.domain.feature.sensors.interactor.StopCompassUseCase
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
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.plus

internal class MapViewModel(
    animalId: AnimalId?,
    regionId: RegionId?,
    mapper: MapViewStateMapper,

    observeCompassUseCase: ObserveCompassUseCase,
    observeSuggestedThemeTypeUseCase: ObserveSuggestedThemeTypeUseCase,
    private val stopCompassUseCase: StopCompassUseCase,
    private val startCompassUseCase: StartCompassUseCase,
    private val startNavigationUseCase: StartNavigationUseCase,
    observeCurrentPlanPathUseCase: ObserveCurrentPlanPathWithOptimizationUseCase,
    getUserPositionUseCase: GetUserPositionUseCase,

    observeWorldBoundsUseCase: ObserveWorldBoundsUseCase,
    observeBuildingsUseCase: ObserveBuildingsUseCase,
    observeAviaryUseCase: ObserveAviaryUseCase,
    observeRoadsUseCase: ObserveRoadsUseCase,
    observeTechnicalRoadsUseCase: ObserveTechnicalRoadsUseCase,
    observeTakenRouteUseCase: ObserveTakenRouteUseCase,
    observeOldTakenRouteUseCase: ObserveOldTakenRouteUseCase,
    observeMapLinesUseCase: ObserveMapLinesUseCase,
    observeVisitedRoadsUseCase: ObserveVisitedRoadsUseCase,
    getTerminalNodesUseCase: GetTerminalNodesUseCase,

    loadAnimalsUseCase: LoadAnimalsUseCase,
    loadMapUseCase: LoadMapUseCase,
    loadVisitedRouteUseCase: LoadVisitedRouteUseCase,
    observeRegionsWithAnimalsInUserPositionUseCase: ObserveRegionsWithAnimalsInUserPositionUseCase,
    private val getAnimalsInRegionUseCase: GetAnimalsInRegionUseCase,
    private val getRegionsContainingPointUseCase: GetRegionsContainingPointUseCase,
    private val getAnimalUseCase: GetAnimalUseCase,
    private val findNearRegionWithDistance: FindNearRegionWithDistanceUseCase,
    private val uploadHistoryUseCase: UploadHistoryUseCase,
    private val getShortestPathUseCase: GetShortestPathFromUserUseCase,
) : ViewModel() {

    private val state = NullSafeMutableLiveData(MapState())
    private val currentState get() = checkNotNull(state.value)
    val viewState: LiveData<MapViewState> = state.map(mapper::from)

    private val volatileState = NullSafeMutableLiveData(MapVolatileState())
    val volatileViewState: LiveData<MapVolatileViewState> =
        combine(
            volatileState.asFlow(),
            observeCurrentPlanPathUseCase.run(),
            observeVisitedRoadsUseCase.run(),
            observeTakenRouteUseCase.run(),
            observeCompassUseCase.run(),
        ) { state, plannedPath, visitedRoads, takenRoute, compass ->
            state.copy(
                plannedPath = plannedPath,
                compass = compass,
                visitedRoads = visitedRoads,
                takenRoute = takenRoute,
            )
        }.asLiveData().map(mapper::from)

    private val mapWorldState = NullSafeMutableLiveData(MapWorldState())
    var mapWorldViewState: LiveData<MapWorldViewState> = mapWorldState.mapInBackground(mapper::from)

    private val _effect = Channel<MapEffect>()
    val effect: Flow<MapEffect> = _effect.receiveAsFlow()

    init {
        launchInBackground {
            listOf(
                async { loadAnimalsUseCase.run() },
                async { loadMapUseCase.run() },
            ).awaitAll()

            if (animalId != null) {
                onMyLocationClicked()
                navigationToAnimal(getAnimalUseCase.run(animalId), regionId)
            }

            @Suppress("DeferredResultUnused")
            async { loadVisitedRouteUseCase.run() }
        }

        observeSuggestedThemeTypeUseCase.run()
            .onEach { state.reduceOnMain { copy(suggestedThemeType = it) } }
            .launchIn(viewModelScope + dispatcherProvider.default)

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
            .launchIn(viewModelScope + dispatcherProvider.default)

        observeRegionsWithAnimalsInUserPositionUseCase.run()
            .onEach { state.reduceOnMain { copy(regionsWithAnimalsInUserPosition = it) } }
            .launchIn(viewModelScope + dispatcherProvider.default)

        combine(
            observeWorldBoundsUseCase.run(),
            observeBuildingsUseCase.run(),
            observeAviaryUseCase.run(),
            observeRoadsUseCase.run(),
            observeMapLinesUseCase.run(),
            observeOldTakenRouteUseCase.run(),
            observeTechnicalRoadsUseCase.run(),
        ) { worldBounds, buildings, aviary, roads, lines, rawTakenRoute, technicalRoads ->
            val terminalPoints = onBackground { getTerminalNodesUseCase.run() }

            mapWorldState.reduceOnMain {
                copy(
                    worldBounds = worldBounds,
                    buildings = buildings,
                    aviary = aviary,
                    lines = lines,
                    roads = roads,
                    technicalRoads = technicalRoads,
                    rawOldTakenRoute = rawTakenRoute,
                    terminalPoints = terminalPoints,
                )
            }
        }.launchIn(viewModelScope)
    }

    private suspend fun navigationToAnimal(animal: AnimalEntity, regionId: RegionId?) {
        val regionIds = if (regionId != null) {
            listOf(regionId)
        } else {
            animal.regionInZoo
        }

        val pathToNearestWithDistance = findNearRegionWithDistance.run { it.id in regionIds } ?: return

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
        startNavigationUseCase.run()
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

    private suspend inline fun <reified T> findNearRegionWithDistance(): Pair<List<PointD>, Double>? =
        findNearRegionWithDistance.run { it is T }

    fun onAnimalClicked(router: MapRouter, animalId: AnimalId) {
        router.navigateToAnimal(animalId)
    }

    fun onStopEvent() {
        stopCompassUseCase.run()
    }
}