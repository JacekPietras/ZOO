package com.jacekpietras.mapview.ui.opengl

import android.graphics.Bitmap
import android.opengl.GLES20
import android.opengl.GLUtils
import com.jacekpietras.mapview.utils.BYTES_PER_FLOAT
import com.jacekpietras.mapview.utils.COORDS_PER_VERTEX
import com.jacekpietras.mapview.utils.GL_A_TEX_COORD_VAR
import com.jacekpietras.mapview.utils.GL_COLOR_VAR
import com.jacekpietras.mapview.utils.GL_MATRIX_VAR
import com.jacekpietras.mapview.utils.GL_POSITION_VAR
import com.jacekpietras.mapview.utils.GL_U_TEX_VAR
import com.jacekpietras.mapview.utils.GL_V_TEX_COORD_VAR
import com.jacekpietras.mapview.utils.allocateFloatBuffer
import com.jacekpietras.mapview.utils.allocateShortBuffer
import com.jacekpietras.mapview.utils.loadShader
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer

internal class Sprite {

    private val mCubeTextureCoordinates: FloatBuffer
    private var mTextureUniformHandle = 0
    private var mTextureCoordinateHandle = 0
    private val mTextureCoordinateDataSize = 2
    private val vertexShaderCode =
"""
    attribute vec2 $GL_A_TEX_COORD_VAR;
    varying vec2 $GL_V_TEX_COORD_VAR;
    uniform mat4 $GL_MATRIX_VAR;
    attribute vec4 $GL_POSITION_VAR;
    void main() {
        gl_Position = $GL_MATRIX_VAR * $GL_POSITION_VAR;
        $GL_V_TEX_COORD_VAR = $GL_A_TEX_COORD_VAR;
    }
"""
    private val fragmentShaderCode =
"""
    precision mediump float;
    uniform vec4 $GL_COLOR_VAR;
    uniform sampler2D $GL_U_TEX_VAR;
    varying vec2 $GL_V_TEX_COORD_VAR;
    void main() {
        gl_FragColor = ($GL_COLOR_VAR * texture2D($GL_U_TEX_VAR, $GL_V_TEX_COORD_VAR));
    }
"""
    private val shaderProgram: Int

    init {

        // S, T (or X, Y)
        // Texture coordinate data.
        // Because images have a Y axis pointing downward (values increase as you move down the image) while
        // OpenGL has a Y axis pointing upward, we adjust for that here by flipping the Y axis.
        // What's more is that the texture coordinates are the same for every face.
        val cubeTextureCoordinateData = floatArrayOf(
            -0.5f, 0.5f,
            -0.5f, -0.5f,
            0.5f, -0.5f,
            0.5f, 0.5f,
        )
        mCubeTextureCoordinates = ByteBuffer.allocateDirect(cubeTextureCoordinateData.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
        mCubeTextureCoordinates.put(cubeTextureCoordinateData).position(0)

        val vertexShader: Int = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode)
        val fragmentShader: Int = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode)
        shaderProgram = GLES20.glCreateProgram()
        GLES20.glAttachShader(shaderProgram, vertexShader)
        GLES20.glAttachShader(shaderProgram, fragmentShader)

        GLES20.glBindAttribLocation(shaderProgram, 0, GL_A_TEX_COORD_VAR)
        GLES20.glLinkProgram(shaderProgram)
    }

    fun draw(mvpMatrix: FloatArray?, cX: Float, cY: Float, bitmap: Bitmap) {
        val mTextureDataHandle = loadTexture(bitmap)
        val data = SquareShapeData(cX, cY, bitmap.height.toFloat(), bitmap.width.toFloat())

        GLES20.glUseProgram(shaderProgram)

        val mMVPMatrixHandle = GLES20.glGetUniformLocation(shaderProgram, GL_MATRIX_VAR)
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0)

        val mPositionHandle = GLES20.glGetAttribLocation(shaderProgram, GL_POSITION_VAR)
        GLES20.glEnableVertexAttribArray(mPositionHandle)
        GLES20.glVertexAttribPointer(
            mPositionHandle,
            COORDS_PER_VERTEX,
            GLES20.GL_FLOAT,
            false,
            COORDS_PER_VERTEX * BYTES_PER_FLOAT,
            data.vertexBuffer,
        )

        //Set Texture Handles and bind Texture
        mTextureUniformHandle = GLES20.glGetAttribLocation(shaderProgram, GL_U_TEX_VAR)
        mTextureCoordinateHandle = GLES20.glGetAttribLocation(shaderProgram, GL_A_TEX_COORD_VAR)

        //Set the active texture unit to texture unit 0.
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)

        //Bind the texture to this unit.
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureDataHandle)

        //Tell the texture uniform sampler to use this texture in the shader by binding to texture unit 0.
        GLES20.glUniform1i(mTextureUniformHandle, 0)

        //Pass in the texture coordinate information
        mCubeTextureCoordinates.position(0)
        GLES20.glVertexAttribPointer(
            mTextureCoordinateHandle,
            mTextureCoordinateDataSize,
            GLES20.GL_FLOAT,
            false,
            0,
            mCubeTextureCoordinates
        )
        GLES20.glEnableVertexAttribArray(mTextureCoordinateHandle)

        val mColorHandle = GLES20.glGetUniformLocation(shaderProgram, GL_COLOR_VAR)
        GLES20.glUniform4fv(mColorHandle, 1, color, 0)

        //Draw the triangle
        GLES20.glDrawElements(
            GLES20.GL_TRIANGLES,
            data.drawListBuffer.capacity(),
            GLES20.GL_UNSIGNED_SHORT,
            data.drawListBuffer
        )

        //Disable Vertex Array
        GLES20.glDisableVertexAttribArray(mPositionHandle)
    }

    private fun loadTexture(bitmap: Bitmap): Int {
        val textureHandle = IntArray(1)
        GLES20.glGenTextures(1, textureHandle, 0)
        if (textureHandle[0] != 0) {
            // Bind to the texture in OpenGL
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[0])

            // Set filtering
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST)
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST)

            // Load the bitmap into the bound texture.
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0)
        }
        if (textureHandle[0] == 0) {
            throw RuntimeException("Error loading texture.")
        }
        return textureHandle[0]
    }

    private class SquareShapeData(cX: Float, cY: Float, height: Float, width: Float) : ShapeOfTrianglesData(color) {

        override val vertexCount: Int = 4
        override val vertexBuffer: FloatBuffer
        override val drawListBuffer: ShortBuffer

        init {
            val pathCords = FloatArray(stamp.size)
            for (i in pathCords.indices step COORDS_PER_VERTEX) {
                pathCords[i] = stamp[i] * width + cX
                pathCords[i + 1] = stamp[i + 1] * height + cY
            }
            vertexBuffer = allocateFloatBuffer(pathCords)
            drawListBuffer = sprintDrawListBuffer
        }
    }

    private companion object {

        val stamp = floatArrayOf(
            0f, 1f,
            0f, 0f,
            1f, 0f,
            1f, 1f,
        )
        val stampIndices = shortArrayOf(
            0, 1, 2,
            0, 2, 3
        )
        val sprintDrawListBuffer = allocateShortBuffer(stampIndices)
        var color = floatArrayOf(1f, 0f, 1f, 1.0f)
    }
}
