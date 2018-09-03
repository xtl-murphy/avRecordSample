package com.murphy.opengldemo.feature.component.encoder.gles

import android.opengl.GLES11Ext
import android.opengl.GLES20
import com.murphy.avrecordsample.utils.OpenGlUtils
import java.nio.FloatBuffer

class TextureStuff {

    private val VERTEX_SHADER =
            "uniform mat4 uMVPMatrix;\n" +
                    "uniform mat4 uTexMatrix;\n" +
                    "attribute vec4 aPosition;\n" +
                    "attribute vec4 aTextureCoord;\n" +
                    "varying vec2 vTextureCoord;\n" +
                    "void main() {\n" +
                    "    gl_Position = uMVPMatrix * aPosition;\n" +
                    "    vTextureCoord = (uTexMatrix * aTextureCoord).xy;\n" +
                    "}\n"
    private val FRAGMENT_SHADER_EXT =
            "#extension GL_OES_EGL_image_external : require\n" +
                    "precision mediump float;\n" +
                    "varying vec2 vTextureCoord;\n" +
                    "uniform samplerExternalOES sTexture;\n" +
                    "void main() {\n" +
                    "    gl_FragColor = texture2D(sTexture, vTextureCoord);\n" +
                    "}\n"


    var mProgramId : Int = -1

    var maPositionLoc: Int = -1
    var maTextureCoordLoc: Int = -1
    var muMVPMatrixLoc: Int = -1
    var muTexMatrixLoc: Int = -1
    var muKernelLoc: Int = -1

    fun init() {

        mProgramId = OpenGlUtils.genProgram(VERTEX_SHADER, FRAGMENT_SHADER_EXT)
        if (mProgramId <= 0) {
            throw RuntimeException("Unable to create program")
        }

        maPositionLoc = OpenGlUtils.glGetAttribLocation(mProgramId, "aPosition")
        OpenGlUtils.checkLocation(maPositionLoc, "aPosition")
        maTextureCoordLoc = OpenGlUtils.glGetAttribLocation(mProgramId, "aTextureCoord")
        OpenGlUtils.checkLocation(maTextureCoordLoc, "aTextureCoord")

        muMVPMatrixLoc = OpenGlUtils.glGetUniformLocation(mProgramId, "uMVPMatrix")
        OpenGlUtils.checkLocation(muMVPMatrixLoc, "uMVPMatrix")
        muTexMatrixLoc = OpenGlUtils.glGetUniformLocation(mProgramId, "uTexMatrix")
        OpenGlUtils.checkLocation(muTexMatrixLoc, "uTexMatrix")
        muKernelLoc = OpenGlUtils.glGetUniformLocation(mProgramId, "uKernel")
    }

    fun release() {
        OpenGlUtils.glDeleteProgram(mProgramId)
        mProgramId = -1
    }

    fun draw(mvpMatrix: FloatArray, vertexBuffer: FloatBuffer, firstVertex: Int,
             vertexCount: Int, coordsPerVertex: Int, vertexStride: Int,
             texMatrix: FloatArray, texBuffer: FloatBuffer, textureId: Int, texStride: Int) {
        OpenGlUtils.checkGlError("draw start")

        // Select the program.
        OpenGlUtils.glUseProgram(mProgramId)
        OpenGlUtils.checkGlError("glUseProgram")

        // Set the texture.
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId)

        // Copy the model / view / projection matrix over.
        GLES20.glUniformMatrix4fv(muMVPMatrixLoc, 1, false, mvpMatrix, 0)
        OpenGlUtils.checkGlError("glUniformMatrix4fv")

        // Copy the texture transformation matrix over.
        GLES20.glUniformMatrix4fv(muTexMatrixLoc, 1, false, texMatrix, 0)
        OpenGlUtils.checkGlError("glUniformMatrix4fv")

        // Enable the "aPosition" vertex attribute.
        GLES20.glEnableVertexAttribArray(maPositionLoc)
        OpenGlUtils.checkGlError("glEnableVertexAttribArray")

        // Connect vertexBuffer to "aPosition".
        GLES20.glVertexAttribPointer(maPositionLoc, coordsPerVertex,
                GLES20.GL_FLOAT, false, vertexStride, vertexBuffer)
        OpenGlUtils.checkGlError("glVertexAttribPointer")

        // Enable the "aTextureCoord" vertex attribute.
        GLES20.glEnableVertexAttribArray(maTextureCoordLoc)
        OpenGlUtils.checkGlError("glEnableVertexAttribArray")

        // Connect texBuffer to "aTextureCoord".
        GLES20.glVertexAttribPointer(maTextureCoordLoc, 2,
                GLES20.GL_FLOAT, false, texStride, texBuffer)
        OpenGlUtils.checkGlError("glVertexAttribPointer")

        // Draw the rect.
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, firstVertex, vertexCount)
        OpenGlUtils.checkGlError("glDrawArrays")

        // Done -- disable vertex array, texture, and program.
        GLES20.glDisableVertexAttribArray(maPositionLoc)
        GLES20.glDisableVertexAttribArray(maTextureCoordLoc)
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0)
        GLES20.glUseProgram(0)
    }
}