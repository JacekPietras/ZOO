package com.jacekpietras.zoo.map.model

import com.jacekpietras.core.PointD
import com.jacekpietras.core.RectD
import com.jacekpietras.mapview.model.MapItem
import kotlinx.coroutines.flow.Flow

internal class MapViewState(
    val currentRegionIds: Flow<String>,
    val compass: Flow<Float>,
    val worldBounds: Flow<RectD>,
    val mapData: Flow<List<MapItem>>,
    val userPosition: Flow<PointD>,
    val snappedPoint: Flow<PointD>,
    val shortestPath: Flow<List<PointD>>,
    val terminalPoints: Flow<List<PointD>>,
    val effect: Flow<MapEffect>,
)

sealed class MapEffect {

    data class ShowToast(
        val text: String,
    ) : MapEffect()

    object CenterAtUser : MapEffect()
}
