package com.jacekpietras.mapview.ui.opengl

import com.jacekpietras.mapview.utils.COORDS_PER_VERTEX
import com.jacekpietras.mapview.utils.allocateFloatBuffer
import com.jacekpietras.mapview.utils.allocateShortBuffer
import java.nio.FloatBuffer
import java.nio.ShortBuffer

internal class Diamond : ShapeOfTriangles() {

    fun draw(mvpMatrix: FloatArray?, cX: Float, cY: Float, radius: Float, color: FloatArray) {
        val data = SmallCircleShapeData(cX, cY, radius, color)
        draw(mvpMatrix, data)
    }

    private class SmallCircleShapeData(cX: Float, cY: Float, radius: Float, color: FloatArray) : ShapeOfTrianglesData(color) {

        override val vertexCount: Int
        override val vertexBuffer: FloatBuffer
        override val drawListBuffer: ShortBuffer

        init {
            val pathCords = FloatArray(stamp.size)
            for (i in pathCords.indices step COORDS_PER_VERTEX) {
                pathCords[i] = stamp[i] * radius + cX
                pathCords[i + 1] = stamp[i + 1] * radius + cY
            }
            vertexCount = pathCords.size / COORDS_PER_VERTEX
            vertexBuffer = allocateFloatBuffer(pathCords)
            drawListBuffer = allocateShortBuffer(stampIndices)
        }
    }

    private companion object {

        val stamp = floatArrayOf(
            -1f, 0f, 0f,
            0f, -1f, 0f,
            1f, 0f, 0f,
            0f, 1f, 0f,
        )
        val stampIndices = shortArrayOf(
            0, 1, 2,
            0, 2, 3,
        )
    }
}