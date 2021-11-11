package com.jacekpietras.zoo.map.mapper

import com.jacekpietras.mapview.model.MapItem
import com.jacekpietras.mapview.model.MapPaint
import com.jacekpietras.mapview.model.PathD
import com.jacekpietras.mapview.model.PolygonD
import com.jacekpietras.zoo.core.text.Text
import com.jacekpietras.zoo.domain.model.MapItemEntity.PathEntity
import com.jacekpietras.zoo.domain.model.MapItemEntity.PolygonEntity
import com.jacekpietras.zoo.map.model.*
import java.util.*

internal class MapViewStateMapper {

    fun from(state: MapState): MapViewState = with(state) {
        MapViewState(
            isGuidanceShown = isToolbarOpened,
            isBackArrowShown = selectedAnimal != null,
            title = when {
                selectedAnimal != null -> Text(selectedAnimal.name)
                mapAction != null -> Text(mapAction.title)
                else -> Text.Empty
            },
            mapCarouselItems = if (mapAction == MapAction.AROUND_YOU) {
                getCarousel(state)
            } else {
                emptyList()
            },
            content = when {
                selectedAnimal != null -> Text(selectedAnimal.nameLatin)
                else -> {
                    Text.Listing(regionsInUserPosition.map { Text(it) }) +
                            "\n" +
                            Text.Listing(animalsInUserPosition.map { Text(it.name) })
                }
            },
            mapActions = if (!isToolbarOpened) {
                MapAction.values().asList()
            } else {
                emptyList()
            },
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

    private fun getCarousel(state: MapState) = mutableListOf<MapCarouselItem>().apply {
        state.regionsInUserPosition.forEach { region ->
            val animalsInRegion = state.animalsInUserPosition.filter { it.regionInZoo.contains(region) }
            if (animalsInRegion.size > 5) {
                val images = animalsInRegion.map { it.photos }.flatten().shuffled(Random(100))
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
                            photoUrl = animal.photos.shuffled(Random(100)).firstOrNull(),
                        )
                    )
                }
            }
        }
    }
}