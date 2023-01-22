package com.jacekpietras.mapview.ui.opengl

import android.opengl.GLES20
import com.jacekpietras.mapview.utils.BYTES_PER_FLOAT
import com.jacekpietras.mapview.utils.COORDS_PER_VERTEX
import com.jacekpietras.mapview.utils.GL_COLOR_VAR
import com.jacekpietras.mapview.utils.GL_MATRIX_VAR
import com.jacekpietras.mapview.utils.GL_POSITION_VAR
import com.jacekpietras.mapview.utils.allocateFloatBuffer
import com.jacekpietras.mapview.utils.allocateShortBuffer
import com.jacekpietras.mapview.utils.createCircularIndicesStamp
import com.jacekpietras.mapview.utils.createCircularStamp
import com.jacekpietras.mapview.utils.createGLProgram
import java.nio.FloatBuffer
import java.nio.ShortBuffer

internal open class Circle {

    private val glProgram = createGLProgram()

    open fun draw(mvpMatrix: FloatArray?, cX: Float, cY: Float, radius: Float, color: Int) {
        val data = CircleShapeData(cX, cY, radius, color)
        draw(mvpMatrix, data)
    }

    protected fun draw(mvpMatrix: FloatArray?, data: OrganizedShapeData) {
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
            data.drawListBuffer
        )

        GLES20.glDisableVertexAttribArray(mPositionHandle)
    }

    private class CircleShapeData(cX: Float, cY: Float, radius: Float, colorInt: Int) : OrganizedShapeData(colorInt) {

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

        const val CIRCLE_POINTS = 16
        val stamp = createCircularStamp(CIRCLE_POINTS)
        val stampIndices = createCircularIndicesStamp(CIRCLE_POINTS)
    }
}
