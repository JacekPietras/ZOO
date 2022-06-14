package com.jacekpietras.mapview.ui

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import com.jacekpietras.core.PointD
import com.jacekpietras.mapview.ui.MapViewLogic.RenderItem.*
import com.jacekpietras.mapview.utils.drawPath

class MapView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : GesturedView(context, attrs, defStyleAttr) {

    private val paintBaker = ViewPaintBaker(context)

    val logic = MapViewLogic(
        invalidate = { invalidate() },
        bakeCanvasPaint = paintBaker::bakeCanvasPaint,
        bakeBorderCanvasPaint = paintBaker::bakeBorderCanvasPaint,
        bakeDimension = paintBaker::bakeDimension,
    )

    init {
        logic.onSizeChanged(width, height)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        logic.onSizeChanged(width, height)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        logic.onSizeChanged(width, height)
    }

    override fun onScale(cX: Float, cY: Float, scale: Float) {
        logic.onScale(cX, cY, scale)
    }

    override fun onRotate(rotate: Float) {
        logic.onRotate(rotate)
    }

    override fun onScroll(vX: Float, vY: Float) {
        logic.onScroll(vX, vY)
    }

    override fun onClick(x: Float, y: Float) {
        logic.onClick(x, y)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        logic.renderList?.forEach {
            when (it) {
                is RenderPathItem -> canvas.drawPath(it.shape, it.paint, false)
                is RenderPolygonItem -> canvas.drawPath(it.shape, it.paint, true)
                is RenderCircleItem -> canvas.drawCircle(it.cX, it.cY, it.radius, it.paint)
            }
        }
    }

    fun centerAtUserPosition() {
        logic.centerAtUserPosition()
    }

    fun centerAtPoint(point: PointD) {
        logic.centerAtPoint(point)
    }
}
