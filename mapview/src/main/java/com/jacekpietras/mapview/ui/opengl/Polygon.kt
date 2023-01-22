package com.jacekpietras.mapview.ui.opengl

import com.jacekpietras.mapview.utils.COORDS_PER_VERTEX
import com.jacekpietras.mapview.utils.addZDimension
import com.jacekpietras.mapview.utils.allocateFloatBuffer
import com.jacekpietras.mapview.utils.allocateShortBuffer
import com.jacekpietras.mapview.utils.createPolygonFanIndicesStamp
import java.nio.FloatBuffer
import java.nio.ShortBuffer

internal open class Polygon : ShapeOfTriangles() {

    fun draw(mvpMatrix: FloatArray?, line: FloatArray, color: Int) {
        val data = PolygonShapeData(line, color)
        draw(mvpMatrix, data)
    }

    private class PolygonShapeData(line: FloatArray, colorInt: Int) : ShapeOfTrianglesData(colorInt) {

        override val vertexCount: Int
        override val vertexBuffer: FloatBuffer
        override val drawListBuffer: ShortBuffer

        init {
            val pathCords = line.addZDimension()
            vertexCount = pathCords.size / COORDS_PER_VERTEX
            vertexBuffer = allocateFloatBuffer(pathCords)
            drawListBuffer = allocateShortBuffer(createPolygonFanIndicesStamp(vertexCount))
        }
    }
}
