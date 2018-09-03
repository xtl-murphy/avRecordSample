package com.murphy.opengldemo.feature.component.view

import android.content.Context
import android.graphics.SurfaceTexture
import android.opengl.EGL14
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.os.Environment
import android.util.AttributeSet
import android.util.Log
import com.murphy.avrecordsample.component.encoder.TextureMovieEncoder
import com.murphy.avrecordsample.utils.OpenGlUtils
import com.murphy.opengldemo.feature.component.camera.CameraCapture
import com.murphy.opengldemo.feature.component.camera.CameraProxy
import com.murphy.avrecordsample.component.encoder.FrameFilter
import java.io.File
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class CameraRecordGLSurfaceView : GLSurfaceView, GLSurfaceView.Renderer {


    private var mRecordingEnabled: Boolean = false
    private var mRecordingStatus: Int = -1
    private val RECORDING_OFF = 0
    private val RECORDING_ON = 1
    private val RECORDING_RESUMED = 2
    private val mSTMatrix = FloatArray(16)

    private val mVideoEncoder = TextureMovieEncoder()


    private lateinit var mSurfaceTexture: SurfaceTexture

    private lateinit var mCameraProxy: CameraProxy
    private var mOutputFile: File
    private var mFilter: FrameFilter

    private var mTextureId: Int = -1

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        setEGLContextClientVersion(2)
        var path = Environment.getExternalStorageDirectory().absolutePath + "/mytest/123camera-test.mp4"
        mOutputFile = File(path)

        if (!mOutputFile.exists()) {
            mOutputFile.createNewFile()
        }
        mFilter = FrameFilter()
        setRenderer(this)
        renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        Log.i("GLSurfaceView_History","onSurfaceCreated")
        GLES20.glClearColor(0.5f, 0.5f, 0.5f, 1.0f)
        mRecordingEnabled = mVideoEncoder.isRecording
        mRecordingStatus = if (mRecordingEnabled)
            RECORDING_RESUMED
        else
            RECORDING_OFF

        mTextureId = OpenGlUtils.getExternalOESTextureID()
//
        mSurfaceTexture = SurfaceTexture(mTextureId)
        mSurfaceTexture.setOnFrameAvailableListener(onFrameAvailableListener)
        mCameraProxy = CameraProxy(CameraCapture())
        mCameraProxy.openCamera()
        mCameraProxy.setPreviewDisplay(mSurfaceTexture)
        mFilter.init(false)

    }

    override fun onDrawFrame(gl: GL10?) {
        Log.i("GLSurfaceView_History","onDrawFrame")
        mSurfaceTexture.updateTexImage()

        if (mRecordingEnabled) {
            when (mRecordingStatus) {
                RECORDING_OFF -> {
                    mVideoEncoder.startRecording(TextureMovieEncoder.EncoderConfig(
                            mOutputFile, 1280, 720, 1000000, EGL14.eglGetCurrentContext()))
                    mRecordingStatus = RECORDING_ON
                }
                RECORDING_RESUMED -> {
                    mVideoEncoder.updateSharedContext(EGL14.eglGetCurrentContext())
                    mRecordingStatus = RECORDING_ON
                }

                RECORDING_ON -> {
                }


            }
        } else {
            when (mRecordingStatus) {
                RECORDING_ON, RECORDING_RESUMED -> {
                    mVideoEncoder.stopRecording()
                    mRecordingStatus = RECORDING_OFF
                }
                RECORDING_OFF -> {

                }
            }
        }
        mVideoEncoder.setTextureId(mTextureId)
        mVideoEncoder.frameAvailable(mSurfaceTexture)
        mSurfaceTexture.getTransformMatrix(mSTMatrix)
//        Log.e("onDrawFrame_1", mSTMatrix.contentToString())
        mFilter.drawFrame(mTextureId, mSTMatrix)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        Log.i("GLSurfaceView_History","onSurfaceChanged")
    }


    fun changeRecordingState(isRecording: Boolean) {
        mRecordingEnabled = isRecording
    }

    private val onFrameAvailableListener = SurfaceTexture.OnFrameAvailableListener {
        Log.i("GLSurfaceView_History","requestRender")
        requestRender()
    }

    override fun onPause() {
        super.onPause()
        mCameraProxy.releaseCamera()
    }

    override fun onResume() {
        super.onResume()
    }
}