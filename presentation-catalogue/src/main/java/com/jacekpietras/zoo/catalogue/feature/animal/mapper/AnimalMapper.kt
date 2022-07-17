package com.jacekpietras.zoo.catalogue.feature.animal.mapper

import androidx.annotation.StringRes
import com.jacekpietras.geometry.PointD
import com.jacekpietras.geometry.RectD
import com.jacekpietras.mapview.model.MapColor
import com.jacekpietras.mapview.model.MapDimension
import com.jacekpietras.mapview.model.MapItem
import com.jacekpietras.mapview.model.MapItem.MapColoredItem.CircleMapItem
import com.jacekpietras.mapview.model.MapItem.MapColoredItem.PathMapItem
import com.jacekpietras.mapview.model.MapItem.MapColoredItem.PolygonMapItem
import com.jacekpietras.mapview.model.MapPaint
import com.jacekpietras.mapview.model.PathD
import com.jacekpietras.mapview.model.PolygonD
import com.jacekpietras.zoo.catalogue.R
import com.jacekpietras.zoo.catalogue.feature.animal.model.AnimalState
import com.jacekpietras.zoo.catalogue.feature.animal.model.AnimalViewState
import com.jacekpietras.zoo.catalogue.feature.animal.model.TextParagraph
import com.jacekpietras.zoo.core.text.RichText
import com.jacekpietras.zoo.core.theme.MapColors
import com.jacekpietras.zoo.domain.feature.map.model.MapItemEntity
import com.jacekpietras.zoo.domain.model.Feeding

internal class AnimalMapper {

    private lateinit var mapColors: MapColors

    fun setColors(colors: MapColors) {
        mapColors = colors
    }

    fun from(
        worldBounds: RectD,
        buildings: List<MapItemEntity.PolygonEntity>,
        aviary: List<MapItemEntity.PolygonEntity>,
        roads: List<MapItemEntity.PathEntity>,
        pathsToAnimal: List<MapItemEntity.PathEntity>,
        state: AnimalState,
    ): AnimalViewState? {
        if (state.animal == null) return null

        return AnimalViewState(
            title = RichText(state.animal.name),
            subTitle = RichText(state.animal.nameLatin),
            content = with(state.animal) {
                listOfNotNull(
                    paragraph(R.string.occurrence, occurrence),
                    paragraph(R.string.environment, environment),
                    paragraph(R.string.food, food),
                    paragraph(R.string.multiplication, multiplication),
                    paragraph(R.string.protection_and_threats, protectionAndThreats),
                    paragraph(R.string.facts, facts),
                )
            },
            isWikiLinkVisible = state.animal.wiki.isNotBlank(),
            isWebLinkVisible = state.animal.web.isNotBlank(),
            isNavLinkVisible = state.animal.regionInZoo.isNotEmpty(),
            images = state.animal.photos,
            isSeen = state.isSeen,
            favoriteButtonText = when {
                state.isFavorite == null -> RichText.Empty
                state.isFavorite -> RichText(R.string.is_not_favorite)
                else -> RichText(R.string.is_favorite)
            },
            worldBounds = worldBounds,
            mapData =
            with(ComposeColors(mapColors)) {
                flatListOf(
                    fromPaths(roads, roadPaint),
                    fromPaths(pathsToAnimal, pathPaint),
                    fromPolygons(buildings, buildingPaint),
                    fromPolygons(aviary, aviaryPaint),
                    fromPoints(state.animalPositions, positionsPaint),
                )
            },
            feeding =
            if (state.animal.feeding.isNotEmpty()) {
                RichText.Listing(
                    listOfNotNull(
                        state.animal.feeding.filterUnique()
                            .map { it.toText() }
                            .filterNot { it is RichText.Empty }
                            .takeIf { it.isNotEmpty() }
                            ?.let {
                                RichText.Listing(
                                    it,
                                    if (it.any { text -> text is RichText.Listing }) {
                                        RichText("\n")
                                    } else {
                                        RichText(", ")
                                    }
                                )
                            },
                        state.animal.feeding.filterRepetitive()
                            .toText()
                            .takeUnless { it is RichText.Empty }
                    ),
                    RichText("\n")
                )
            } else {
                null
            },
        )
    }

    private fun List<Feeding>.filterRepetitive(): Feeding =
        Feeding(
            time = takeIfRepetitive { it.time } ?: "",
            weekdays = takeIfRepetitive { it.weekdays },
            note = takeIfRepetitive { it.note },
        )

    private fun List<Feeding>.filterUnique(): List<Feeding> =
        map { feeding ->
            Feeding(
                time = takeIfUnique(feeding) { it.time } ?: "",
                weekdays = takeIfUnique(feeding) { it.weekdays },
                note = takeIfUnique(feeding) { it.note },
            )
        }

    private fun <T> List<Feeding>.takeIfUnique(feeding: Feeding, block: (Feeding) -> T): T? =
        if (this.all { block(it) == block(feeding) }) {
            null
        } else {
            block(feeding)
        }

    private fun <T> List<Feeding>.takeIfRepetitive(block: (Feeding) -> T): T? =
        if (isEmpty() || !this.all { block(it) == block(first()) }) {
            null
        } else {
            block(first())
        }

    private fun Feeding.toText(): RichText =
        listOfNotNull(
            time.takeIf { it.isNotBlank() }?.let(RichText::Value),
            weekdays?.let { weekdays ->
                if (weekdays.size <= 5) {
                    weekdays.map { it.toWeekdayName() }.let(RichText::Listing)
                } else {
                    RichText(R.string.all_week_except) + " " +
                            ((0..6).toList() - weekdays.toSet())
                                .map { it.toWeekdayName() }
                                .let(RichText::Listing)
                }
            },
            note?.let(RichText::Value),
        ).let {
            when {
                it.isEmpty() -> RichText.Empty
                it.size == 1 -> it.first()
                else -> RichText.Listing(it, RichText("\n"))
            }
        }

    private fun Int.toWeekdayName(): RichText =
        when (this) {
            0 -> RichText(R.string.monday)
            1 -> RichText(R.string.tuesday)
            2 -> RichText(R.string.wednesday)
            3 -> RichText(R.string.thursday)
            4 -> RichText(R.string.friday)
            5 -> RichText(R.string.saturday)
            6 -> RichText(R.string.sunday)
            else -> throw IllegalArgumentException("There is not weekday $this")
        }

    private fun paragraph(@StringRes title: Int, content: String): TextParagraph? =
        if (content.isNotBlank()) {
            TextParagraph(
                title = RichText(title),
                text = RichText(content),
            )
        } else {
            null
        }

    private fun <T> flatListOf(vararg lists: List<T>): List<T> = listOf(*lists).flatten()

    private fun fromPolygons(
        polygons: List<MapItemEntity.PolygonEntity>,
        paint: MapPaint
    ): List<MapItem> =
        polygons.map { polygon ->
            PolygonMapItem(
                PolygonD(polygon.vertices),
                paint,
            )
        }

    private fun fromPaths(paths: List<MapItemEntity.PathEntity>, paint: MapPaint): List<MapItem> =
        paths.map { path ->
            PathMapItem(
                PathD(path.vertices),
                paint,
            )
        }

    private fun fromPoints(points: List<PointD>, paint: MapPaint): List<MapItem> =
        points.map { point ->
            CircleMapItem(
                point,
                (paint as MapPaint.Circle).radius,
                paint,
            )
        }


    private class ComposeColors(mapColors: MapColors) {

        val buildingPaint: MapPaint = MapPaint.Fill(
            fillColor = MapColor.Compose(mapColors.colorSmallMapBuilding),
        )
        val aviaryPaint: MapPaint = MapPaint.Fill(
            fillColor = MapColor.Compose(mapColors.colorSmallMapBuilding),
        )
        val roadPaint: MapPaint = MapPaint.Stroke(
            strokeColor = MapColor.Compose(mapColors.colorSmallMapRoad),
            width = MapDimension.Dynamic.World(2.0),
        )
        val pathPaint: MapPaint = MapPaint.DashedStroke(
            strokeColor = MapColor.Compose(mapColors.colorSmallMapAnimal),
            width = MapDimension.Dynamic.World(4.0),
            pattern = MapDimension.Static.Screen(dp = 4),
        )
        val positionsPaint: MapPaint = MapPaint.Circle(
            fillColor = MapColor.Compose(mapColors.colorSmallMapAnimal),
            radius = MapDimension.Static.Screen(dp = 4),
        )
    }
}