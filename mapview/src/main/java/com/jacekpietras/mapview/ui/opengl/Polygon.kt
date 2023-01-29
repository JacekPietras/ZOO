package com.jacekpietras.mapview.ui.opengl

import com.jacekpietras.mapview.utils.COORDS_PER_VERTEX
import com.jacekpietras.mapview.utils.allocateFloatBuffer
import com.jacekpietras.mapview.utils.allocateShortBuffer
import java.nio.FloatBuffer
import java.nio.ShortBuffer

internal open class Polygon : ShapeOfTriangles() {

    private val tLine by lazy { Line() }

    fun draw(mvpMatrix: FloatArray?, line: FloatArray, triangles: ShortArray, color: FloatArray) {
        val data = PolygonShapeData(line, triangles, color)
        draw(mvpMatrix, data)

        if (showTriangles) {
            triangles.toList().chunked(3).map { t ->
                tLine.drawClosed(
                    mvpMatrix = mvpMatrix,
                    line = t.map { v -> listOf(line[v.toInt() * 2], line[v.toInt() * 2 + 1]) }.flatten().toFloatArray(),
                    color = floatArrayOf(1f, 0f, 0f, 1f),
                    thickness = 3f,
                )
            }
        }
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

    private companion object {

        const val showTriangles: Boolean = true
    }
}