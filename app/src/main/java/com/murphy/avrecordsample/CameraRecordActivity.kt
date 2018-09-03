package com.murphy.avrecordsample

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_camera_record.*

class CameraRecordActivity : AppCompatActivity() {

    companion object {
        fun startThisActivity(context: Context) {
            context.startActivity(Intent(context, CameraRecordActivity::class.java))
        }
    }

    private var mRecordingEnabled: Boolean = false      // controls button state

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera_record)

        btnRecord.setOnClickListener {
            mRecordingEnabled = !mRecordingEnabled
            surface_view.changeRecordingState(mRecordingEnabled)
            if (mRecordingEnabled) {
                btnRecord.text = "停止录制"
            } else {
                btnRecord.text = "开始录制"
            }
        }
    }
}