package com.jacekpietras.mapview.ui.opengl

import com.jacekpietras.mapview.utils.COORDS_PER_VERTEX
import com.jacekpietras.mapview.utils.allocateFloatBuffer
import com.jacekpietras.mapview.utils.allocateShortBuffer
import java.nio.FloatBuffer
import java.nio.ShortBuffer

internal open class Polygon : ShapeOfTriangles() {

    fun draw(mvpMatrix: FloatArray?, line: FloatArray, triangles: ShortArray, color: FloatArray) {
        val data = PolygonShapeData(line, triangles, color)
        draw(mvpMatrix, data)
    }

    private class PolygonShapeData(path: FloatArray, triangles: ShortArray, colorInt: FloatArray) : ShapeOfTrianglesData(colorInt) {

        override val vertexCount: Int
        override val vertexBuffer: FloatBuffer
        override val drawListBuffer: ShortBuffer

        init {
            vertexCount = path.size / COORDS_PER_VERTEX
            vertexBuffer = allocateFloatBuffer(path)
            drawListBuffer = allocateShortBuffer(triangles)
        }
    }
}
