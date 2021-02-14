package com.jacekpietras.zoo.map.model

import android.graphics.Color
import com.jacekpietras.core.PointD
import com.jacekpietras.core.RectD
import com.jacekpietras.mapview.model.MapColor
import com.jacekpietras.mapview.model.MapDimension
import com.jacekpietras.mapview.model.MapPaint
import com.jacekpietras.zoo.domain.model.MapItemEntity.PathEntity
import com.jacekpietras.zoo.domain.model.MapItemEntity.PolygonEntity
import com.jacekpietras.zoo.map.R
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

internal data class MapState(
    val regionsInUserPosition: Flow<List<String>> = flowOf(emptyList()),

    val buildings: Flow<List<PolygonEntity>> = flowOf(emptyList()),
    val roads: Flow<List<PathEntity>> = flowOf(emptyList()),
    val lines: Flow<List<PathEntity>> = flowOf(emptyList()),
    val takenRoute: Flow<List<PathEntity>> = flowOf(emptyList()),

    val buildingPaint: Flow<MapPaint> = flowOf(
        MapPaint.FillWithBorder(
            fillColor = MapColor.Attribute(R.attr.colorMapBuilding),
            borderColor = MapColor.Attribute(R.attr.colorMapBuildingBorder),
            borderWidth = MapDimension.Static.Screen(1),
        )
    ),
    val roadPaint: Flow<MapPaint> = flowOf(
        MapPaint.StrokeWithBorder(
            strokeColor = MapColor.Attribute(R.attr.colorMapRoute),
            width = MapDimension.Dynamic.World(2.0),
            borderColor = MapColor.Attribute(R.attr.colorMapRouteBorder),
            borderWidth = MapDimension.Static.Screen(1),
        )
    ),
    val linesPaint: Flow<MapPaint> = flowOf(
        MapPaint.Stroke(
            strokeColor = MapColor.Hard(Color.BLUE),
            width = MapDimension.Dynamic.World(0.5),
        )
    ),
    val takenRoutePaint: Flow<MapPaint> = flowOf(
        MapPaint.Stroke(
            strokeColor = MapColor.Attribute(R.attr.colorMapTaken),
            width = MapDimension.Static.Screen(1),
//            pattern = MapDimension.Static.Screen(8)
        )
    ),

    val userPosition: Flow<PointD>,

    val worldBounds: Flow<RectD>,
)
