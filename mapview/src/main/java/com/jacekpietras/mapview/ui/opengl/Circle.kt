package com.jacekpietras.mapview.ui.opengl

import com.jacekpietras.mapview.utils.COORDS_PER_VERTEX
import com.jacekpietras.mapview.utils.allocateFloatBuffer
import com.jacekpietras.mapview.utils.allocateShortBuffer
import com.jacekpietras.mapview.utils.createCircularIndicesStamp
import com.jacekpietras.mapview.utils.createCircularStamp
import java.nio.FloatBuffer
import java.nio.ShortBuffer

internal class Circle : ShapeOfTriangles() {

    fun draw(mvpMatrix: FloatArray?, cX: Float, cY: Float, radius: Float, color: FloatArray) {
        val data = CircleShapeData(cX, cY, radius, color)
        draw(mvpMatrix, data)
    }

    private class CircleShapeData(cX: Float, cY: Float, radius: Float, color: FloatArray) : ShapeOfTrianglesData(color) {

        override val vertexCount: Int = CIRCLE_POINTS + 1
        override val vertexBuffer: FloatBuffer
        override val drawListBuffer: ShortBuffer

        init {
            val pathCords = FloatArray(stamp.size)
            for (i in pathCords.indices step COORDS_PER_VERTEX) {
                pathCords[i] = stamp[i] * radius + cX
                pathCords[i + 1] = stamp[i + 1] * radius + cY
            }
            vertexBuffer = allocateFloatBuffer(pathCords)
            drawListBuffer = circleDrawListBuffer
        }
    }

    private companion object {

        const val CIRCLE_POINTS = 16
        val stamp = createCircularStamp(CIRCLE_POINTS)
        val stampIndices = createCircularIndicesStamp(CIRCLE_POINTS)
        val circleDrawListBuffer = allocateShortBuffer(stampIndices)
    }
}
