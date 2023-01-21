package com.jacekpietras.mapview.ui.view

import android.graphics.Color
import android.graphics.Paint
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import com.jacekpietras.mapview.model.RenderItem
import com.jacekpietras.mapview.ui.LastMapUpdate
import com.jacekpietras.mapview.utils.setOpenGLClearColor
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class MyGLRenderer : GLSurfaceView.Renderer {

    @Volatile
    var mapList: List<RenderItem<Paint>> = emptyList()

    @Volatile
    var openGLBackground: Int = Color.BLUE

    private lateinit var mTriangle: Triangle
    private lateinit var mLine: Line

    private val vPMatrix = FloatArray(16)
    private val projectionMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)

    private val line = floatArrayOf(
        0.0f, 0.0f,
        500.0f, 500.0f,
        1080.0f, 0.0f,
        500.0f, 700.0f,
        1090f, 2340f,
    )

    override fun onSurfaceCreated(unused: GL10, config: EGLConfig) {
        setOpenGLClearColor(openGLBackground)

        mTriangle = Triangle()
        mLine = Line()
    }

    override fun onDrawFrame(unused: GL10) {
        LastMapUpdate.rendS = System.nanoTime()

        // Redraw background color
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)

        // Set the camera position (View matrix)
        Matrix.setLookAtM(viewMatrix, 0, 0f, 0f, 3f, 0f, 0f, 0f, 0f, 1.0f, 0.0f)

        // Calculate the projection and view transformation
        Matrix.multiplyMM(vPMatrix, 0, projectionMatrix, 0, viewMatrix, 0)

        mapList.forEach {
            if(it is RenderItem.RenderPathItem){
                mLine.draw(vPMatrix, it.shape, it.paint.color, 10f)
            }
        }

        LastMapUpdate.log()
    }

    override fun onSurfaceChanged(unused: GL10, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
        Matrix.frustumM(projectionMatrix, 0, 0f, width.toFloat(), height.toFloat(), 0f, 3f, 7f)
    }
}