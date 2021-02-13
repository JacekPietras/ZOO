package com.jacekpietras.zoo.map.model

import com.jacekpietras.zoo.domain.model.MapItemEntity.PathEntity
import com.jacekpietras.zoo.domain.model.MapItemEntity.PolygonEntity
import com.jacekpietras.core.PointD
import com.jacekpietras.core.RectD
import com.jacekpietras.mapview.model.MapColor
import com.jacekpietras.mapview.model.MapDimension
import com.jacekpietras.mapview.model.MapPaint
import com.jacekpietras.zoo.map.R
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

internal data class MapState(
    val regionsInUserPosition: Flow<List<String>> = flowOf(emptyList()),

    val buildings: Flow<List<PolygonEntity>> = flowOf(emptyList()),
    val roads: Flow<List<PathEntity>> = flowOf(emptyList()),
    val takenRoute: Flow<List<PathEntity>> = flowOf(emptyList()),

    val buildingPaint: Flow<MapPaint> = flowOf(
        MapPaint.FillWithBorder(
            fillColor = MapColor.Attribute(R.attr.colorMapBuilding),
            borderColor = MapColor.Attribute(R.attr.colorMapBuildingBorder),
            borderWidth = MapDimension.Screen(1),
        )
    ),
    val roadPaint: Flow<MapPaint> = flowOf(
        MapPaint.StrokeWithBorder(
            strokeColor = MapColor.Attribute(R.attr.colorMapRoute),
            width = MapDimension.Screen(2),
            borderColor = MapColor.Attribute(R.attr.colorMapRouteBorder),
            borderWidth = MapDimension.Screen(1),
        )
    ),
    val takenRoutePaint: Flow<MapPaint> = flowOf(
        MapPaint.DashedStroke(
            strokeColor = MapColor.Attribute(R.attr.colorMapTaken),
            width = MapDimension.Screen(2),
            pattern = MapDimension.Screen(8)
        )
    ),

    val userPosition: Flow<PointD>,

    val worldBounds: Flow<RectD>,
)
