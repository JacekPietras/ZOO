package com.jacekpietras.zoo.catalogue.feature.animal.mapper

import android.graphics.Color.BLUE
import android.graphics.Color.RED
import androidx.annotation.StringRes
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
                fromPolygons(state.buildings, buildingPaint),
                fromPolygons(state.aviary, aviaryPaint),
            ),
        )

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

    private companion object {

        val buildingPaint: MapPaint = MapPaint.Fill(
            fillColor = MapColor.Hard(RED),
        )
        val aviaryPaint: MapPaint = MapPaint.Fill(
            fillColor = MapColor.Hard(RED),
        )
        val roadPaint: MapPaint = MapPaint.Stroke(
            strokeColor = MapColor.Hard(BLUE),
            width = MapDimension.Dynamic.World(2.0),
        )
    }
}