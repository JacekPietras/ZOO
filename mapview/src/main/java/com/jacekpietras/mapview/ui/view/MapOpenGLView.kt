package com.jacekpietras.mapview.ui.view

import android.content.Context
import android.graphics.Paint
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import com.jacekpietras.mapview.model.RenderItem
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class MapOpenGLView(
    context: Context
) : GLSurfaceView(context) {

    var onSizeChanged: ((width: Int, height: Int) -> Unit)? = null
    var onClick: ((Float, Float) -> Unit)? = null
    var onTransform: ((Float, Float, Float, Float, Float, Float) -> Unit)? = null
    var mapList: List<RenderItem<Paint>> = emptyList()
        set(value) {
            field = value
            replaceMap()
        }

    private val renderer: MyGLRenderer

    init {
        setEGLContextClientVersion(2)
        renderer = MyGLRenderer()
        setRenderer(renderer)
        renderMode = RENDERMODE_WHEN_DIRTY
    }

    private fun replaceMap() {
        renderer.replaceMap()
    }
}

class MyGLRenderer : GLSurfaceView.Renderer {

    override fun onSurfaceCreated(unused: GL10, config: EGLConfig) {
        // Set the background frame color
        GLES20.glClearColor(1.0f, 0.0f, 0.0f, 1.0f)
    }

    override fun onDrawFrame(unused: GL10) {
        // Redraw background color
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
    }

    override fun onSurfaceChanged(unused: GL10, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
    }

    fun replaceMap() {
        GLES20.glClearColor(0.0f, 1.0f, 1.0f, 1.0f)
    }
}

