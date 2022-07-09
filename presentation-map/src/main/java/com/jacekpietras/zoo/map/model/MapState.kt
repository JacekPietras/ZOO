package com.jacekpietras.zoo.map.model

import com.jacekpietras.geometry.PointD

internal data class MapState(
    val userPosition: PointD = PointD(),
    val isToolbarOpened: Boolean = false,
    val toolbarMode: MapToolbarMode? = null,
)
