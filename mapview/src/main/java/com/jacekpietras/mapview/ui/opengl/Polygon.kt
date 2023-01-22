package com.jacekpietras.mapview.ui.opengl

import com.jacekpietras.mapview.utils.COORDS_PER_VERTEX
import com.jacekpietras.mapview.utils.allocateFloatBuffer
import com.jacekpietras.mapview.utils.allocateShortBuffer
import com.jacekpietras.mapview.utils.createPolygonFanIndicesStamp
import java.nio.FloatBuffer
import java.nio.ShortBuffer

internal open class Polygon : ShapeOfTriangles() {

    fun draw(mvpMatrix: FloatArray?, line: FloatArray, color: FloatArray) {
        val data = PolygonShapeData(line, color)
        draw(mvpMatrix, data)
    }

    private class PolygonShapeData(path: FloatArray, colorInt: FloatArray) : ShapeOfTrianglesData(colorInt) {

        override val vertexCount: Int
        override val vertexBuffer: FloatBuffer
        override val drawListBuffer: ShortBuffer

        init {
            vertexCount = path.size / COORDS_PER_VERTEX
            vertexBuffer = allocateFloatBuffer(path)
            drawListBuffer = allocateShortBuffer(createPolygonFanIndicesStamp(vertexCount))
        }
    }
}
