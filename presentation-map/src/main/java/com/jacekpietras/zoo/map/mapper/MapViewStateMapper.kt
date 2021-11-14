package com.jacekpietras.zoo.map.mapper

import com.jacekpietras.core.PointD
import com.jacekpietras.mapview.model.MapItem
import com.jacekpietras.mapview.model.MapPaint
import com.jacekpietras.mapview.model.PathD
import com.jacekpietras.mapview.model.PolygonD
import com.jacekpietras.zoo.core.RegionMapper
import com.jacekpietras.zoo.core.text.Text
import com.jacekpietras.zoo.domain.model.AnimalEntity
import com.jacekpietras.zoo.domain.model.MapItemEntity.PathEntity
import com.jacekpietras.zoo.domain.model.MapItemEntity.PolygonEntity
import com.jacekpietras.zoo.domain.model.Region
import com.jacekpietras.zoo.map.R
import com.jacekpietras.zoo.map.model.*
import com.jacekpietras.zoo.map.model.MapVolatileState.Companion.shortestPathPaint
import com.jacekpietras.zoo.map.model.MapVolatileState.Companion.snappedPointPaint
import com.jacekpietras.zoo.map.model.MapVolatileState.Companion.takenRoutePaint
import com.jacekpietras.zoo.map.model.MapVolatileState.Companion.userPositionPaint
import com.jacekpietras.zoo.map.model.MapWorldState.Companion.aviaryPaint
import com.jacekpietras.zoo.map.model.MapWorldState.Companion.buildingPaint
import com.jacekpietras.zoo.map.model.MapWorldState.Companion.linesPaint
import com.jacekpietras.zoo.map.model.MapWorldState.Companion.roadPaint
import com.jacekpietras.zoo.map.model.MapWorldState.Companion.technicalPaint
import com.jacekpietras.zoo.map.model.MapWorldState.Companion.terminalPaint
import kotlin.random.Random

internal class MapViewStateMapper(
    private val regionMapper: RegionMapper,
) {

    private val carouselSeed = Random.nextLong()

    fun from(state: MapState): MapViewState = with(state) {
        MapViewState(
            isGuidanceShown = isToolbarOpened,
            isBackArrowShown = toolbarMode is MapToolbarMode.SelectedAnimalMode,
            title = when (toolbarMode) {
                is MapToolbarMode.SelectedAnimalMode -> Text(toolbarMode.animal.name) + toolbarMode.distance.metersToText()
                is MapToolbarMode.NavigableMapActionMode -> Text(toolbarMode.mapAction.title) + toolbarMode.distance.metersToText()
                is MapToolbarMode.AroundYouMapActionMode -> Text(toolbarMode.mapAction.title)
                is MapToolbarMode.SelectedRegionMode -> {
                    if (toolbarMode.regionsWithAnimals.size > 1) {
                        Text(R.string.selected)
                    } else {
                        toolbarMode.regionsWithAnimals.first().first.id.id.let(regionMapper::from)
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
            mapActions = MapAction.values().asList(),
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
                fromPaths(takenRoute, takenRoutePaint),
                fromPath(shortestPath, shortestPathPaint),
                fromPoint(userPosition, userPositionPaint),
                fromPoint(snappedPoint, snappedPointPaint),
            ),
        )
    }

    fun from(state: MapWorldState): MapWorldViewState = with(state) {
        MapWorldViewState(
            worldBounds = worldBounds,
            mapData = flatListOf(
                fromPaths(technicalRoute, technicalPaint),
                fromPaths(roads, roadPaint),
                fromPaths(lines, linesPaint),
                fromPolygons(buildings, buildingPaint),
                fromPolygons(aviary, aviaryPaint),
                fromPaths(oldTakenRoute, takenRoutePaint),
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
            MapItem(
                PolygonD(polygon.vertices),
                paint,
            )
        }

    private fun fromPath(path: List<PointD>, paint: MapPaint): List<MapItem> =
        listOf(
            MapItem(
                PathD(path),
                paint,
            )
        )

    private fun fromPaths(paths: List<PathEntity>, paint: MapPaint): List<MapItem> =
        paths.map { path ->
            MapItem(
                PathD(path.vertices),
                paint,
            )
        }

    private fun fromPoints(points: List<PointD>, paint: MapPaint): List<MapItem> =
        points.map { point ->
            MapItem(
                point,
                paint,
            )
        }

    private fun fromPoint(point: PointD?, paint: MapPaint): List<MapItem> =
        if (point != null) {
            listOf(
                MapItem(
                    point,
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
                    val images = animalsInRegion.map { it.photos }.flatten().shuffled(Random(carouselSeed))
                    add(
                        MapCarouselItem.Region(
                            id = region.id,
                            name = region.id.id.let(regionMapper::from),
                            photoUrlLeftTop = images.getOrNull(0),
                            photoUrlRightTop = images.getOrNull(1),
                            photoUrlLeftBottom = images.getOrNull(2),
                            photoUrlRightBottom = images.getOrNull(3),
                        )
                    )
                } else {
                    animalsInRegion.forEach { animal ->
                        add(
                            MapCarouselItem.Animal(
                                id = animal.id,
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
}
