package com.jacekpietras.zoo.map.model

import com.jacekpietras.zoo.domain.model.LatLon
import com.jacekpietras.zoo.map.ui.MapItem

internal class MapViewState(
    val mapData: List<MapItem>,
    val userPosition: LatLon,
)