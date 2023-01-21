package com.jacekpietras.mapview.ui.view

import android.opengl.GLES20
import com.jacekpietras.mapview.utils.BYTES_PER_FLOAT
import com.jacekpietras.mapview.utils.GL_COLOR_VAR
import com.jacekpietras.mapview.utils.GL_MATRIX_VAR
import com.jacekpietras.mapview.utils.GL_POSITION_VAR
import com.jacekpietras.mapview.utils.allocateFloatBuffer
import com.jacekpietras.mapview.utils.allocateShortBuffer
import com.jacekpietras.mapview.utils.colorToGLFloatArray
import com.jacekpietras.mapview.utils.createProgram

private const val COORDS_PER_VERTEX = 3
//private val pathCords = floatArrayOf(
//    0.0f, 0.0f, 0.0f,
//    500.0f, 500.0f, 0.0f,
//    1080.0f, 0.0f, 0.0f,
//    500.0f, 700.0f, 0.0f,
//    1090f, 2340f, 0.0f,
//)

class Line {

    fun draw(mvpMatrix: FloatArray?, line: FloatArray, color: Int, thickness: Float) {
        val data = LineData(line, color)

        GLES20.glUseProgram(mProgram)

        val mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, GL_MATRIX_VAR)
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0)

        val mPositionHandle = GLES20.glGetAttribLocation(mProgram, GL_POSITION_VAR)
        GLES20.glEnableVertexAttribArray(mPositionHandle)
        GLES20.glVertexAttribPointer(
            mPositionHandle,
            COORDS_PER_VERTEX,
            GLES20.GL_FLOAT,
            false,
            COORDS_PER_VERTEX * BYTES_PER_FLOAT,
            data.vertexBuffer,
        )

        val mColorHandle = GLES20.glGetUniformLocation(mProgram, GL_COLOR_VAR)
        GLES20.glUniform4fv(mColorHandle, 1, data.color, 0)

        GLES20.glLineWidth(thickness)
        GLES20.glDrawArrays(GLES20.GL_LINE_STRIP, 0, data.vertexCount)
        GLES20.glDrawElements(GLES20.GL_LINES, data.vertexCount, GLES20.GL_UNSIGNED_SHORT, data.drawListBuffer)
        GLES20.glDisableVertexAttribArray(mPositionHandle)
        GLES20.glDisable(mColorHandle)
    }

    private companion object {

        private val mProgram = createProgram()
    }

    private class LineData(line: FloatArray, color: Int, pathCords: FloatArray = line.addZDimension()) {

        val color = color.colorToGLFloatArray()
        val vertexCount = pathCords.size / COORDS_PER_VERTEX
        val vertexBuffer = allocateFloatBuffer(pathCords)
        val drawListBuffer = allocateShortBuffer(ShortArray(pathCords.size, Int::toShort))
    }
}

private fun FloatArray.addZDimension(): FloatArray {
    var srcI = 0
    return FloatArray(size / 2 * COORDS_PER_VERTEX) { resIt ->
        if (resIt % COORDS_PER_VERTEX != 2) {
            this[srcI++]
        } else {
            0f
        }
    }
}