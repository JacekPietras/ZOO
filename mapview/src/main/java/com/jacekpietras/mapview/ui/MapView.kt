package com.jacekpietras.mapview.ui

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import com.jacekpietras.core.PointD
import com.jacekpietras.mapview.utils.doAnimation
import com.jacekpietras.mapview.utils.drawPath

class MapView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : GesturedView(context, attrs, defStyleAttr) {

    private val paintBaker = ViewPaintBaker(context)

    val logic = MapViewLogic(
        doAnimation = { lambda -> doAnimation(true, lambda) },
        invalidate = { invalidate() },
        bakeCanvasPaint = paintBaker::bakeCanvasPaint,
        bakeBorderCanvasPaint = paintBaker::bakeBorderCanvasPaint,
    )

    init {
        logic.currentHeight = height
        logic.currentWidth = width
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        logic.currentHeight = height
        logic.currentWidth = width
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        logic.onSizeChanged()
        logic.currentHeight = height
        logic.currentWidth = width
    }

    override fun onScaleBegin(x: Float, y: Float) {
        logic.onScaleBegin()
    }

    override fun onScale(scale: Float) {
        logic.onScale(scale)
    }

    override fun onRotateBegin() {
        logic.onRotateBegin()
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

        logic.draw(
            drawPath = canvas::drawPath,
            drawCircle = canvas::drawCircle,
        )
    }

    fun centerAtUserPosition() {
        logic.centerAtUserPosition()
    }

    fun centerAtPoint(point: PointD) {
        logic.centerAtPoint(point)
    }
}
