package com.jacekpietras.mapview.ui.opengl

import java.nio.FloatBuffer
import java.nio.ShortBuffer

internal abstract class ShapeData(
    val color: FloatArray,
) {
    abstract val vertexCount: Int
    abstract val vertexBuffer: FloatBuffer
}

internal abstract class ShapeOfTrianglesData(color: FloatArray) : ShapeData(color) {
    abstract val drawListBuffer: ShortBuffer
}
