package com.jacekpietras.zoo.map.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jacekpietras.geometry.PointD
import com.jacekpietras.zoo.core.dispatcher.dispatcherProvider
import com.jacekpietras.zoo.core.dispatcher.flowOnBackground
import com.jacekpietras.zoo.core.dispatcher.launchInBackground
import com.jacekpietras.zoo.core.dispatcher.onMain
import com.jacekpietras.zoo.core.text.RichText
import com.jacekpietras.zoo.domain.feature.animal.interactor.GetAnimalUseCase
import com.jacekpietras.zoo.domain.feature.animal.interactor.LoadAnimalsUseCase
import com.jacekpietras.zoo.domain.feature.map.interactor.LoadMapUseCase
import com.jacekpietras.zoo.domain.feature.map.interactor.ObserveAviaryUseCase
import com.jacekpietras.zoo.domain.feature.map.interactor.ObserveBuildingsUseCase
import com.jacekpietras.zoo.domain.feature.map.interactor.ObserveMapLinesUseCase
import com.jacekpietras.zoo.domain.feature.map.interactor.ObserveRoadsUseCase
import com.jacekpietras.zoo.domain.feature.map.interactor.ObserveTechnicalRoadsUseCase
import com.jacekpietras.zoo.domain.feature.map.interactor.ObserveWorldBoundsUseCase
import com.jacekpietras.zoo.domain.feature.pathfinder.interactor.GetShortestPathFromUserUseCase
import com.jacekpietras.zoo.domain.feature.planner.interactor.ObserveCurrentPlanPathWithOptimizationUseCase
import com.jacekpietras.zoo.domain.feature.sensors.interactor.ObserveCompassUseCase
import com.jacekpietras.zoo.domain.feature.sensors.interactor.StartCompassUseCase
import com.jacekpietras.zoo.domain.feature.sensors.interactor.StartNavigationUseCase
import com.jacekpietras.zoo.domain.feature.sensors.interactor.StopCompassUseCase
import com.jacekpietras.zoo.domain.interactor.FindNearRegionWithDistanceUseCase
import com.jacekpietras.zoo.domain.interactor.GetAnimalsInRegionUseCase
import com.jacekpietras.zoo.domain.interactor.GetRegionsContainingPointUseCase
import com.jacekpietras.zoo.domain.interactor.LoadVisitedRouteUseCase
import com.jacekpietras.zoo.domain.interactor.ObserveOldTakenRouteUseCase
import com.jacekpietras.zoo.domain.interactor.ObserveRegionsWithAnimalsInUserPositionUseCase
import com.jacekpietras.zoo.domain.interactor.ObserveSuggestedThemeTypeUseCase
import com.jacekpietras.zoo.domain.interactor.ObserveTakenRouteUseCase
import com.jacekpietras.zoo.domain.interactor.ObserveUserPositionUseCase
import com.jacekpietras.zoo.domain.interactor.ObserveVisitedRoadsUseCase
import com.jacekpietras.zoo.domain.interactor.UploadHistoryUseCase
import com.jacekpietras.zoo.domain.model.AnimalEntity
import com.jacekpietras.zoo.domain.model.AnimalId
import com.jacekpietras.zoo.domain.model.Region
import com.jacekpietras.zoo.domain.model.RegionId
import com.jacekpietras.zoo.map.BuildConfig
import com.jacekpietras.zoo.map.R
import com.jacekpietras.zoo.map.extensions.combine
import com.jacekpietras.zoo.map.extensions.reduce
import com.jacekpietras.zoo.map.extensions.reduceOnMain
import com.jacekpietras.zoo.map.mapper.MapViewStateMapper
import com.jacekpietras.zoo.map.model.MapAction
import com.jacekpietras.zoo.map.model.MapEffect
import com.jacekpietras.zoo.map.model.MapEffect.CenterAtUser
import com.jacekpietras.zoo.map.model.MapEffect.ShowToast
import com.jacekpietras.zoo.map.model.MapState
import com.jacekpietras.zoo.map.model.MapToolbarMode
import com.jacekpietras.zoo.map.model.MapViewState
import com.jacekpietras.zoo.map.model.MapVolatileState
import com.jacekpietras.zoo.map.model.MapVolatileViewState
import com.jacekpietras.zoo.map.model.MapWorldViewState
import com.jacekpietras.zoo.map.router.MapRouter
import com.jacekpietras.zoo.map.service.TrackingServiceStarter
import com.jacekpietras.zoo.tracking.permissions.GpsPermissionRequester
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.plus

internal class MapViewModel(
    animalId: String?,
    regionId: String?,
    mapper: MapViewStateMapper,

    observeCompassUseCase: ObserveCompassUseCase,
    observeSuggestedThemeTypeUseCase: ObserveSuggestedThemeTypeUseCase,
    observeCurrentPlanPathUseCase: ObserveCurrentPlanPathWithOptimizationUseCase,
    observeUserPositionUseCase: ObserveUserPositionUseCase,
    private val stopCompassUseCase: StopCompassUseCase,
    private val startCompassUseCase: StartCompassUseCase,
    private val startNavigationUseCase: StartNavigationUseCase,
    private val trackingServiceStarter: TrackingServiceStarter,

    observeWorldBoundsUseCase: ObserveWorldBoundsUseCase,
    observeBuildingsUseCase: ObserveBuildingsUseCase,
    observeAviaryUseCase: ObserveAviaryUseCase,
    observeRoadsUseCase: ObserveRoadsUseCase,
    observeTechnicalRoadsUseCase: ObserveTechnicalRoadsUseCase,
    observeTakenRouteUseCase: ObserveTakenRouteUseCase,
    observeOldTakenRouteUseCase: ObserveOldTakenRouteUseCase,
    observeMapLinesUseCase: ObserveMapLinesUseCase,
    observeVisitedRoadsUseCase: ObserveVisitedRoadsUseCase,

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

    val effects = MutableStateFlow<List<MapEffect>>(emptyList())

    private val state = MutableStateFlow(MapState())
    val viewState: Flow<MapViewState> = combine(
        state,
        observeSuggestedThemeTypeUseCase.run(),
        observeRegionsWithAnimalsInUserPositionUseCase.run(),
        mapper::from,
    ).flowOnBackground()

    private val volatileState = MutableStateFlow(MapVolatileState())
    val volatileViewState: Flow<MapVolatileViewState> = combine(
        volatileState,
        observeCurrentPlanPathUseCase.run(),
        observeVisitedRoadsUseCase.run(),
        observeTakenRouteUseCase.run(),
        observeCompassUseCase.run(),
        mapper::from,
    ).flowOnBackground()

    val mapWorldViewState: Flow<MapWorldViewState> = combine(
        observeWorldBoundsUseCase.run(),
        observeBuildingsUseCase.run(),
        observeAviaryUseCase.run(),
        observeRoadsUseCase.run(),
        observeMapLinesUseCase.run(),
        observeTechnicalRoadsUseCase.run(),
        observeOldTakenRouteUseCase.run(),
        mapper::from,
    ).flowOnBackground()

    init {
        launchInBackground {
            listOf(
                async { loadAnimalsUseCase.run() },
                async { loadMapUseCase.run() },
            ).awaitAll()

            val animalIdObj = animalId
                ?.takeIf { it.isNotBlank() }
                ?.takeIf { it != "null" }
                ?.let(::AnimalId)

            val regionIdObj = regionId
                ?.takeIf { it.isNotBlank() }
                ?.takeIf { it != "null" }
                ?.let(::RegionId)

            if (animalIdObj != null) {
                sendEffect(CenterAtUser)
                navigationToAnimal(getAnimalUseCase.run(animalIdObj), regionIdObj)
            }

            loadVisitedRouteUseCase.run()
        }

        observeUserPositionUseCase.run()
            .onEach {
                volatileState.reduceOnMain { copy(userPosition = it) }
                with(state.value) {
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
            sendEffect(ShowToast(RichText("Upload failed")))
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
            onDenied = { onLocationDenied() },
            onGranted = {
                trackingServiceStarter.run()
                sendEffect(CenterAtUser)
            },
        )
    }

    private fun onLocationDenied() {
        if (BuildConfig.DEBUG) {
            sendEffect(ShowToast(RichText(R.string.location_denied)))
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
        sendEffect(CenterAtUser)
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

                    (state.value.toolbarMode as? MapToolbarMode.NavigableMapActionMode)?.let { currentMode ->
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
                    sendEffect(ShowToast(RichText.Res(R.string.cannot_find_near, RichText(mapAction.title))))
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

    fun consumeEffect() {
        effects.value = effects.value.drop(1)
    }

    private fun sendEffect(effect: MapEffect) {
        effects.value += effect
    }
}
