package com.jacekpietras.zoo.map.ui

import android.graphics.*
import com.jacekpietras.zoo.map.model.MapPaint
import com.jacekpietras.zoo.map.model.PathF
import com.jacekpietras.zoo.map.model.PathsF
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

    constructor(
        paths: PathsF,
        paint: MapPaint,
    ) {
        this.shape = paths
        this.onClick = null
        this.paint = paint
    }

    constructor(
        rect: RectF,
        paint: MapPaint,
        onClick: ((x: Float, y: Float) -> Unit)? = null,
    ) {
        this.shape = rect
        this.onClick = onClick
        this.paint = paint
    }

    constructor(
        rect: Rect,
        paint: MapPaint,
        onClick: ((x: Float, y: Float) -> Unit)? = null,
    ) {
        this.shape = rect
        this.onClick = onClick
        this.paint = paint
    }

    constructor(
        point: PointF,
        paint: MapPaint,
    ) {
        this.shape = point
        this.onClick = null
        this.paint = paint
    }

    constructor(
        point: Point,
        paint: MapPaint,
    ) {
        this.shape = point
        this.onClick = null
        this.paint = paint
    }
}