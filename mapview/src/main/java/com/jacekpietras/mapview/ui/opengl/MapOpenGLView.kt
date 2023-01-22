package com.jacekpietras.mapview.ui.opengl

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Paint
import android.opengl.GLSurfaceView
import android.view.MotionEvent
import com.jacekpietras.mapview.model.RenderItem
import com.jacekpietras.mapview.utils.ViewGestures

class MapOpenGLView(
    context: Context
) : GLSurfaceView(context) {

    var onSizeChanged: ((width: Int, height: Int) -> Unit)? = null
    var onClick: ((Float, Float) -> Unit)? = null
    var onTransform: ((Float, Float, Float, Float, Float, Float) -> Unit)? = null
    var openGLBackground: Int
        set(value) {
            renderer.openGLBackground = value
        }
        get() = renderer.openGLBackground
    var mapList: List<RenderItem<Paint>>
        set(value) {
            renderer.mapList = value
            requestRender()
        }
        get() = renderer.mapList

    private val viewGestures = object : ViewGestures(context) {

        override fun onTransform(cX: Float, cY: Float, scale: Float, rotate: Float, vX: Float, vY: Float) {
            onTransform?.invoke(cX, cY, scale, rotate, vX, vY)
        }

        override fun onClick(x: Float, y: Float) {
            onClick?.invoke(x, y)
        }
    }
    private val renderer = GLRenderer()

    init {
        onSizeChanged?.invoke(width, height)
        setEGLContextClientVersion(2)
        setRenderer(renderer)
        renderMode = RENDERMODE_WHEN_DIRTY
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
}
