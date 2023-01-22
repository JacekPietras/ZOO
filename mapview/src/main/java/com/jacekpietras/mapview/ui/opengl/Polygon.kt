package com.jacekpietras.mapview.ui.opengl

import android.opengl.GLES20
import com.jacekpietras.mapview.utils.BYTES_PER_FLOAT
import com.jacekpietras.mapview.utils.COORDS_PER_VERTEX
import com.jacekpietras.mapview.utils.GL_COLOR_VAR
import com.jacekpietras.mapview.utils.GL_MATRIX_VAR
import com.jacekpietras.mapview.utils.GL_POSITION_VAR
import com.jacekpietras.mapview.utils.addZDimension
import com.jacekpietras.mapview.utils.allocateFloatBuffer
import com.jacekpietras.mapview.utils.allocateShortBuffer
import com.jacekpietras.mapview.utils.createGLProgram
import com.jacekpietras.mapview.utils.createPolygonFanIndicesStamp
import java.nio.FloatBuffer
import java.nio.ShortBuffer

internal open class Polygon {

    private val glProgram = createGLProgram()

    fun draw(mvpMatrix: FloatArray?, line: FloatArray, color: Int) {
        val data = PolygonShapeData(line, color)

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
            GLES20.GL_TRIANGLE_FAN,
            data.drawListBuffer.capacity(),
            GLES20.GL_UNSIGNED_SHORT,
            data.drawListBuffer,
        )

        GLES20.glDisableVertexAttribArray(mPositionHandle)
    }

    private class PolygonShapeData(line: FloatArray, colorInt: Int) : OrganizedShapeData(colorInt) {

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
