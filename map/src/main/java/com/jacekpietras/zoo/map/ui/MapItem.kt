package com.jacekpietras.zoo.map.ui

import com.jacekpietras.zoo.map.model.MapPaint
import com.jacekpietras.zoo.map.model.PathF
import com.jacekpietras.zoo.map.model.PolygonF

internal class MapItem {
    val shape: Any
    val paint: MapPaint
    val onClick: ((x: Float, y: Float) -> Unit)?

    constructor(
        polygon: PolygonF,
        paint: MapPaint,
        onClick: ((x: Float, y: Float) -> Unit)? = null,
    ) {
        this.shape = polygon
        this.onClick = onClick
        this.paint = paint
    }

    constructor(
        path: PathF,
        paint: MapPaint,
    ) {
        this.shape = path
        this.onClick = null
        this.paint = paint
    }
}
