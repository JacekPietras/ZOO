package com.jacekpietras.mapview.logic

import com.jacekpietras.geometry.PointD
import com.jacekpietras.mapview.model.MapItem

class UserData(
    var userPosition: PointD? = null,
    var compass: Float = 0f,
    var objectList: List<MapItem> = emptyList(),
)
