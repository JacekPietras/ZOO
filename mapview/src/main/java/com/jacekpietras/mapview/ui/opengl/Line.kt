package com.jacekpietras.mapview.ui.opengl

import android.opengl.GLES20
import com.jacekpietras.mapview.utils.BYTES_PER_FLOAT
import com.jacekpietras.mapview.utils.COORDS_PER_VERTEX
import com.jacekpietras.mapview.utils.GL_COLOR_VAR
import com.jacekpietras.mapview.utils.GL_MATRIX_VAR
import com.jacekpietras.mapview.utils.GL_POSITION_VAR
import com.jacekpietras.mapview.utils.addZDimension
import com.jacekpietras.mapview.utils.addZDimensionAndLoop
import com.jacekpietras.mapview.utils.allocateFloatBuffer
import com.jacekpietras.mapview.utils.createGLProgram
import java.nio.FloatBuffer

internal class Line {

    private val glProgram = createGLProgram()
    private val diamond = Diamond()
    private val circle = Circle()

    fun drawClosed(mvpMatrix: FloatArray?, line: FloatArray, color: Int, thickness: Float) {
        if (thickness >= THICKNESS_BOLD) {
            for (i in 2..line.lastIndex - 2 step 2) {
                diamond.draw(mvpMatrix, line[i], line[i + 1], thickness / 2, color)
            }
        }

        val data = ClosedLineShapeData(line, color)
        draw(mvpMatrix, data, thickness)
    }

    fun draw(mvpMatrix: FloatArray?, line: FloatArray, color: Int, thickness: Float, roundCap: Boolean) {
        if (thickness >= THICKNESS_BOLD) {
            for (i in 2..line.lastIndex - 2 step 2) {
                diamond.draw(mvpMatrix, line[i], line[i + 1], thickness / 2, color)
            }
            if (roundCap) {
                circle.draw(mvpMatrix, line[0], line[1], thickness * 0.45f, color)
                circle.draw(mvpMatrix, line[line.lastIndex - 1], line[line.lastIndex], thickness * 0.45f, color)
            }
        }

        val data = LineShapeData(line, color)
        draw(mvpMatrix, data, thickness)
    }

    private fun draw(mvpMatrix: FloatArray?, data: ShapeData, thickness: Float) {
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

        GLES20.glLineWidth(thickness)
        GLES20.glDrawArrays(GLES20.GL_LINE_STRIP, 0, data.vertexCount)

        GLES20.glDisableVertexAttribArray(mPositionHandle)
    }

    private class LineShapeData(line: FloatArray, colorInt: Int) : ShapeData(colorInt) {

        override val vertexCount: Int
        override val vertexBuffer: FloatBuffer

        init {
            val pathCords = line.addZDimension()
            vertexCount = pathCords.size / COORDS_PER_VERTEX
            vertexBuffer = allocateFloatBuffer(pathCords)
        }
    }

    private class ClosedLineShapeData(line: FloatArray, colorInt: Int) : ShapeData(colorInt) {

        override val vertexCount: Int
        override val vertexBuffer: FloatBuffer

        init {
            val pathCords = line.addZDimensionAndLoop()
            vertexCount = pathCords.size / COORDS_PER_VERTEX
            vertexBuffer = allocateFloatBuffer(pathCords)
        }
    }

    private companion object {

        const val THICKNESS_BOLD = 6
    }
}
