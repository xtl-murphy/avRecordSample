package com.murphy.avrecordsample.component.wave

import android.opengl.GLES20
import com.murphy.avrecordsample.utils.OpenGlUtils
import com.murphy.avrecordsample.utils.Rotation
import com.murphy.avrecordsample.utils.TextureRotationUtil
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.util.*

class GPUImageFilter {

    companion object {
        val NO_FILTER_VERTEX_SHADER = "" +
                "attribute vec4 position;\n" +
                "attribute vec4 inputTextureCoordinate;\n" +
                " \n" +
                "varying vec2 textureCoordinate;\n" +
                " \n" +
                "void main()\n" +
                "{\n" +
                "    gl_Position = position;\n" +
                "    textureCoordinate = inputTextureCoordinate.xy;\n" +
                "}"
        val NO_FILTER_FRAGMENT_SHADER = "" +
                "varying highp vec2 textureCoordinate;\n" +
                " \n" +
                "uniform sampler2D inputImageTexture;\n" +
                " \n" +
                "void main()\n" +
                "{\n" +
                "     gl_FragColor = texture2D(inputImageTexture, textureCoordinate);\n" +
                "}"
    }

    lateinit var mTextureTransformMatrix: FloatArray
    private var mTextureTransformMatrixLocation: Int = 0

    private var mVertexShader: String = ""
    private var mFragmentShader: String = ""

    var mProgramId: Int = 0
    protected var mGLAttribPosition: Int = 0
    protected var mGLUniformTexture: Int = 0
    protected var mGLAttribTextureCoordinate: Int = 0
    protected var mIsInitialized: Boolean = false

    private val mRunOnDraw: LinkedList<Runnable>

    protected var mInputWidth: Int = 0
    protected var mInputHeight: Int = 0

    protected var mOutputWidth: Int = 0
    protected var mOutputHeight:Int = 0

    protected var mGLCubeBuffer: FloatBuffer
    protected var mGLTextureBuffer: FloatBuffer
    constructor():this(NO_FILTER_VERTEX_SHADER, NO_FILTER_FRAGMENT_SHADER)

    constructor(vertexShader: String, fragmentShader: String) {
        mVertexShader = vertexShader
        mFragmentShader = fragmentShader

        mRunOnDraw = LinkedList()

        mGLCubeBuffer = ByteBuffer.allocateDirect(TextureRotationUtil.CUBE.size * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
        mGLCubeBuffer.put(TextureRotationUtil.CUBE).position(0)
        mGLTextureBuffer = ByteBuffer.allocateDirect(TextureRotationUtil.TEXTURE_NO_ROTATION.size * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
        mGLTextureBuffer.put(TextureRotationUtil.getRotation(Rotation.NORMAL, false, true)).position(0)
    }

    fun init() {
        onInit()
        onInitialized()
    }

    fun onInit() {
        mProgramId = OpenGlUtils.genProgram(mVertexShader, mFragmentShader)
        mTextureTransformMatrixLocation = GLES20.glGetUniformLocation(mProgramId, "textureTransform")
        mGLAttribPosition = GLES20.glGetAttribLocation(mProgramId, "position")
        mGLUniformTexture = GLES20.glGetUniformLocation(mProgramId, "inputImageTexture")
        mGLAttribTextureCoordinate = GLES20.glGetAttribLocation(mProgramId, "inputTextureCoordinate")
        mIsInitialized = true
    }

    fun onInitialized() {

    }

    fun destroy() {
        mIsInitialized = false
        GLES20.glDeleteProgram(mProgramId)
        onDestroy()
    }

    fun onDestroy() {

    }

    fun onInputSizeChanged(width: Int, height: Int) {
        mInputWidth = width
        mInputHeight = height
    }

    fun setTextureTransformMatrix(mtx: FloatArray) {
        mTextureTransformMatrix = mtx
    }

    fun onDrawFrame(textureId: Int, cubeBuffer: FloatBuffer, textureBuffer: FloatBuffer): Int {
        GLES20.glUseProgram(mProgramId)
        if (!mIsInitialized) {
            return OpenGlUtils.NOT_INIT
        }

        cubeBuffer.position(0)

        GLES20.glVertexAttribPointer(mGLAttribPosition, 2, GLES20.GL_FLOAT, false, 0, cubeBuffer)
        GLES20.glEnableVertexAttribArray(mGLAttribPosition)

        textureBuffer.position(0)
        GLES20.glVertexAttribPointer(mGLAttribTextureCoordinate, 2, GLES20.GL_FLOAT, false, 0,
                textureBuffer)
        GLES20.glEnableVertexAttribArray(mGLAttribTextureCoordinate)
        GLES20.glUniformMatrix4fv(mTextureTransformMatrixLocation, 1, false, mTextureTransformMatrix, 0)
        if (textureId != OpenGlUtils.NO_TEXTURE) {
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId)
            GLES20.glUniform1i(mGLUniformTexture, 0)
        }
        onDrawArraysPre()
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)
        GLES20.glDisableVertexAttribArray(mGLAttribPosition)
        GLES20.glDisableVertexAttribArray(mGLAttribTextureCoordinate)
        onDrawArraysAfter()
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
        return OpenGlUtils.ON_DRAWN
    }

    fun onDrawFrame(textureId: Int) {
        onDrawFrame(textureId, mGLCubeBuffer, mGLTextureBuffer)
    }

    protected fun onDrawArraysPre() {}
    protected fun onDrawArraysAfter() {}

    protected fun runOnDraw(runnable: Runnable) {
        synchronized(mRunOnDraw) {
            mRunOnDraw.addLast(runnable)
        }
    }

    fun onDisplaySizeChanged(width: Int, height: Int) {
        mOutputWidth = width
        mOutputHeight = height
    }
}