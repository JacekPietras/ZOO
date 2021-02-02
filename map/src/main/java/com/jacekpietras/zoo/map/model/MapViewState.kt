package com.jacekpietras.zoo.map.model

import com.jacekpietras.zoo.domain.model.LatLon
import com.jacekpietras.zoo.map.ui.MapItem
import kotlinx.coroutines.flow.MutableStateFlow

internal class MapViewState(
    val mapData: MutableStateFlow<List<MapItem>> = MutableStateFlow(emptyList()),
    val userPosition: MutableStateFlow<LatLon> = MutableStateFlow(LatLon(0.0, 0.0)),
)