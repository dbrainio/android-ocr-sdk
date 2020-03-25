package com.dbrain.textrecognizer

import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.dbrain.recognition.activities.CaptureActivity
import com.dbrain.recognition.camera.CropParameters
import com.dbrain.recognition.processors.DataBundle
import com.dbrain.recognition.utils.dp

class TextCaptureActivity : CaptureActivity() {
    private var cameraFacing = android.hardware.Camera.CameraInfo.CAMERA_FACING_BACK
    private var cropParameters: CropParameters? = null

    override fun getDrawer() = TextDrawer(this)
    override fun getProcessor(parameters: Bundle?) = TextProcessor(this, this)
    override fun getCameraFacing() = cameraFacing
    override fun getCropRegionParameters() = cropParameters

    private val histogramView by lazy {
        LuminocityHistogramView(this).apply {
            layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, dp(250f, this@TextCaptureActivity).toInt(), Gravity.BOTTOM)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        intent.extras?.let {
            cameraFacing = it.getInt(ARG_CAMERA_FACING)
            val cropX = it.getFloat(ARG_CROP_REGION_X)
            val cropY = it.getFloat(ARG_CROP_REGION_Y)
            val cropScale = it.getFloat(ARG_CROP_SCALE)
            cropParameters = CropParameters(cropX, cropY, cropScale)
        }

        super.onCreate(savedInstanceState)

        //only for debug purposes
        if (BuildConfig.DEBUG) {
            val root: ViewGroup = findViewById(android.R.id.content)
            root.addView(histogramView)
        }
    }

    override fun onProcessorEvent(dataBundle: DataBundle) {
        super.onProcessorEvent(dataBundle)
        if (dataBundle is HistogramDataBundle) {
            histogramView.notifyHistogramDataChanged(dataBundle.histogramData, dataBundle.xThresholdValue, dataBundle.yThresholdValue)
        }
    }

}