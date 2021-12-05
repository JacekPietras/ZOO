package com.jacekpietras.zoo.catalogue.feature.animal.mapper

import androidx.annotation.StringRes
import com.jacekpietras.core.PointD
import com.jacekpietras.mapview.model.MapColor
import com.jacekpietras.mapview.model.MapDimension
import com.jacekpietras.mapview.model.MapItem
import com.jacekpietras.mapview.model.MapPaint
import com.jacekpietras.mapview.model.PathD
import com.jacekpietras.mapview.model.PolygonD
import com.jacekpietras.zoo.catalogue.R
import com.jacekpietras.zoo.catalogue.feature.animal.model.AnimalState
import com.jacekpietras.zoo.catalogue.feature.animal.model.AnimalViewState
import com.jacekpietras.zoo.catalogue.feature.animal.model.TextParagraph
import com.jacekpietras.zoo.core.text.Text
import com.jacekpietras.zoo.domain.model.Feeding
import com.jacekpietras.zoo.domain.model.MapItemEntity

internal class AnimalMapper {

    fun from(state: AnimalState): AnimalViewState =
        AnimalViewState(
            title = Text(state.animal.name),
            subTitle = Text(state.animal.nameLatin),
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
                state.isFavorite == null -> Text.Empty
                state.isFavorite -> Text(R.string.is_not_favorite)
                else -> Text(R.string.is_favorite)
            },
            worldBounds = state.worldBounds,
            mapData = flatListOf(
                fromPaths(state.roads, roadPaint),
                fromPaths(state.pathsToAnimal, pathPaint),
                fromPolygons(state.buildings, buildingPaint),
                fromPolygons(state.aviary, aviaryPaint),
                fromPoints(state.animalPositions, positionsPaint),
            ),
            feeding =
            if (state.animal.feeding.isNotEmpty()) {
                Text.Listing(
                    listOfNotNull(
                        state.animal.feeding.filterUnique()
                            .map { it.toText() }
                            .filterNot { it is Text.Empty }
                            .takeIf { it.isNotEmpty() }
                            ?.let {
                                Text.Listing(
                                    it,
                                    if (it.any { text -> text is Text.Listing }) {
                                        Text("\n")
                                    } else {
                                        Text(", ")
                                    }
                                )
                            },
                        state.animal.feeding.filterRepetitive()
                            .toText()
                            .takeUnless { it is Text.Empty }
                    ),
                    Text("\n")
                )
            } else {
                null
            },
        )

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

    private fun Feeding.toText(): Text =
        listOfNotNull(
            time.takeIf { it.isNotBlank() }?.let(Text::Value),
            weekdays?.let { weekdays ->
                if (weekdays.size <= 5) {
                    weekdays.map { it.toWeekdayName() }.let(Text::Listing)
                } else {
                    Text(R.string.all_week_except) + " " +
                            ((0..6).toList() - weekdays.toSet())
                                .map { it.toWeekdayName() }
                                .let(Text::Listing)
                }
            },
            note?.let(Text::Value),
        ).let {
            when {
                it.isEmpty() -> Text.Empty
                it.size == 1 -> it.first()
                else -> Text.Listing(it, Text("\n"))
            }
        }

    private fun Int.toWeekdayName(): Text =
        when (this) {
            0 -> Text(R.string.monday)
            1 -> Text(R.string.tuesday)
            2 -> Text(R.string.wednesday)
            3 -> Text(R.string.thursday)
            4 -> Text(R.string.friday)
            5 -> Text(R.string.saturday)
            6 -> Text(R.string.sunday)
            else -> throw IllegalArgumentException("There is not weekday $this")
        }

    private fun paragraph(@StringRes title: Int, content: String): TextParagraph? =
        if (content.isNotBlank()) {
            TextParagraph(
                title = Text(title),
                text = Text(content),
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
            MapItem.PolygonMapItem(
                PolygonD(polygon.vertices),
                paint,
            )
        }

    private fun fromPaths(paths: List<MapItemEntity.PathEntity>, paint: MapPaint): List<MapItem> =
        paths.map { path ->
            MapItem.PathMapItem(
                PathD(path.vertices),
                paint,
            )
        }

    private fun fromPoints(points: List<PointD>, paint: MapPaint): List<MapItem> =
        points.map { point ->
            MapItem.CircleMapItem(
                point,
                (paint as MapPaint.Circle).radius,
                paint,
            )
        }

    private companion object {

        val buildingPaint: MapPaint = MapPaint.Fill(
            fillColor = MapColor.Attribute(R.attr.colorSmallMapBuilding),
        )
        val aviaryPaint: MapPaint = MapPaint.Fill(
            fillColor = MapColor.Attribute(R.attr.colorSmallMapBuilding),
        )
        val roadPaint: MapPaint = MapPaint.Stroke(
            strokeColor = MapColor.Attribute(R.attr.colorSmallMapRoad),
            width = MapDimension.Dynamic.World(2.0),
        )
        val pathPaint: MapPaint = MapPaint.DashedStroke(
            strokeColor = MapColor.Attribute(R.attr.colorSmallMapAnimal),
            width = MapDimension.Dynamic.World(4.0),
            pattern = MapDimension.Static.Screen(dp = 4),
        )
        val positionsPaint: MapPaint = MapPaint.Circle(
            fillColor = MapColor.Attribute(R.attr.colorSmallMapAnimal),
            radius = MapDimension.Static.Screen(dp = 4),
        )
    }
}