package com.jacekpietras.zoo.map

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import androidx.core.graphics.plus
import kotlin.math.sqrt

class MapView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : GesturedView(context, attrs, defStyleAttr) {

    private val paint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
        color = Color.RED
    }
    private lateinit var visibleCoordinate: RectF
    private var centerCoordinate: PointF = PointF(20f, 20f)
    private var zoom: Float = 5f

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        calcVisibleCoordinates()
    }

    override fun onScale(scale: Float) {
        zoom = zoom.div(sqrt(scale)).coerceAtMost(10f).coerceAtLeast(2f)
        calcVisibleCoordinates()
        invalidate()
    }

    override fun onScroll(vX: Float, vY: Float) {
        val horizontalScale = width / visibleCoordinate.width()
        val verticalScale = height / visibleCoordinate.height()
        centerCoordinate += PointF(vX / horizontalScale, vY / verticalScale)
        calcVisibleCoordinates()
        invalidate()
    }

    private val redRectangle1 = RectF(18f, 18f, 20f, 20f)
    private val redRectangle2 = RectF(28f, 15f, 40f, 25f)

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        canvas?.drawRect(redRectangle1.toViewCoordinates(), paint)
        canvas?.drawRect(redRectangle2.toViewCoordinates(), paint)
    }

    private fun calcVisibleCoordinates() {
        val ratioZoom = zoom * (height / width.toFloat())
        visibleCoordinate = RectF(
            centerCoordinate.x - zoom,
            centerCoordinate.y - ratioZoom,
            centerCoordinate.x + zoom,
            centerCoordinate.y + ratioZoom
        )
    }

    private fun RectF.toViewCoordinates(): Rect {
        val horizontalScale = width / visibleCoordinate.width()
        val verticalScale = height / visibleCoordinate.height()

        return Rect(
            ((left - visibleCoordinate.left) * horizontalScale).toInt(),
            ((top - visibleCoordinate.top) * verticalScale).toInt(),
            ((right - visibleCoordinate.left) * horizontalScale).toInt(),
            ((bottom - visibleCoordinate.top) * verticalScale).toInt()
        )
    }

    private fun PointF.toViewCoordinates(): Point {
        val horizontalScale = width / visibleCoordinate.width()
        val verticalScale = height / visibleCoordinate.height()

        return Point(
            ((x - visibleCoordinate.left) * horizontalScale).toInt(),
            ((y - visibleCoordinate.top) * verticalScale).toInt()
        )
    }
}