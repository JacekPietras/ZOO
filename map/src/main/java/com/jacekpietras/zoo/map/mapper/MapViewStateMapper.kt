package com.jacekpietras.zoo.map.mapper

import android.graphics.PointF
import com.jacekpietras.zoo.map.model.MapState
import com.jacekpietras.zoo.map.model.MapViewState
import com.jacekpietras.zoo.map.model.PathF
import com.jacekpietras.zoo.map.model.PolygonF
import com.jacekpietras.zoo.map.ui.MapItem

internal class MapViewStateMapper {

   fun from(state: MapState, viewState: MapViewState) {
        val mapData = state.buildings.map { polygon ->
            MapItem(
                PolygonF(polygon.vertices.map {
                    PointF(it.lat.toFloat(), it.lon.toFloat())
                }),
                state.buildingPaint
            )
        } + state.roads.map { polygon ->
            MapItem(
                PathF(polygon.vertices.map {
                    PointF(it.lat.toFloat(), it.lon.toFloat())
                }),
                state.roadPaint
            )
        }
        val userPosition = state.userPosition

        viewState.mapData.value = mapData
        viewState.userPosition.value = userPosition
    }
}