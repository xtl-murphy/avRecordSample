package com.murphy.opengldemo.feature.component.camera

import android.graphics.SurfaceTexture
import android.view.SurfaceHolder
import android.view.View

class CameraProxy(camera: CameraInterface) : CameraInterface {
    override fun switchCamera(holder: SurfaceHolder) {
        mCameraInterface.switchCamera(holder)
    }


    private var mCameraInterface: CameraInterface = camera

    override fun openCamera() {
        mCameraInterface.openCamera()
    }

    override fun releaseCamera() {
        mCameraInterface.releaseCamera()
    }

    override fun switchCamera(surface: SurfaceTexture) {
        mCameraInterface.switchCamera(surface)
    }

    override fun openCamera(cameraId: Int) {
        mCameraInterface.openCamera(cameraId)
    }

    override fun setPreviewDisplay(holder: SurfaceHolder) {
        mCameraInterface.setPreviewDisplay(holder)
    }

    override fun setPreviewDisplay(surface: SurfaceTexture) {
        mCameraInterface.setPreviewDisplay(surface)
    }
}