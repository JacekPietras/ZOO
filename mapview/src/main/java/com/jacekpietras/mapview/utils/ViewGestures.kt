package com.jacekpietras.mapview.utils

import android.content.Context
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector

abstract class ViewGestures(
    context: Context,
) {

    private var swipeDetector = GestureDetector(context, OnGestureListener())
    private var pinchDetector = ScaleGestureDetector(context, OnScaleGestureListener())
    private var rotateDetector = RotationGestureDetector(OnRotationGestureListener())
    private var rotationBuffer = 0f
    private var zoomBuffer = 1f
    private var zoomX = 1f
    private var zoomY = 1f

    fun onTouchEvent(event: MotionEvent) {
        pinchDetector.onTouchEvent(event)
        swipeDetector.onTouchEvent(event)
        rotateDetector.onTouchEvent(event)
    }

    abstract fun onTransform(cX: Float, cY: Float, scale: Float, rotate: Float, vX: Float, vY: Float)

    abstract fun onClick(x: Float, y: Float)

    fun onScale(cX: Float, cY: Float, scale: Float) {
        onTransform(cX, cY, scale, 0f, 0f, 0f)
    }

    fun onRotate(rotate: Float) {
        onTransform(0f, 0f, 1f, rotate, 0f, 0f)
    }

    fun onScroll(vX: Float, vY: Float) {
        onTransform(0f, 0f, 1f, 0f, vX, vY)
    }

    private inner class OnGestureListener : GestureDetector.OnGestureListener {

        override fun onShowPress(e: MotionEvent) = Unit

        override fun onSingleTapUp(e: MotionEvent): Boolean {
            onClick(e.x, e.y)
            return false
        }

        override fun onDown(e: MotionEvent) = false

        override fun onFling(e1: MotionEvent, e2: MotionEvent, vX: Float, vY: Float) = false

        override fun onScroll(e1: MotionEvent, e2: MotionEvent, vX: Float, vY: Float): Boolean {
            onScroll(vX, vY)
            return false
        }

        override fun onLongPress(e: MotionEvent) = Unit
    }

    private inner class OnScaleGestureListener : ScaleGestureDetector.OnScaleGestureListener {

        override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
            zoomBuffer = 1f
            zoomX = detector.focusX
            zoomY = detector.focusY
            return true
        }

        override fun onScaleEnd(detector: ScaleGestureDetector) = Unit

        override fun onScale(detector: ScaleGestureDetector): Boolean {
            val zoom = detector.scaleFactor
            onScale(zoomX, zoomY, zoomBuffer / zoom)
            zoomBuffer = zoom
            return false
        }
    }

    private inner class OnRotationGestureListener : RotationGestureDetector.OnRotationGestureListener {

        override fun onRotation(rotationDetector: RotationGestureDetector?) {
            val rotate = rotationDetector?.angle ?: 0f
            onRotate(rotate - rotationBuffer)
            rotationBuffer = rotate
        }

        override fun onRotationStart(rotationDetector: RotationGestureDetector?) {
            rotationBuffer = 0f
        }
    }
}