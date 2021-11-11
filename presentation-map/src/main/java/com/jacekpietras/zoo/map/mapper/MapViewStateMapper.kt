package com.jacekpietras.zoo.map.mapper

import com.jacekpietras.mapview.model.MapItem
import com.jacekpietras.mapview.model.MapPaint
import com.jacekpietras.mapview.model.PathD
import com.jacekpietras.mapview.model.PolygonD
import com.jacekpietras.zoo.core.text.Text
import com.jacekpietras.zoo.domain.model.AnimalEntity
import com.jacekpietras.zoo.domain.model.MapItemEntity.PathEntity
import com.jacekpietras.zoo.domain.model.MapItemEntity.PolygonEntity
import com.jacekpietras.zoo.map.R
import com.jacekpietras.zoo.map.model.*
import kotlin.random.Random

internal class MapViewStateMapper {

    private val carouselSeed = Random.nextLong()

    fun from(state: MapState): MapViewState = with(state) {
        MapViewState(
            isGuidanceShown = isToolbarOpened,
            isBackArrowShown = toolbarMode is MapToolbarMode.SelectedAnimalMode,
            title = when (toolbarMode) {
                is MapToolbarMode.SelectedAnimalMode -> Text(toolbarMode.animal.name)
                is MapToolbarMode.MapActionMode -> Text(toolbarMode.mapAction.title)
                is MapToolbarMode.SelectedRegionMode -> {
                    if (toolbarMode.regionsWithAnimals.size > 1) {
                        Text(R.string.selected)
                    } else {
                        Text(toolbarMode.regionsWithAnimals.first().first)
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

    fun from(state: MapVolatileState): MapVolatileViewState = with(state) {
        MapVolatileViewState(
            compass = compass,
            userPosition = userPosition,
            snappedPoint = snappedPoint,
            shortestPath = shortestPath,
        )
    }

    fun from(state: MapWorldState): MapWorldViewState = with(state) {
        MapWorldViewState(
            worldBounds = worldBounds,
            mapData = flatListOf(
                fromPaths(technicalRoute, technicalPaint),
                fromPaths(roads, roadPaint),
                fromPaths(lines, linesPaint),
                fromPaths(takenRoute, takenRoutePaint),
                fromPolygons(buildings, buildingPaint),
                fromPolygons(aviary, aviaryPaint),
            ),
            terminalPoints = terminalPoints,
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
                paint
            )
        }

    private fun fromPaths(paths: List<PathEntity>, paint: MapPaint): List<MapItem> =
        paths.map { polygon ->
            MapItem(
                PathD(polygon.vertices),
                paint
            )
        }

    private fun getCarousel(regionsWithAnimals: List<Pair<String, List<AnimalEntity>>>) =
        mutableListOf<MapCarouselItem>().apply {
            regionsWithAnimals.forEach { (region, animalsInRegion) ->
                if (animalsInRegion.size > 5 && regionsWithAnimals.size > 1) {
                    val images = animalsInRegion.map { it.photos }.flatten().shuffled(Random(carouselSeed))
                    add(
                        MapCarouselItem.Region(
                            id = region,
                            name = Text(region),
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