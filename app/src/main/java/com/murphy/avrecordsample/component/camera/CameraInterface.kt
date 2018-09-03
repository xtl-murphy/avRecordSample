package com.murphy.opengldemo.feature.component.camera

import android.graphics.SurfaceTexture
import android.view.SurfaceHolder

interface CameraInterface {
    fun openCamera()
    fun openCamera(cameraId : Int)
    fun releaseCamera()
    fun switchCamera(surface: SurfaceTexture)
    fun setPreviewDisplay(surface: SurfaceTexture)
    fun switchCamera(holder: SurfaceHolder)
    fun setPreviewDisplay(holder: SurfaceHolder)
}