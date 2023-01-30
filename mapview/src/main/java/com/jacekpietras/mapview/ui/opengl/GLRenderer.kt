package com.jacekpietras.mapview.ui.opengl

import android.graphics.Color
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import com.jacekpietras.mapview.model.OpenGLPaint
import com.jacekpietras.mapview.model.RenderItem
import com.jacekpietras.mapview.model.RenderItem.PointItem.RenderBitmapItem
import com.jacekpietras.mapview.model.RenderItem.PointItem.RenderCircleItem
import com.jacekpietras.mapview.model.RenderItem.RenderPathItem
import com.jacekpietras.mapview.model.RenderItem.RenderPolygonItem
import com.jacekpietras.mapview.ui.LastMapUpdate
import com.jacekpietras.mapview.ui.MapRenderConfig.isDrawing
import com.jacekpietras.mapview.utils.setOpenGLClearColor
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class GLRenderer : GLSurfaceView.Renderer {

    var mapList: List<RenderItem<OpenGLPaint>> = emptyList()
    var openGLBackground: Int = Color.BLUE

    private lateinit var line: Line
    private lateinit var circle: Circle
    private lateinit var polygon: Polygon
    private lateinit var sprite: Sprite

    private val vPMatrix = FloatArray(16)
    private val projectionMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)

    override fun onSurfaceCreated(unused: GL10, config: EGLConfig) {
        setOpenGLClearColor(openGLBackground)

        line = Line()
        circle = Circle()
        polygon = Polygon()
        sprite = Sprite()
    }

    override fun onDrawFrame(unused: GL10) {
        isDrawing.set(true)
        LastMapUpdate.rendS = System.nanoTime()

        // Redraw background color
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)

        // Set the camera position (View matrix)
        Matrix.setLookAtM(viewMatrix, 0, 0f, 0f, 3f, 0f, 0f, 0f, 0f, 1.0f, 0.0f)

        // Calculate the projection and view transformation
        Matrix.multiplyMM(vPMatrix, 0, projectionMatrix, 0, viewMatrix, 0)

        mapList.forEach {
            when (it) {
                is RenderPathItem -> {
                    when (val paint = it.paint) {
                        is OpenGLPaint.Stroke -> line.draw(vPMatrix, it.shape, paint.color, paint.width)
                        is OpenGLPaint.Line -> line.drawTriangles(vPMatrix, it.triangles!!.strip.array, paint.color)
                        is OpenGLPaint.LineBorder -> line.drawClosed(vPMatrix, it.triangles!!.outline.array, paint.color, paint.borderWidth)
                        else -> {}
                    }
                }
                is RenderPolygonItem -> {
                    when (val paint = it.paint) {
                        is OpenGLPaint.Fill -> polygon.draw(vPMatrix, it.shape, it.triangles!!, it.paint.color)
                        is OpenGLPaint.Stroke -> line.drawClosed(vPMatrix, it.shape, paint.color, paint.width)
                        else -> {}
                    }
                }
                is RenderCircleItem -> {
                    circle.draw(vPMatrix, it.cX, it.cY, it.radius, it.paint.color)
                }
                is RenderBitmapItem -> {
                    sprite.draw(vPMatrix, it.cXpivoted, it.cYpivoted, it.bitmap)
                }
            }
        }

        LastMapUpdate.log()
        isDrawing.set(false)
    }

    override fun onSurfaceChanged(unused: GL10, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
        Matrix.frustumM(projectionMatrix, 0, 0f, width.toFloat(), height.toFloat(), 0f, 3f, 7f)
    }
}