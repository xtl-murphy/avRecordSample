package com.murphy.opengldemo.feature.component.camera

import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Camera
import android.util.Log

class CameraUtils {

    companion object {
        fun checkCameraHardware(context: Context): Boolean {
            return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)
        }

        fun getLargePreviewSize(list: List<Camera.Size>): Camera.Size? {

            if (list.isEmpty()) {
                return null
            }

            var temp: Camera.Size = list[0]

            for (i in 1 until list.size) {
                Log.i("camera_size", list[i].width.toString() + "  " + list[i].height)
                if (list[i].width == 1280 && list[i].height == 720)

                    temp = list[i]
            }

            return temp
        }
    }
}