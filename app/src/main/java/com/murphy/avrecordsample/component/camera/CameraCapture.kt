package com.murphy.opengldemo.feature.component.camera

import android.graphics.SurfaceTexture
import android.hardware.Camera
import android.view.SurfaceHolder

class CameraCapture : CameraInterface, Camera.PreviewCallback {



    private var mCamera: Camera? = null
    /**
     * 摄像头支持的预览尺寸集合
     */
    private lateinit var mPreviewSizeList: List<Camera.Size>
    private lateinit var mVideoSizeList: List<Camera.Size>
    //    val nums = Camera.getNumberOfCameras()
    private var mCameraId: Int = Camera.CameraInfo.CAMERA_FACING_FRONT


    private fun setDefaultParameters(camera: Camera) {
        val parameters: Camera.Parameters = camera.parameters

        if (parameters.supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
            parameters.focusMode = Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE
        }


        mPreviewSizeList = parameters.supportedPreviewSizes

        val previewSize = CameraUtils.getLargePreviewSize(mPreviewSizeList)
        if (previewSize != null) {
            parameters.setPreviewSize(previewSize.width, previewSize.height)
//            parameters.setPictureSize(previewSize.width / 2, previewSize.height / 2)
        }

        mVideoSizeList = parameters.supportedVideoSizes

        camera.parameters = parameters
//        camera.setDisplayOrientation(90)
    }

    override fun openCamera() {
        openCamera(mCameraId)
    }

    override fun openCamera(cameraId: Int) {
        try {
            mCamera = Camera.open(cameraId)
            setDefaultParameters(mCamera!!)
        } catch (e: Exception) {
            e.printStackTrace()
            mCamera = null
        }

    }

    override fun releaseCamera() {
        mCamera?.setPreviewCallback(null)
        mCamera?.stopPreview()
        mCamera?.release()
        mCamera = null
    }

    override fun switchCamera(surface: SurfaceTexture) {
        releaseCamera()
        mCameraId = if (mCameraId == Camera.CameraInfo.CAMERA_FACING_BACK) {
            Camera.CameraInfo.CAMERA_FACING_FRONT
        } else {
            Camera.CameraInfo.CAMERA_FACING_BACK
        }
        openCamera(mCameraId)
        setPreviewDisplay(surface)
    }

    override fun switchCamera(holder: SurfaceHolder) {
        releaseCamera()
        mCameraId = if (mCameraId == Camera.CameraInfo.CAMERA_FACING_BACK) {
            Camera.CameraInfo.CAMERA_FACING_FRONT
        } else {
            Camera.CameraInfo.CAMERA_FACING_BACK
        }
        openCamera(mCameraId)
        setPreviewDisplay(holder)
    }

    override fun setPreviewDisplay(holder: SurfaceHolder) {
        mCamera?.setPreviewDisplay(holder)
        mCamera?.startPreview()
    }

    override fun setPreviewDisplay(surface: SurfaceTexture) {
        mCamera?.setPreviewTexture(surface)
        mCamera?.startPreview()
    }


    override fun onPreviewFrame(data: ByteArray, camera: Camera) {

    }

}