package com.jacekpietras.zoo.map.mapper

import android.graphics.Color
import com.jacekpietras.geometry.PointD
import com.jacekpietras.mapview.model.MapColor
import com.jacekpietras.mapview.model.MapDimension
import com.jacekpietras.mapview.model.MapItem
import com.jacekpietras.mapview.model.MapItem.BitmapMapItem
import com.jacekpietras.mapview.model.MapItem.MapColoredItem.CircleMapItem
import com.jacekpietras.mapview.model.MapItem.MapColoredItem.PathMapItem
import com.jacekpietras.mapview.model.MapItem.MapColoredItem.PolygonMapItem
import com.jacekpietras.mapview.model.MapPaint
import com.jacekpietras.mapview.model.PathD
import com.jacekpietras.mapview.model.Pivot
import com.jacekpietras.mapview.model.PolygonD
import com.jacekpietras.zoo.core.text.Dictionary.findFacilityDrawableRes
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
import com.jacekpietras.zoo.map.model.BitmapLibrary
import com.jacekpietras.zoo.map.model.BitmapVersions
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

        return with(state) {
            with(ComposeColors(mapColors)) {
                MapVolatileViewState(
                    compass = compass,
                    userPosition = userPosition,
                    mapData = flatListOf(
                        fromPaths(visitedRoads, visitedRoadsPaint),
                        fromPaths(takenRoute, takenRoutePaint),
                        if (shortestPath.isNotEmpty()) {
                            fromPath(shortestPath, shortestPathPaint) +
                                    fromPoint(shortestPath.last(), snappedPointPaint)
                        } else {
                            fromPath(navigationPlan.points, shortestPathPaint) +
                                    fromPoints(navigationPlan.stops, snappedPointPaint) +
                                    fromPolygons(navigationPlan.firstTurnArrowOuter.map(::PolygonEntity), turnArrowOuterPaint, ZOOM_MEDIUM) +
                                    fromPolygons(navigationPlan.firstTurnArrowInner.map(::PolygonEntity), turnArrowInnerPaint, ZOOM_MEDIUM)
                        },
                        fromPoint(userPosition, userPositionRadiusPaint(mapColors, userPositionAccuracy)),
                        fromPoint(userPosition, userPositionShadowPaint),
                        fromPoint(userPosition, userPositionBorderPaint),
                        fromPoint(userPosition, userPositionPaint),
                    ).toImmutableList(),
                )
            }
        }
    }

    fun from(
        mapColors: MapColors,
        bitmapLibrary: BitmapLibrary,
        mapObjects: ObserveMapObjectsUseCase.MapObject,
    ): MapWorldViewState =
        with(mapObjects) {
            with(ComposeColors(mapColors)) {
                MapWorldViewState(
                    worldBounds = worldBounds,
                    mapData = flatListOf(
                        fromPolygons(water, waterPaint, ZOOM_MEDIUM),
                        fromPolygons(forest, forestPaint),
                        fromPaths(technicalRoads, technicalPaint, ZOOM_CLOSE),
                        fromPaths(roads, roadPaint),
                        fromPaths(lines, linesPaint, ZOOM_CLOSE),
                        fromPolygons(buildings, buildingPaint),
                        fromPolygons(aviary, aviaryPaint),
                        fromTrees(mapColors.nightTheme, trees, bitmapLibrary.data["tree"]),
                        fromPaths(rawOldTakenRoute, oldTakenRoutePaint),
                        fromRegions(mapColors.nightTheme, regionsWithCenters, bitmapLibrary),
                    ),
                )
            }
        }

    private fun fromTrees(nightTheme: Boolean, trees: List<Pair<PointD, Float>>, bitmapVersions: BitmapVersions?): List<MapItem> {
        val bitmap = bitmapVersions?.get(nightTheme)
            ?: return emptyList()

        return trees.map { (position, zoom) ->
            val finalZoom = ZOOM_CLOSE + zoom * (ZOOM_FAR - ZOOM_CLOSE)
            BitmapMapItem(
                point = position,
                bitmap = bitmap,
                minZoom = finalZoom,
            )
        }
    }

    private fun fromRegions(
        nightTheme: Boolean,
        regions: List<Pair<Region, PointD>>,
        bitmapLibrary: BitmapLibrary,
    ): List<MapItem> =
        regions.mapNotNull { (region, position) ->
            fromRegion(
                nightTheme = nightTheme,
                region = region,
                position = position,
                bitmapLibrary = bitmapLibrary,
            )
        }

    private fun fromRegion(
        nightTheme: Boolean,
        region: Region,
        position: PointD,
        bitmapLibrary: BitmapLibrary,
    ): MapItem? {
        bitmapLibrary.data.entries.forEach { (key, bitmapVersions) ->
            if (region.id.id.startsWith(key)) {
                val bitmap = bitmapVersions.get(nightTheme)
                    ?: return null

                return BitmapMapItem(
                    point = position,
                    bitmap = bitmap,
                    minZoom = ZOOM_MEDIUM,
                    pivot = Pivot.CENTER,
                )
            }
        }
        return null
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
                } else if (animalsInRegion.isNotEmpty()) {
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
                } else {
                    val facilityIcon = region.id.id.findFacilityDrawableRes()
                    if (facilityIcon != null) {
                        add(
                            MapCarouselItem.Facility(
                                id = region.id,
                                name = region.id.id.findReadableName(),
                                icon = facilityIcon,
                            )
                        )
                    }
                }
            }
        }.sortedWith(
            compareBy(
                { it !is MapCarouselItem.Facility },
                { it !is MapCarouselItem.Region },
                { it is MapCarouselItem.Animal && it.id !in animalFavorites },
                { (it as? MapCarouselItem.Animal)?.photoUrl == null },
            )
        )

    private fun Division.toViewValue(): AnimalDivisionValue =
        AnimalDivisionValue.valueOf(name)

    private fun userPositionRadiusPaint(mapColors: MapColors, radius: Number): MapPaint =
        MapPaint.Circle(
            fillColor = MapColor.Compose(mapColors.colorAccent.copy(alpha = 0.2f)),
            radius = MapDimension.Dynamic.World(radius.toDouble())
        )

    private class ComposeColors(mapColors: MapColors) {

        val buildingPaint: MapPaint = MapPaint.FillWithBorder(
            fillColor = MapColor.Compose(mapColors.colorMapBuilding),
            borderColor = MapColor.Compose(mapColors.colorMapBuildingBorder),
            borderWidth = MapDimension.Static.Screen(1),
        )
        val aviaryPaint: MapPaint = MapPaint.FillWithBorder(
            fillColor = MapColor.Compose(mapColors.colorMapAviary),
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
        val technicalPaint: MapPaint = MapPaint.DashedStroke(
            strokeColor = MapColor.Compose(mapColors.colorMapTechnical),
            width = MapDimension.Dynamic.World(1.0),
            pattern = MapDimension.Static.Screen(8)
        )
        val linesPaint: MapPaint = MapPaint.Stroke(
            strokeColor = MapColor.Compose(mapColors.colorMapLines),
            width = MapDimension.Dynamic.World(0.5),
        )

        val turnArrowOuterPaint: MapPaint = MapPaint.Fill(
            fillColor = MapColor.Compose(mapColors.colorMapNavigation),
        )
        val turnArrowInnerPaint: MapPaint = MapPaint.Fill(
            fillColor = MapColor.Compose(mapColors.colorMapNavigationArrow),
        )

        val shortestPathPaint: MapPaint = MapPaint.Stroke(
            strokeColor = MapColor.Compose(mapColors.colorMapNavigation),
            width = MapDimension.Static.Screen(4),
        )
        val snappedPointPaint: MapPaint = MapPaint.Circle(
            fillColor = MapColor.Compose(mapColors.colorAccent),
            radius = MapDimension.Static.Screen(dp = 6)
        )
        val userPositionPaint: MapPaint = MapPaint.Circle(
            fillColor = MapColor.Compose(mapColors.colorAccent),
            radius = MapDimension.Static.Screen(dp = 6)
        )
        val userPositionBorderPaint: MapPaint = MapPaint.Circle(
            fillColor = MapColor.Compose(mapColors.userPositionBorder),
            radius = MapDimension.Static.Screen(dp = 8)
        )
        val userPositionShadowPaint: MapPaint = MapPaint.Circle(
            fillColor = MapColor.Compose(mapColors.userPositionShadow),
            radius = MapDimension.Static.Screen(dp = 9)
        )

        val oldTakenRoutePaint: MapPaint = MapPaint.Stroke(
            strokeColor = MapColor.Hard(Color.RED),
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

        const val ZOOM_CLOSE = 0.0012f
        const val ZOOM_MEDIUM = 0.0022f
        const val ZOOM_FAR = 0.0032f
    }
}
