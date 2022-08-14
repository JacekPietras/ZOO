package com.jacekpietras.zoo.map.mapper

import android.graphics.Color
import com.jacekpietras.geometry.PointD
import com.jacekpietras.mapview.model.MapColor
import com.jacekpietras.mapview.model.MapDimension
import com.jacekpietras.mapview.model.MapItem
import com.jacekpietras.mapview.model.MapItem.IconMapItem
import com.jacekpietras.mapview.model.MapItem.MapColoredItem.CircleMapItem
import com.jacekpietras.mapview.model.MapItem.MapColoredItem.PathMapItem
import com.jacekpietras.mapview.model.MapItem.MapColoredItem.PolygonMapItem
import com.jacekpietras.mapview.model.MapPaint
import com.jacekpietras.mapview.model.PathD
import com.jacekpietras.mapview.model.PolygonD
import com.jacekpietras.zoo.core.text.Dictionary.findReadableName
import com.jacekpietras.zoo.core.text.RichText
import com.jacekpietras.zoo.core.theme.MapColors
import com.jacekpietras.zoo.domain.feature.animal.model.AnimalEntity
import com.jacekpietras.zoo.domain.feature.animal.model.AnimalId
import com.jacekpietras.zoo.domain.feature.animal.model.Division
import com.jacekpietras.zoo.domain.feature.map.interactor.ObserveMapObjectsUseCase
import com.jacekpietras.zoo.domain.feature.map.model.MapItemEntity.PathEntity
import com.jacekpietras.zoo.domain.feature.map.model.MapItemEntity.PolygonEntity
import com.jacekpietras.zoo.domain.feature.planner.interactor.ObserveCurrentPlanPathWithOptimizationUseCase.NavigationPlan
import com.jacekpietras.zoo.domain.model.Region
import com.jacekpietras.zoo.domain.model.ThemeType
import com.jacekpietras.zoo.map.BuildConfig
import com.jacekpietras.zoo.map.R
import com.jacekpietras.zoo.map.model.AnimalDivisionValue
import com.jacekpietras.zoo.map.model.MapAction
import com.jacekpietras.zoo.map.model.MapCarouselItem
import com.jacekpietras.zoo.map.model.MapState
import com.jacekpietras.zoo.map.model.MapToolbarMode
import com.jacekpietras.zoo.map.model.MapViewState
import com.jacekpietras.zoo.map.model.MapVolatileState
import com.jacekpietras.zoo.map.model.MapVolatileViewState
import com.jacekpietras.zoo.map.model.MapWorldViewState
import kotlinx.collections.immutable.toImmutableList
import kotlin.random.Random

internal class MapViewStateMapper {

    private val carouselSeed = Random.nextLong()

    fun from(
        state: MapState,
        suggestedThemeTypeAndLuminance: Pair<ThemeType, Float?>,
        regionsWithAnimalsInUserPosition: List<Pair<Region, List<AnimalEntity>>>,
        animalFavorites: List<AnimalId>,
    ): MapViewState = with(state) {
        val isValidLocation = userPosition != PointD()
        val (_, luminance) = suggestedThemeTypeAndLuminance

        MapViewState(
            isGuidanceShown = isToolbarOpened,
            isBackArrowShown = toolbarMode is MapToolbarMode.SelectedAnimalMode,
            title = when (toolbarMode) {
                is MapToolbarMode.SelectedAnimalMode -> RichText(toolbarMode.animal.name) + toolbarMode.distance.metersToText()
                is MapToolbarMode.NavigableMapActionMode -> RichText(toolbarMode.mapAction.title) + toolbarMode.distance.metersToText()
                is MapToolbarMode.AroundYouMapActionMode -> RichText(toolbarMode.mapAction.title)
                is MapToolbarMode.SelectedRegionMode -> {
                    if (toolbarMode.regionsWithAnimals.size > 1) {
                        RichText(R.string.selected)
                    } else {
                        toolbarMode.regionsWithAnimals.first().first.id.id.findReadableName()
                    }
                }
                else -> RichText.Empty
            },
            navigationText = planState?.let { it.nextStageRegion.id.findReadableName() + it.distance.metersToText() } ?: RichText.Empty,
            isNavigationVisible = planState != null && !isToolbarOpened,
            luminanceText = luminance?.toInt().toString(),
            mapCarouselItems = when (toolbarMode) {
                is MapToolbarMode.MapActionMode ->
                    when (toolbarMode.mapAction) {
                        MapAction.AROUND_YOU -> getCarousel(regionsWithAnimalsInUserPosition, animalFavorites)
                        else -> emptyList()
                    }
                is MapToolbarMode.SelectedRegionMode -> getCarousel(toolbarMode.regionsWithAnimals, animalFavorites)
                else -> emptyList()
            }.toImmutableList(),
            isMapActionsVisible = !isToolbarOpened,
            mapActions = MapAction.values().asList()
                .filterOutNavigationActions(isValidLocation)
                .filterOutDebugActions()
                .toImmutableList(),
        )
    }

    private fun List<MapAction>.filterOutNavigationActions(isValidLocation: Boolean): List<MapAction> =
        let {
            if (isValidLocation) {
                it
            } else {
                it - actionsWithNavigation
            }
        }

    private fun List<MapAction>.filterOutDebugActions(): List<MapAction> =
        let {
            if (BuildConfig.DEBUG) {
                it
            } else {
                it - actionsWhenDebug
            }
        }

    private fun Double?.metersToText() =
        this?.let { distance ->
            RichText(" ") + RichText(distance.toInt().toString()) + "m"
        } ?: RichText.Empty

    fun from(
        state: MapVolatileState,
        mapColors: MapColors,
        navigationPlan: NavigationPlan,
        visitedRoads: List<PathEntity> = emptyList(),
        takenRoute: List<PathEntity> = emptyList(),
        compass: Float = 0f,
    ): MapVolatileViewState {
        val plannedPath = navigationPlan.points
        val turnPath = navigationPlan.firstTurn
        val turnArrowPath = navigationPlan.firstTurnArrow

        return with(state) {
            with(ComposeColors(mapColors)) {
                MapVolatileViewState(
                    compass = compass,
                    userPosition = userPosition,
                    mapData = flatListOf(
                        fromPaths(visitedRoads, visitedRoadsPaint),
                        fromPaths(takenRoute, takenRoutePaint),
                        if (shortestPath.isNotEmpty()) {
                            fromPath(shortestPath, shortestPathPaint)
                        } else {
                            fromPath(plannedPath, shortestPathPaint) +
                                    fromPath(turnPath, turnPaint, ZOOM_MEDIUM) +
                                    fromPolygon(PolygonEntity(turnArrowPath), turnArrowPaint, ZOOM_MEDIUM)
                        },
                        fromPoint(userPosition, userPositionPaint),
                        fromPoint(snappedPoint, snappedPointPaint),
                    ).toImmutableList(),
                )
            }
        }
    }

    fun from(
        mapColors: MapColors,
        mapObjects: ObserveMapObjectsUseCase.MapObject,
    ): MapWorldViewState =
        with(mapObjects) {
            with(ComposeColors(mapColors)) {
                MapWorldViewState(
                    worldBounds = worldBounds,
                    mapData = flatListOf(
                        fromPaths(technicalRoads, technicalPaint),
                        fromPaths(roads, roadPaint),
                        fromPaths(lines, linesPaint, ZOOM_CLOSE),
                        fromPolygons(buildings, buildingPaint),
                        fromPolygons(aviary, aviaryPaint),
                        fromPolygons(water, waterPaint, ZOOM_MEDIUM),
                        fromPolygons(forest, forestPaint, ZOOM_MEDIUM),
                        fromPaths(rawOldTakenRoute, oldTakenRoutePaint),
                        fromRegions(regionsWithCenters),
                    ),
                )
            }
        }

    private fun fromRegions(regions: List<Pair<Region, PointD>>): List<MapItem> =
        regions.mapNotNull { (region, position) ->
            val (icon, minZoom) = when (region) {
                is Region.WcRegion -> R.drawable.ic_wc_24 to ZOOM_MEDIUM
                is Region.ExitRegion -> R.drawable.ic_door_24 to ZOOM_MEDIUM
//                else -> return@mapNotNull null
                else -> {
                    when (region.id.id) {
                        "wielkie-koty-2" -> R.drawable.ic_region_big_cats_24 to null
                        else -> return@mapNotNull null
                    }
                }
            }
            IconMapItem(
                point = position,
                icon = icon,
                minZoom = minZoom,
            )
        }

    private fun <T> flatListOf(vararg lists: List<T>): List<T> = listOf(*lists).flatten()

    private fun fromPolygon(polygon: PolygonEntity, paint: MapPaint, minZoom: Float? = null): List<MapItem> =
        listOf(
            PolygonMapItem(
                PolygonD(polygon.vertices),
                paint,
                minZoom,
            )
        )

    private fun fromPolygons(polygons: List<PolygonEntity>, paint: MapPaint, minZoom: Float? = null): List<MapItem> =
        polygons.map { polygon ->
            PolygonMapItem(
                PolygonD(polygon.vertices),
                paint,
                minZoom,
            )
        }

    private fun fromPath(path: List<PointD>, paint: MapPaint, minZoom: Float? = null): List<MapItem> =
        listOf(
            PathMapItem(
                PathD(path),
                paint,
                minZoom,
            )
        )

    private fun fromPaths(paths: List<PathEntity>, paint: MapPaint, minZoom: Float? = null): List<MapItem> =
        paths.map { path ->
            PathMapItem(
                PathD(path.vertices),
                paint,
                minZoom,
            )
        }

    @Suppress("unused", "SameParameterValue")
    private fun fromPoints(points: List<PointD>, paint: MapPaint, minZoom: Float? = null): List<MapItem> =
        points.map { point ->
            CircleMapItem(
                point,
                (paint as MapPaint.Circle).radius,
                paint,
                minZoom,
            )
        }

    private fun fromPoint(point: PointD?, paint: MapPaint): List<MapItem> =
        if (point != null) {
            listOf(
                CircleMapItem(
                    point,
                    (paint as MapPaint.Circle).radius,
                    paint,
                )
            )
        } else {
            emptyList()
        }

    private fun getCarousel(
        regionsWithAnimals: List<Pair<Region, List<AnimalEntity>>>,
        animalFavorites: List<AnimalId>,
    ) =
        mutableListOf<MapCarouselItem>().apply {
            regionsWithAnimals.forEach { (region, animalsInRegion) ->
                if (animalsInRegion.size > 5 && regionsWithAnimals.size > 1) {
                    val images = animalsInRegion
                        .map { animal -> animal.photos.map { photo -> photo to animal.division } }
                        .flatten()
                        .shuffled(Random(carouselSeed))
                    add(
                        MapCarouselItem.Region(
                            id = region.id,
                            name = region.id.id.findReadableName(),
                            photoUrlLeftTop = images.getOrNull(0)?.first,
                            photoUrlRightTop = images.getOrNull(1)?.first,
                            photoUrlLeftBottom = images.getOrNull(2)?.first,
                            photoUrlRightBottom = images.getOrNull(3)?.first,
                            divisionLeftTop = images.getOrNull(0)?.second?.toViewValue(),
                            divisionRightTop = images.getOrNull(1)?.second?.toViewValue(),
                            divisionLeftBottom = images.getOrNull(2)?.second?.toViewValue(),
                            divisionRightBottom = images.getOrNull(3)?.second?.toViewValue(),
                        )
                    )
                } else {
                    animalsInRegion.forEach { animal ->
                        add(
                            MapCarouselItem.Animal(
                                id = animal.id,
                                division = animal.division.toViewValue(),
                                name = RichText(animal.name),
                                photoUrl = animal.photos.shuffled(Random(carouselSeed)).firstOrNull(),
                            )
                        )
                    }
                }
            }
        }.sortedWith(
            compareBy(
                { it !is MapCarouselItem.Region },
                { it is MapCarouselItem.Animal && it.id !in animalFavorites },
                { (it as? MapCarouselItem.Animal)?.photoUrl == null },
            )
        )

    private fun Division.toViewValue(): AnimalDivisionValue =
        AnimalDivisionValue.valueOf(name)

    private class ComposeColors(mapColors: MapColors) {

        val buildingPaint: MapPaint = MapPaint.FillWithBorder(
            fillColor = MapColor.Compose(mapColors.colorMapBuilding),
            borderColor = MapColor.Compose(mapColors.colorMapBuildingBorder),
            borderWidth = MapDimension.Static.Screen(1),
        )
        val aviaryPaint: MapPaint = MapPaint.FillWithBorder(
            fillColor = MapColor.Compose(mapColors.colorMapBuilding),
            borderColor = MapColor.Compose(mapColors.colorMapBuildingBorder),
            borderWidth = MapDimension.Static.Screen(1),
        )
        val forestPaint: MapPaint = MapPaint.Fill(
            fillColor = MapColor.Compose(mapColors.colorMapForest),
        )
        val waterPaint: MapPaint = MapPaint.Fill(
            fillColor = MapColor.Compose(mapColors.colorMapWater),
        )
        val roadPaint: MapPaint = MapPaint.StrokeWithBorder(
            strokeColor = MapColor.Compose(mapColors.colorMapRoad),
            width = MapDimension.Dynamic.World(2.0),
            borderColor = MapColor.Compose(mapColors.colorMapRoadBorder),
            borderWidth = MapDimension.Static.Screen(1),
        )
        val visitedRoadsPaint: MapPaint = MapPaint.Stroke(
            strokeColor = MapColor.Compose(mapColors.colorMapRoadVisited),
            width = MapDimension.Dynamic.World(2.0),
        )
        val technicalPaint: MapPaint = MapPaint.Stroke(
            strokeColor = MapColor.Compose(mapColors.colorMapTechnical),
            width = MapDimension.Dynamic.World(2.0),
        )
        val linesPaint: MapPaint = MapPaint.Stroke(
            strokeColor = MapColor.Hard(Color.rgb(240, 180, 140)),
            width = MapDimension.Dynamic.World(0.5),
        )
        val turnPaint: MapPaint = MapPaint.Stroke(
            strokeColor = MapColor.Hard(Color.argb(255, 150, 150, 255)),
            width = MapDimension.Dynamic.World(2.0),
        )
        val turnArrowPaint: MapPaint = MapPaint.Fill(
            fillColor = MapColor.Hard(Color.argb(255, 150, 150, 255)),
        )

        val shortestPathPaint: MapPaint = MapPaint.DashedStroke(
            strokeColor = MapColor.Hard(Color.argb(50, 0, 0, 255)),
            width = MapDimension.Static.Screen(4),
            pattern = MapDimension.Static.Screen(dp = 3),
        )
        val snappedPointPaint: MapPaint = MapPaint.Circle(
            fillColor = MapColor.Hard(Color.BLUE),
            radius = MapDimension.Static.Screen(dp = 4)
        )
        val userPositionPaint: MapPaint = MapPaint.Circle(
            fillColor = MapColor.Compose(mapColors.colorPrimary),
            radius = MapDimension.Static.Screen(dp = 8)
        )

        val oldTakenRoutePaint: MapPaint = MapPaint.Stroke(
            strokeColor = MapColor.Hard(Color.rgb(150, 180, 150)),
            width = MapDimension.Static.Screen(0.5),
        )
        val takenRoutePaint: MapPaint = MapPaint.Stroke(
            strokeColor = MapColor.Compose(mapColors.colorMapTaken),
            width = MapDimension.Static.Screen(0.5),
        )
    }

    private companion object {

        val actionsWhenDebug = listOf(
            MapAction.UPLOAD,
        )
        val actionsWithNavigation = listOf(
            MapAction.AROUND_YOU,
            MapAction.WC,
            MapAction.RESTAURANT,
            MapAction.EXIT,
        )

        val ZOOM_CLOSE = 0.001f
        val ZOOM_MEDIUM = 0.002f
    }
}
