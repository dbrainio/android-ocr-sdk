package com.dbrain.facerecognizer

import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import com.dbrain.recognition.activities.CaptureActivity
import com.dbrain.recognition.camera.CropParameters
import com.dbrain.recognition.processors.DataBundle
import com.dbrain.recognition.utils.dp
import com.dbrain.recognition.utils.getAppTypeface

class FaceCaptureActivity : CaptureActivity() {
    private var cameraFacing = android.hardware.Camera.CameraInfo.CAMERA_FACING_BACK
    private var cropParameters: CropParameters? = null

    private var text: TextView? = null
    override fun getDrawer() = FaceDrawer(this)
    override fun getProcessor(parameters: Bundle?) = FaceProcessor(this, parameters, this)
    override fun getCameraFacing() = cameraFacing
    override fun getCropRegionParameters() = cropParameters

    override fun onCreate(savedInstanceState: Bundle?) {
        intent.extras?.let {
            cameraFacing = it.getInt(ARG_CAMERA_FACING)
            val cropX = it.getFloat(ARG_CROP_REGION_X)
            val cropY = it.getFloat(ARG_CROP_REGION_Y)
            val cropScale = it.getFloat(ARG_CROP_SCALE)
            cropParameters = CropParameters(cropX, cropY, cropScale)
        }

        super.onCreate(savedInstanceState)

        text = TextView(this).apply {
            setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16f)
            setTextColor(0xffffffff.toInt())
            typeface = getAppTypeface(this@FaceCaptureActivity)
            gravity = Gravity.CENTER
            visibility = View.GONE
            text = getString(R.string.move_face)
        }

        getRootLayout()?.addView(
            text, FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
            ).apply {
                bottomMargin = dp(16f, this@FaceCaptureActivity).toInt()
                val m = dp(24f, this@FaceCaptureActivity).toInt()
                leftMargin = m
                rightMargin = m
            }
        )
    }

    override fun onProcessorEvent(dataBundle: DataBundle) {
        super.onProcessorEvent(dataBundle)
        text?.visibility = if (dataBundle.detected) View.VISIBLE else View.GONE
    }

}