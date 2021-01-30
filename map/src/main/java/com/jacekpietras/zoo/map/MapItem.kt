package com.jacekpietras.zoo.map

import android.graphics.*

internal class MapItem {
    val shape: Any
    val paint: Paint
    val onClick: ((x: Float, y: Float) -> Unit)?

    constructor(
        polygon: PolygonF,
        paint: Paint,
        onClick: ((x: Float, y: Float) -> Unit)? = null,
    ) {
        this.shape = polygon
        this.onClick = onClick
        this.paint = paint
    }

    constructor(
        path: PathF,
        paint: Paint,
    ) {
        this.shape = path
        this.onClick = null
        this.paint = paint
    }

    constructor(
        paths: PathsF,
        paint: Paint,
    ) {
        this.shape = paths
        this.onClick = null
        this.paint = paint
    }

    constructor(
        rect: RectF,
        paint: Paint,
        onClick: ((x: Float, y: Float) -> Unit)? = null,
    ) {
        this.shape = rect
        this.onClick = onClick
        this.paint = paint
    }

    constructor(
        rect: Rect,
        paint: Paint,
        onClick: ((x: Float, y: Float) -> Unit)? = null,
    ) {
        this.shape = rect
        this.onClick = onClick
        this.paint = paint
    }

    constructor(
        point: PointF,
        paint: Paint,
    ) {
        this.shape = point
        this.onClick = null
        this.paint = paint
    }

    constructor(
        point: Point,
        paint: Paint,
    ) {
        this.shape = point
        this.onClick = null
        this.paint = paint
    }
}