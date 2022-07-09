package com.jacekpietras.zoo.map.model

import androidx.compose.runtime.Immutable
import com.jacekpietras.geometry.RectD
import com.jacekpietras.mapview.model.MapItem

@Immutable
internal class MapWorldViewState(
    val worldBounds: RectD = RectD(0.0, 0.0, 0.0, 0.0),
    val mapData: List<MapItem> = emptyList(),
)
