package com.jacekpietras.zoo.map.mapper

import android.graphics.Color
import com.jacekpietras.core.PointD
import com.jacekpietras.mapview.model.*
import com.jacekpietras.zoo.core.text.Dictionary.findReadableName
import com.jacekpietras.zoo.core.text.Text
import com.jacekpietras.zoo.domain.model.AnimalEntity
import com.jacekpietras.zoo.domain.model.Division
import com.jacekpietras.zoo.domain.model.MapItemEntity.PathEntity
import com.jacekpietras.zoo.domain.model.MapItemEntity.PolygonEntity
import com.jacekpietras.zoo.domain.model.Region
import com.jacekpietras.zoo.domain.model.ThemeType
import com.jacekpietras.zoo.map.BuildConfig
import com.jacekpietras.zoo.map.R
import com.jacekpietras.zoo.map.model.*
import kotlin.random.Random

internal class MapViewStateMapper {

    private val carouselSeed = Random.nextLong()

    fun from(state: MapState): MapViewState = with(state) {
        MapViewState(
            isGuidanceShown = isToolbarOpened,
            isNightThemeSuggested = suggestedThemeType == ThemeType.NIGHT,
            isBackArrowShown = toolbarMode is MapToolbarMode.SelectedAnimalMode,
            title = when (toolbarMode) {
                is MapToolbarMode.SelectedAnimalMode -> Text(toolbarMode.animal.name) + toolbarMode.distance.metersToText()
                is MapToolbarMode.NavigableMapActionMode -> Text(toolbarMode.mapAction.title) + toolbarMode.distance.metersToText()
                is MapToolbarMode.AroundYouMapActionMode -> Text(toolbarMode.mapAction.title)
                is MapToolbarMode.SelectedRegionMode -> {
                    if (toolbarMode.regionsWithAnimals.size > 1) {
                        Text(R.string.selected)
                    } else {
                        toolbarMode.regionsWithAnimals.first().first.id.id.findReadableName()
                    }
                }
                else -> Text.Empty
            },

            mapCarouselItems = when (toolbarMode) {
                is MapToolbarMode.MapActionMode ->
                    when (toolbarMode.mapAction) {
                        MapAction.AROUND_YOU -> getCarousel(state.regionsWithAnimalsInUserPosition)
                        else -> emptyList()
                    }
                is MapToolbarMode.SelectedRegionMode -> getCarousel(toolbarMode.regionsWithAnimals)
                else -> emptyList()
            },
            isMapActionsVisible = !isToolbarOpened,
            mapActions = MapAction.values()
                .filter { BuildConfig.DEBUG || it != MapAction.UPLOAD },
        )
    }

    private fun Double?.metersToText() =
        this?.let { distance ->
            Text(" ") + Text(distance.toInt().toString()) + "m"
        } ?: Text.Empty

    fun from(state: MapVolatileState): MapVolatileViewState = with(state) {
        MapVolatileViewState(
            compass = compass,
            userPosition = userPosition,
            mapData = flatListOf(
                fromPaths(visitedRoads, visitedRoadsPaint),
                fromPaths(takenRoute, takenRoutePaint),
                if (shortestPath.isNotEmpty()) {
                    fromPath(shortestPath, shortestPathPaint)
                } else {
                    fromPath(plannedPath, shortestPathPaint)
                },
                fromPoint(userPosition, userPositionPaint),
                fromPoint(snappedPoint, snappedPointPaint),
            ),
        )
    }

    fun from(state: MapWorldState): MapWorldViewState = with(state) {
        MapWorldViewState(
            worldBounds = worldBounds,
            mapData = flatListOf(
                fromPaths(technicalRoads, technicalPaint),
                fromPaths(roads, roadPaint),
                fromPaths(lines, linesPaint),
                fromPolygons(buildings, buildingPaint),
                fromPolygons(aviary, aviaryPaint),
                fromPaths(rawOldTakenRoute, oldTakenRoutePaint),
                fromPoints(terminalPoints, terminalPaint),
            ),
        )
    }

    private fun <T> flatListOf(vararg lists: List<T>): List<T> = listOf(*lists).flatten()

    private fun fromPolygons(
        polygons: List<PolygonEntity>,
        paint: MapPaint
    ): List<MapItem> =
        polygons.map { polygon ->
            MapItem.PolygonMapItem(
                PolygonD(polygon.vertices),
                paint,
            )
        }

    private fun fromPath(path: List<PointD>, paint: MapPaint): List<MapItem> =
        listOf(
            MapItem.PathMapItem(
                PathD(path),
                paint,
            )
        )

    private fun fromPaths(paths: List<PathEntity>, paint: MapPaint): List<MapItem> =
        paths.map { path ->
            MapItem.PathMapItem(
                PathD(path.vertices),
                paint,
            )
        }

    @Suppress("unused")
    private fun fromPoints(points: List<PointD>, paint: MapPaint): List<MapItem> =
        points.map { point ->
            MapItem.CircleMapItem(
                point,
                (paint as MapPaint.Circle).radius,
                paint,
            )
        }

    private fun fromPoint(point: PointD?, paint: MapPaint): List<MapItem> =
        if (point != null) {
            listOf(
                MapItem.CircleMapItem(
                    point,
                    (paint as MapPaint.Circle).radius,
                    paint,
                )
            )
        } else {
            emptyList()
        }

    private fun getCarousel(regionsWithAnimals: List<Pair<Region, List<AnimalEntity>>>) =
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
                                name = Text(animal.name),
                                photoUrl = animal.photos.shuffled(Random(carouselSeed)).firstOrNull(),
                            )
                        )
                    }
                }
            }
        }.sortedWith(
            compareBy(
                { it !is MapCarouselItem.Region },
                { (it as? MapCarouselItem.Animal)?.photoUrl == null },
            )
        )

    private fun Division.toViewValue(): AnimalDivisionValue =
        AnimalDivisionValue.valueOf(name)

    private companion object {

        val buildingPaint: MapPaint = MapPaint.FillWithBorder(
            fillColor = MapColor.Attribute(R.attr.colorMapBuilding),
            borderColor = MapColor.Attribute(R.attr.colorMapBuildingBorder),
            borderWidth = MapDimension.Static.Screen(1),
        )
        val aviaryPaint: MapPaint = MapPaint.FillWithBorder(
            fillColor = MapColor.Attribute(R.attr.colorMapBuilding),
            borderColor = MapColor.Attribute(R.attr.colorMapBuildingBorder),
            borderWidth = MapDimension.Static.Screen(1),
        )
        val roadPaint: MapPaint = MapPaint.StrokeWithBorder(
            strokeColor = MapColor.Attribute(R.attr.colorMapRoad),
            width = MapDimension.Dynamic.World(2.0),
            borderColor = MapColor.Attribute(R.attr.colorMapRoadBorder),
            borderWidth = MapDimension.Static.Screen(1),
        )
        val visitedRoadsPaint: MapPaint = MapPaint.Stroke(
            strokeColor = MapColor.Attribute(R.attr.colorMapRoadVisited),
            width = MapDimension.Dynamic.World(2.0),
        )
        val technicalPaint: MapPaint = MapPaint.StrokeWithBorder(
            strokeColor = MapColor.Attribute(R.attr.colorMapTechnical),
            width = MapDimension.Dynamic.World(2.0),
            borderColor = MapColor.Attribute(R.attr.colorMapTechnicalBorder),
            borderWidth = MapDimension.Static.Screen(1),
        )
        val linesPaint: MapPaint = MapPaint.Stroke(
            strokeColor = MapColor.Hard(Color.rgb(240, 180, 140)),
            width = MapDimension.Dynamic.World(0.5),
        )

        @Suppress("unused")
        val terminalPaint: MapPaint = MapPaint.Circle(
            fillColor = MapColor.Hard(Color.RED),
            radius = MapDimension.Dynamic.World(meters = 1.0),
        )
        val shortestPathPaint: MapPaint = MapPaint.DashedStroke(
//            strokeColor = MapColor.Hard(Color.BLUE),
            strokeColor = MapColor.Hard(Color.argb(50, 0, 0, 255)),
            width = MapDimension.Static.Screen(4),
            pattern = MapDimension.Static.Screen(dp = 3),
        )
        val snappedPointPaint: MapPaint = MapPaint.Circle(
            fillColor = MapColor.Hard(Color.BLUE),
            radius = MapDimension.Static.Screen(dp = 4)
        )
        val userPositionPaint: MapPaint = MapPaint.Circle(
            fillColor = MapColor.Attribute(com.jacekpietras.mapview.R.attr.colorPrimary),
            radius = MapDimension.Static.Screen(dp = 8)
        )

        @Suppress("unused")
        val oldTakenRoutePaint: MapPaint = MapPaint.Stroke(
            strokeColor = MapColor.Hard(Color.rgb(150, 180, 150)),
            width = MapDimension.Static.Screen(0.5),
        )
        val takenRoutePaint: MapPaint = MapPaint.Stroke(
            strokeColor = MapColor.Attribute(R.attr.colorMapTaken),
            width = MapDimension.Static.Screen(0.5),
        )
    }
}
