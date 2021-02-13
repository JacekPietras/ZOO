package com.jacekpietras.mapview.model

class MapItem {
    val shape: Any
    val paint: MapPaint
    val onClick: ((x: Float, y: Float) -> Unit)?

    constructor(
        polygon: PolygonD,
        paint: MapPaint,
        onClick: ((x: Float, y: Float) -> Unit)? = null,
    ) {
        this.shape = polygon
        this.onClick = onClick
        this.paint = paint
    }

    constructor(
        path: PathD,
        paint: MapPaint,
    ) {
        this.shape = path
        this.onClick = null
        this.paint = paint
    }
}
