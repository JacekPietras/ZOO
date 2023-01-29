package com.jacekpietras.mapview.ui.opengl

import android.opengl.GLES20
import com.jacekpietras.mapview.utils.BYTES_PER_FLOAT
import com.jacekpietras.mapview.utils.COORDS_PER_VERTEX
import com.jacekpietras.mapview.utils.GL_COLOR_VAR
import com.jacekpietras.mapview.utils.GL_MATRIX_VAR
import com.jacekpietras.mapview.utils.GL_POSITION_VAR
import com.jacekpietras.mapview.utils.allocateFloatBuffer
import com.jacekpietras.mapview.utils.allocateShortBuffer
import com.jacekpietras.mapview.utils.createGLProgram
import java.nio.FloatBuffer
import java.nio.ShortBuffer

internal open class Polygon {

    private val glProgram = createGLProgram()
    private val tLine by lazy { Line() }

    fun draw(mvpMatrix: FloatArray?, line: FloatArray, triangles: ShortArray, color: FloatArray) {
        val data = PolygonShapeData(line, triangles, color)
        GLES20.glUseProgram(glProgram)

        val mMVPMatrixHandle = GLES20.glGetUniformLocation(glProgram, GL_MATRIX_VAR)
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0)

        val mPositionHandle = GLES20.glGetAttribLocation(glProgram, GL_POSITION_VAR)
        GLES20.glEnableVertexAttribArray(mPositionHandle)
        GLES20.glVertexAttribPointer(
            mPositionHandle,
            COORDS_PER_VERTEX,
            GLES20.GL_FLOAT,
            false,
            COORDS_PER_VERTEX * BYTES_PER_FLOAT,
            data.vertexBuffer,
        )

        val mColorHandle = GLES20.glGetUniformLocation(glProgram, GL_COLOR_VAR)
        GLES20.glUniform4fv(mColorHandle, 1, data.color, 0)

        GLES20.glDrawElements(
            GLES20.GL_TRIANGLES,
            data.drawListBuffer.capacity(),
            GLES20.GL_UNSIGNED_SHORT,
            data.drawListBuffer,
        )

        GLES20.glDisableVertexAttribArray(mPositionHandle)

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

        const val showTriangles: Boolean = false
    }
}