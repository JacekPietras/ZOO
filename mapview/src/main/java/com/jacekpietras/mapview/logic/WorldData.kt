package com.jacekpietras.mapview.logic

import com.jacekpietras.geometry.RectD
import com.jacekpietras.mapview.model.MapItem

class WorldData(
    val bounds: RectD = RectD(),
    val objectList: List<MapItem> = emptyList(),
)
