package com.jacekpietras.mapview.ui.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.SurfaceView
import com.jacekpietras.mapview.model.RenderItem
import com.jacekpietras.mapview.ui.LastMapUpdate
import com.jacekpietras.mapview.ui.LastMapUpdate.rendS
import com.jacekpietras.mapview.utils.ViewGestures
import com.jacekpietras.mapview.utils.drawMapObjects

class MapSurfaceView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : SurfaceView(context, attrs, defStyleAttr) {

    var onSizeChanged: ((width: Int, height: Int) -> Unit)? = null
    var onClick: ((Float, Float) -> Unit)? = null
    var onTransform: ((Float, Float, Float, Float, Float, Float) -> Unit)? = null
    var mapList: List<RenderItem<Paint>> = emptyList()
        set(value) {
            field = value
            invalidate()
        }

    private val viewGestures = object : ViewGestures(context) {

        override fun onTransform(cX: Float, cY: Float, scale: Float, rotate: Float, vX: Float, vY: Float) {
            onTransform?.invoke(cX, cY, scale, rotate, vX, vY)
        }

        override fun onClick(x: Float, y: Float) {
            onClick?.invoke(x, y)
        }
    }

    init {
        onSizeChanged?.invoke(width, height)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        onSizeChanged?.invoke(width, height)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        onSizeChanged?.invoke(width, height)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        viewGestures.onTouchEvent(event)
        return true
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        rendS = System.nanoTime()

        canvas.drawMapObjects(mapList)

        LastMapUpdate.log()
    }
}
