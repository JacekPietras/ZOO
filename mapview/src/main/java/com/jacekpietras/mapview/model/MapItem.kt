package com.jacekpietras.mapview.model

class MapItem<T> {
    val shape: Any
    val paint: MapPaint<T>

    constructor(
        polygon: PolygonD,
        paint: MapPaint<T>,
    ) {
        this.shape = polygon
        this.paint = paint
    }

    constructor(
        path: PathD,
        paint: MapPaint<T>,
    ) {
        this.shape = path
        this.paint = paint
    }
}
